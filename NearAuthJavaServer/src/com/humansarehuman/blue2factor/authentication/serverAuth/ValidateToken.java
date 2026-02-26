package com.humansarehuman.blue2factor.authentication.serverAuth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.enums.NonMemberStrategy;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.utilities.jwt.JsonWebToken;

/**
 * This is when we have a token on the server - either held in a cookie or in
 * storage, and we want to make sure the token is valid. .
 * 
 * I'm still not sure if this will be for a JWT or a session variable or both.
 * UPDATE: this is where we will come on every check and assign a jwt that last
 * 20 minutes
 * 
 * @author cjm10
 *
 */
@Controller
@RequestMapping(Urls.TOKEN_VALIDATE)
public class ValidateToken extends B2fApi {
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken validateTokenGetPost(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, @PathVariable("apiKey") String apiKey,
			@RequestHeader(name = "Authorization") String authHeader) {
		ApiResponseWithToken response;
		SamlDataAccess dataAccess = new SamlDataAccess();
		CompanyDbObj company = dataAccess.getCompanyByApiKey(apiKey);
		if (company != null) {
			String jwt = getJwtFromAuthHeader(authHeader, dataAccess);
			dataAccess.addLog("jwt: " + jwt, LogConstants.TRACE);
			if (!TextUtils.isEmpty(jwt) && !jwt.equals("None")) {
				response = validateWithJwt(request, httpResponse, jwt, company, dataAccess);
				if (response.getReason().equals(Constants.NEEDS_SAML_VERIFICATION)) {
					response = null;
				}
			} else {
				String b2fAuth = getPersistentToken(request);
				if (!TextUtils.isEmpty(b2fAuth)) {
					dataAccess.addLog("token was found", LogConstants.TRACE);
					DeviceDbObj device = dataAccess.getDeviceByToken(jwt, "ValidateToken");
					if (device != null) {
						response = validateWithSession(b2fAuth, company, device, dataAccess);
					} else {
						response = new ApiResponseWithToken(Outcomes.FAILURE, Constants.DEVICE_NOT_FOUND);
					}
				} else {
					dataAccess.addLog("neither token nor session were found", LogConstants.WARNING);
					response = new ApiResponseWithToken(Outcomes.FAILURE, Constants.NEEDS_SAML_VERIFICATION);
				}
			}
		} else {
			dataAccess.addLog(Constants.CO_NOT_FOUND, LogConstants.WARNING);
			response = new ApiResponseWithToken(Outcomes.FAILURE, "bad url");
		}
		return response;
	}

	public String getJwtFromAuthHeader(String authHeader, DataAccess dataAccess) {
		String jwt = null;
		String[] authHeaderArray = authHeader.split(" ");
		if (authHeaderArray.length > 1) {
			if (authHeaderArray[0].equals("Bearer")) {
				jwt = authHeaderArray[1];
			}
		}
		dataAccess.addLog("jwt: " + jwt);
		return jwt;
	}

