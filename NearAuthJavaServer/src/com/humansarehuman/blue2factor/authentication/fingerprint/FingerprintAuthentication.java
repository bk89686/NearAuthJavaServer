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
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.AllowCredentials;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.AuthenticatorSelection;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.FingerprintAuthenticationRequest;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.FingerprintAuthenticationResponse;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.FingerprintRegistrationResponse;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.PubKeyCredParam;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.RequestCredential;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.Rp;
import com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint.User;
import com.humansarehuman.blue2factor.entities.tables.AuthenticatorDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(Urls.PASSKEY_AUTHENTICATION)
@SuppressWarnings("ucd")
public class FingerprintAuthentication extends B2fApi {
	@RequestMapping(method = RequestMethod.OPTIONS, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse optionJson(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) throws IOException {
		GeneralUtilities generalUtilities = new GeneralUtilities();
		String url = request.getHeader("Origin");
		new DataAccess().addLog("FingerprintAuthentication", "OPTION");
		httpResponse = generalUtilities.setResponseHeader(httpResponse, url);
		return new ApiResponse(Outcomes.FAILURE, Constants.METHOD_NOT_ALLOWED);
	}

	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody FingerprintRegistrationResponse fingerprintAuthenticationGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		GeneralUtilities generalUtilities = new GeneralUtilities();
		String origin = request.getHeader("Origin");
		String nakedDomain = GeneralUtilities.getNakedDomain(origin);
		Rp rp = new Rp(nakedDomain, nakedDomain, Urls.ICON_PATH);
		User user = new User("", "", "");
		PubKeyCredParam[] pubKeyCredParams = new PubKeyCredParam[0];
		response = generalUtilities.setResponseHeader(response, request.getHeader("Origin"));
		AuthenticatorSelection authenticatorSelection = new AuthenticatorSelection();
		authenticatorSelection.setRequireResidentKey(true);
		authenticatorSelection.setAuthenticatorAttachment("");
		authenticatorSelection.setUserVerification("required");
		FingerprintRegistrationResponse frResp = new FingerprintRegistrationResponse("", authenticatorSelection, "",
				new RequestCredential[0], pubKeyCredParams, rp, 1800000, user);
		return frResp;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody FingerprintAuthenticationResponse fingerprintAuthenticationPost(
			@RequestBody FingerprintAuthenticationRequest frReq, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		FingerprintAuthenticationResponse frResp = new FingerprintAuthenticationResponse("failed", 0, "", null, "");
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		try {
			dataAccess.addLog("start");
			String browserToken = frReq.getBrowserToken();
			String origin = GeneralUtilities.getUrlHost(frReq.reqUrl);
			dataAccess.addLog("browserToken: " + browserToken);
			String credId = frReq.getCredId();
			dataAccess.addLog("credId: " + credId);
			AuthenticatorDbObj authenticator = dataAccess.getActiveAuthenticatorByCredentialId(credId);
			if (authenticator != null) {
				dataAccess.addLog("authicator found");
				TokenDbObj token = dataAccess.getToken(browserToken);
				if (token != null) {
					dataAccess.addLog("token found: " + token);
					frResp = authenticateFingerprint(token, authenticator, frReq, response, credId, origin);

				} else {
					dataAccess.addLog("token not found in db");
				}
			} else {
				dataAccess.addLog("credentialId not found");
				frResp.setChallenge(Constants.FINGERPRINT_CREDENTIALS_NOT_FOUND);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		response = new GeneralUtilities().setResponseHeader(response, frReq.reqUrl);
		return frResp;
	}

	private FingerprintAuthenticationResponse authenticateFingerprint(TokenDbObj token,
			AuthenticatorDbObj authenticator, FingerprintAuthenticationRequest frReq, HttpServletResponse response,
			String credId, String origin) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		GeneralUtilities generalUtilities = new GeneralUtilities();
		FingerprintAuthenticationResponse frResp = null;
		boolean success = false;
		if (authenticator.getBrowserId().equals(token.getBrowserId())) {
			String challenge = GeneralUtilities.randomString(20);
			try {
				DeviceDbObj device = dataAccess.getDeviceByToken(token.getTokenId());
				if (device != null) {
					dataAccess.addLog("authenticateFingerprint", "deviceFound");
					CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
					if (company != null) {
						if (doesUrlMatchRegex(company, frReq.getReqUrl(), dataAccess) != "") {
							response = generalUtilities.setResponseHeader(response, frReq.getReqUrl());
							dataAccess.addLog("authenticateFingerprint", "success");
							success = true;
						}
					} else {
						dataAccess.addLog("authenticateFingerprint", "company not found");
					}
				}
				if (!success) {
					response = generalUtilities.setResponseHeaderWithoutCors(response);
				}
				String[] transport = { "internal" };
				AllowCredentials allowCredentials = new AllowCredentials(credId, "public-key", transport);
				AllowCredentials[] acArray = { allowCredentials };
				AuthenticatorSelection authenticatorSelection = new AuthenticatorSelection();
				authenticatorSelection.userVerification = "required";
				authenticator.setChallenge(challenge);
				dataAccess.updateAuthenticatorByCredId(authenticator);
				dataAccess.addLog("authenticateFingerprint", "challenge: " + challenge);
				frResp = new FingerprintAuthenticationResponse(challenge, 180000, origin, acArray, "required");
			} catch (Exception e) {
				frResp = new FingerprintAuthenticationResponse("failed", 0, "", null, e.getLocalizedMessage());
				dataAccess.addLog("authenticateFingerprint", e);
			}
		} else {
			dataAccess.addLog("authenticateFingerprint", authenticator.getBrowserId() + " <> " + token.getBrowserId());
			dataAccess.addLog("authenticateFingerprint", "credentialId not found for browser", LogConstants.WARNING);
			frResp = new FingerprintAuthenticationResponse("failed", 0, "", null, "credentialId not found for browser");
		}
		return frResp;
	}

}
