package com.humansarehuman.blue2factor.authentication.fingerprint;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
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
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponse;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.AuthenticatorSelection;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.FingerprintRegistrationRequest;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.FingerprintRegistrationResponse;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.PubKeyCredParam;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.RequestCredential;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.Rp;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.User;
import com.humansarehuman.blue2factor.entities.tables.AuthenticatorDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(Urls.PASSKEY_REGISTRATION)
@SuppressWarnings("ucd")
public class FingerprintRegistration extends B2fApi {
	@RequestMapping(method = RequestMethod.OPTIONS, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse optionJson(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) throws IOException {
		GeneralUtilities generalUtilities = new GeneralUtilities();
		String url = request.getHeader("Origin");
		new DataAccess().addLog("FingerprintRegistration", "OPTION");
		httpResponse = generalUtilities.setResponseHeader(httpResponse, url);
		return new ApiResponse(Outcomes.FAILURE, Constants.METHOD_NOT_ALLOWED);
	}

	@RequestMapping(method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody FingerprintRegistrationResponse getJson(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		GeneralUtilities generalUtilities = new GeneralUtilities();
		Rp rp = new Rp(Constants.APP_NAME, Constants.APP_NAME, Urls.ICON_PATH);
		User user = new User("", "", "");
		PubKeyCredParam[] pubKeyCredParams = new PubKeyCredParam[0];
		AuthenticatorSelection authenticatorSelection = new AuthenticatorSelection();
		authenticatorSelection.setRequireResidentKey(false);
		response = generalUtilities.setResponseHeader(response, request.getHeader("Origin"));
		FingerprintRegistrationResponse frResp = new FingerprintRegistrationResponse("", authenticatorSelection, "",
				new RequestCredential[0], pubKeyCredParams, rp, 1800000, user);
		return frResp;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody FingerprintRegistrationResponse fingerprintRegistrationResponsePostJson(
			@RequestBody FingerprintRegistrationRequest frReq, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		boolean success = false;
		String instanceId = frReq.getBrowserSession();
		int logLevel = LogConstants.TEMPORARILY_IMPORTANT;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("instanceId: " + instanceId, logLevel);
		GeneralUtilities generalUtilities = new GeneralUtilities();
		FingerprintRegistrationResponse frResp = null;
		String challenge = GeneralUtilities.randomString(20);
		dataAccess.addLog("challenge: " + challenge, logLevel);
		try {
			Rp rp = null;
			User user = null;
			PubKeyCredParam[] pubKeyCredParams = null;
			TokenDbObj token = dataAccess.getToken(instanceId);
			if (token != null) {
				String origin = GeneralUtilities.getUrlHost(frReq.getReqUrl());
				dataAccess.removeOtherAuthenticator(token.getBrowserId(), origin);
				DeviceDbObj device = dataAccess.getDeviceBySessionTokenIgnoringExpiration(instanceId);
				if (device != null) {
					dataAccess.addLog(device.getDeviceId(), "deviceFound: " + device.toString(), logLevel);
					AuthenticatorDbObj auth = new AuthenticatorDbObj(null, token.getBrowserId(), "public-key", null,
							null, null, 0, DateTimeUtilities.getCurrentTimestamp(), false, challenge,
							GeneralUtilities.getUrlHost(frReq.getReqUrl()));
					dataAccess.addAuthenticator(auth);
					if (device.isActive()) {
						pubKeyCredParams = new PubKeyCredParam[] { new PubKeyCredParam(-7, "public-key"),
								new PubKeyCredParam(-257, "public-key") };
						CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
						dataAccess.addLog(device.getDeviceId(), "using baseUrl: " + origin, logLevel);
						// we may want baseUrl here and not origin
						rp = new Rp(origin, origin, Urls.ICON_PATH);
						GroupDbObj gp = dataAccess.getGroupById(device.getGroupId());
						user = new User(gp.getGroupName(), instanceId, gp.getUsername());
						if (this.doesUrlMatchRegex(company, frReq.getReqUrl(), dataAccess) != null) {
							response = generalUtilities.setResponseHeader(response, frReq.getReqUrl());
							dataAccess.addLog(device.getDeviceId(), "success", logLevel);
							success = true;
						} else {
							dataAccess.addLog(device.getDeviceId(), "regex failed with url: " + frReq.getReqUrl(), logLevel);
						}
					}
				} else {
					dataAccess.addLog("device is null", LogConstants.WARNING);
				}
			} else {
				dataAccess.addLog("token not found", LogConstants.WARNING);
			}
			if (!success) {
				rp = new Rp("", "", Urls.ICON_PATH);
				user = new User("", "", "");
				pubKeyCredParams = new PubKeyCredParam[0];
				response = generalUtilities.setResponseHeaderWithoutCors(response);
			} else {
				dataAccess.expireOtherAutheticatorsForBrowser(instanceId, challenge);
			}
			frResp = new FingerprintRegistrationResponse(frReq.getAttestation(), frReq.getAuthenticatorSelection(),
					challenge, new RequestCredential[0], pubKeyCredParams, rp, 1800000, user);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return frResp;
	}
}