	private ApiResponseWithToken validateWithSession(String b2fAuth, CompanyDbObj company, DeviceDbObj device,
			CompanyDataAccess dataAccess) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		if (device.getSignedIn()) {
			// if (dataAccess.quickIsAccessAllowed(device) {

			if (dataAccess.isAccessAllowedByAuthToken(device, b2fAuth)) {
				BrowserDbObj browser = dataAccess.getBrowserByToken(b2fAuth, TokenDescription.AUTHENTICATION);
				if (browser != null) {
					if (!browser.isExpired()) {
						IdentityObjectFromServer idObj = new IdentityObjectFromServer(browser, false);
						token = new JsonWebToken().buildJwt(idObj);
						outcome = Outcomes.SUCCESS;
					} else {
						dataAccess.expireToken(b2fAuth);
						reason = Constants.BROWSER_NOT_FOUND;
					}
				} else {
					reason = Constants.BROWSER_NOT_FOUND;
				}
			} else {
				reason = Constants.SECOND_FACTOR_FAILED;
			}
		} else {
			reason = Constants.SIGNED_OUT;
		}
		return new ApiResponseWithToken(outcome, reason, token);
	}

	public BrowserDbObj getBrowserFromJwt(HttpServletRequest request, HttpServletResponse httpResponse,
			String authToken, CompanyDbObj company, SamlDataAccess dataAccess) {
		String jwtToken = null;
		BrowserDbObj browser = null;
		try {
			JsonWebToken jwt = new JsonWebToken();
			String[] authTokenArr = authToken.split("&");
			if (authTokenArr.length == 2) {
				String jwtStr = authTokenArr[0];
				String signature = authTokenArr[1];
				if (jwt.validateSignedJwt(company, jwtStr, signature, true)) {
					jwtToken = jwt.getJwtTokenId();
					browser = dataAccess.getBrowserByActiveToken(jwtToken, TokenDescription.JWT);
				}
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return browser;
	}

	private ApiResponseWithToken validateWithJwt(HttpServletRequest request, HttpServletResponse httpResponse,
			String authToken, CompanyDbObj company, SamlDataAccess dataAccess) {
		String jwtToken = null;
		ApiResponseWithToken apiResponse = new ApiResponseWithToken(Outcomes.FAILURE, "", "");
		try {
			JsonWebToken jwt = new JsonWebToken();
			String[] authTokenArr = authToken.split("&");
			if (authTokenArr.length == 2) {
				String jwtStr = authTokenArr[0];
				String signature = authTokenArr[1];
				if (jwt.validateSignedJwt(company, jwtStr, signature, true)) {
					jwtToken = jwt.getJwtTokenId();
					DeviceDbObj device = dataAccess.getActiveDeviceByToken(jwtToken, "validateWithJwt");
					BrowserDbObj browser = dataAccess.getBrowserByActiveToken(jwtToken, TokenDescription.JWT);
					if (browser != null && device != null) {
						if (device.getSignedIn()) {
							IdentityObjectFromServer idObj = new IdentityObjectFromServer(browser, false);
							JsonWebToken jwtBuilder = new JsonWebToken();
							if (dataAccess.isAccessAllowed(device, "validateWithJwt")) {
								dataAccess.addLog("access was allowed");
								if (!browser.isExpired()) {
									dataAccess.addLog("browser was not expired", LogConstants.TRACE);
									apiResponse.setToken(jwtBuilder.buildJwtForServer(idObj));
									apiResponse.setOutcome(Outcomes.SUCCESS);
								} else {
									dataAccess.addLog("browser expired", LogConstants.WARNING);
									dataAccess.expireAllJwtsForToken(jwtToken);
									apiResponse.setReason(Constants.BROWSER_EXPIRED);
								}
							} else {
								apiResponse = handleNonMembers(idObj, dataAccess);
							}
						} else {
							apiResponse.setReason(Constants.NEEDS_SAML_VERIFICATION);
						}
						dataAccess.expireToken(jwtToken);
					} else {
						apiResponse.setReason(Constants.DEVICE_NOT_FOUND);
					}
				} else {
					dataAccess.addLog("jwt failed", LogConstants.WARNING);
					jwtToken = jwt.getJwtTokenId();
					DeviceDbObj device = dataAccess.getDeviceByToken(jwtToken, "validateWithJwt");
					if (device != null) {
						apiResponse.setReason(Constants.NEEDS_SAML_VERIFICATION);
					} else {
						apiResponse.setReason(Constants.INVALID_AUTH_TOKEN);
					}
				}
			} else {
				dataAccess.addLog("invalid token", LogConstants.WARNING);
				apiResponse.setReason(Constants.INVALID_AUTH_TOKEN);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return apiResponse;
	}

	private ApiResponseWithToken handleNonMembers(IdentityObjectFromServer idObj, SamlDataAccess dataAccess) {
		boolean nonMemberAccepted = false;
		String token = "";
		String reason = "";
		int outcome = Outcomes.FAILURE;
		NonMemberStrategy nms = idObj.getCompany().getNonMemberStrategy();
		if (nms != NonMemberStrategy.ALLOW_AUTHENTICATED_ONLY) {
			GroupDbObj group = idObj.getGroup();
			if (group != null) {

				if (nms == NonMemberStrategy.ALLOW_NOT_SIGNED_UP
						|| nms == NonMemberStrategy.ALLOW_NOT_SIGNED_UP_OR_NO_DEVICE) {
					if (group.getGroupName().equals(Constants.ANONYMOUS_GROUP)) {
						nonMemberAccepted = true;
					}
				}
				if (!nonMemberAccepted) {
					if (nms == NonMemberStrategy.ALLOW_NO_DEVICE
							|| nms == NonMemberStrategy.ALLOW_NOT_SIGNED_UP_OR_NO_DEVICE) {
						if (dataAccess.getDevicesByGroupId(group.getGroupId(), true).size() == 0) {
							nonMemberAccepted = true;
						}
					}
				}
			} else {
				if (nms == NonMemberStrategy.ALLOW_NOT_SIGNED_UP
						|| nms == NonMemberStrategy.ALLOW_NOT_SIGNED_UP_OR_NO_DEVICE) {
					nonMemberAccepted = true;
				}
			}
		}
		JsonWebToken jwtBuilder = new JsonWebToken();
		if (nonMemberAccepted) {
			dataAccess.addLog("non member Accepted");
			token = jwtBuilder.buildJwtForServer(idObj);
			outcome = Outcomes.SUCCESS;
		} else {
			token = jwtBuilder.buildExpiredJwt(idObj);
			dataAccess.addLog("factor2 failed");
			reason = Constants.SECOND_FACTOR_FAILED;
		}
		return new ApiResponseWithToken(outcome, reason, token);
	}
}
