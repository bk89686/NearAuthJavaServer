package com.humansarehuman.blue2factor.authentication.saml;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.authentication.serverAuth.ValidateToken;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlIdentityProviderDbObj;
import com.humansarehuman.blue2factor.utilities.jwt.JsonWebToken;
import com.humansarehuman.blue2factor.utilities.saml.Saml;

@Controller
@RequestMapping(Urls.SAML_SIGNOUT)
public class SamlSignout extends B2fApi {
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken samlSignoutProcessGet(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, @PathVariable("apiKey") String apiKey,
			@RequestHeader(name = "Authorization") String authHeader) {
		SamlDataAccess dataAccess = new SamlDataAccess();
		int outcome = Outcomes.FAILURE;
		dataAccess.addLog("start");
		CompanyDbObj company = dataAccess.getCompanyByApiKey(apiKey);
		if (company != null) {
			String jwt = new ValidateToken().getJwtFromAuthHeader(authHeader, dataAccess);
			IdentityObjectFromServer idObj = getIdObjFromJwt(request, httpResponse, jwt, company, dataAccess);
			dataAccess.addLog("set up IdObj");
			outcome = signoutWithOutcome(idObj, dataAccess);
		}
		return new ApiResponseWithToken(outcome, "");
	}

	public IdentityObjectFromServer getIdObjFromJwt(HttpServletRequest request, HttpServletResponse httpResponse,
			String authToken, CompanyDbObj company, SamlDataAccess dataAccess) {
		String jwtToken = null;
		IdentityObjectFromServer idObj = null;
		dataAccess.addLog("start");
		try {
			JsonWebToken jwt = new JsonWebToken();
			String[] authTokenArr = authToken.split("&");
			if (authTokenArr.length == 2) {
				String jwtStr = authTokenArr[0];
				String signature = authTokenArr[1];
				if (!TextUtils.isEmpty(jwtStr)) {
					if (jwt.validateSignedJwt(company, jwtStr, signature, true)) {
						jwtToken = jwt.getJwtTokenId();
						BrowserDbObj browser = dataAccess.getBrowserByActiveToken(jwtToken, TokenDescription.JWT);
						DeviceDbObj device = dataAccess.getDeviceByBrowserId(browser.getBrowserId());
						idObj = new IdentityObjectFromServer(company, device, browser, null, false);
					} else {
						dataAccess.addLog("token wasn't valid");
					}
				} else {
					dataAccess.addLog("jwt was empty");
				}
			} else {
				dataAccess.addLog("token was too short");
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return idObj;
	}

	public int signoutWithOutcome(IdentityObjectFromServer idObj, SamlDataAccess dataAccess) {
		int outcome = Outcomes.FAILURE;
		if (idObj != null) {
			DeviceDbObj device = idObj.getDevice();
			if (device != null) {
				dataAccess.expireChecksForDevice(device);
				if (device.getDeviceClass() == DeviceClass.TEMP) {
					dataAccess.deactivateTempDevice(device);
				} else {
					dataAccess.addLog(device.getDeviceId(), "user is signing out", LogConstants.IMPORTANT);
					dataAccess.setSignedIn(device, false);
				}
			}
			if (idObj.getCompany() != null) {
				SamlIdentityProviderDbObj idp = dataAccess.getSamlIdpFromCompany(idObj.getCompany());
				if (idp != null && idObj.getCompany() != null && idObj.getGroup() != null) {
					dataAccess.expireFingerprintAndPush(device);
					Saml saml = new Saml();
					LogoutRequest logout = saml.buildSignoutRequest(idp, idObj);
					saml.logoutRequestToString(logout, false); // TODO: this is not sent, but we are signed out - cjm
					String[] logoutParams = idp.getLogoutUrl().split("::");
					if (logoutParams.length > 1) {
						try {
							if (device != null) {
								dataAccess.addLog("success");
								outcome = Outcomes.SUCCESS;
							}
						} catch (Exception e) {
							dataAccess.addLog(e);
						}
					} else {
						dataAccess.addLog("bad logout params");
					}
				}
			}
		} else {
			dataAccess.addLog("idp was null");
		}
		return outcome;
	}

	public String signout(HttpServletResponse httpResponse, HttpServletRequest request, ModelMap model,
			IdentityObjectFromServer idObj, CompanyDbObj company, SamlDataAccess dataAccess) {
		String nextPage = "notSignedUp";
		dataAccess.addLog("start");
		if (idObj != null) {
			DeviceDbObj device = idObj.getDevice();
			if (device != null) {
				dataAccess.expireChecksForDevice(device);
				if (device.getDeviceClass() == DeviceClass.TEMP) {
					dataAccess.deactivateTempDevice(device);
				} else {
					dataAccess.addLog("user is signing out - in signout", LogConstants.IMPORTANT);
					dataAccess.setSignedIn(device, false);
				}
			}
			if (company != null) {
				if (device != null) {
				} else {
					dataAccess.addLog("signout", "device was null");
				}
				SamlIdentityProviderDbObj idp = dataAccess.getSamlIdpFromCompany(company);
				if (idp != null) {
					dataAccess.addLog("signout", "idp not null");
				}

				if (idObj.getCompany() != null) {
					dataAccess.addLog("signout", "co not null");
				}

				if (idObj.getGroup() != null) {
					dataAccess.addLog("signout", "group not null");
				}
				if (idp != null && idObj.getCompany() != null && idObj.getGroup() != null) {
					dataAccess.expirePushAndBioChecksForPeripheralDevice(device);
					Saml saml = new Saml();
					LogoutRequest logout = saml.buildSignoutRequest(idp, idObj);
					saml.logoutRequestToString(logout, false);
					String[] logoutParams = idp.getLogoutUrl().split("::");
					if (logoutParams.length > 1) {
						try {
							dataAccess.addLog("signout", "signoutType: " + logoutParams[0]);
							dataAccess.expireFingerprintAndPush(device);
							if (logoutParams[0].toLowerCase().endsWith("redirect")) {
								String redirect = logoutParams[1];// + "?SAMLRequest=" + encoded;
								new B2fApi().redirectSamlRequestToIdp(httpResponse, logout, null, redirect);
							} else if (logoutParams[0].toLowerCase().endsWith("post")) {
								nextPage = "samlRequest";
							}
						} catch (Exception e) {
							dataAccess.addLog("signout", e);
						}
					} else {
						dataAccess.addLog("signout", "bad logout params");
					}
				}
			}
		} else {
			dataAccess.addLog("signout", "idp was null");
		}
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return nextPage;
	}
}
