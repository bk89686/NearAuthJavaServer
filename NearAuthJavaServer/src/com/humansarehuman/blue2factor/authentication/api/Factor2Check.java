package com.humansarehuman.blue2factor.authentication.api;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.communication.PushNotifications;
import com.humansarehuman.blue2factor.communication.twilio.TextMessage;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.ConnectedAndConnectionType;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.enums.CompanyAllowed2FaMethods;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.jsonConversion.JsRequest;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithTokenAndJwt;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.CompanySettings;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.humansarehuman.blue2factor.utilities.jwt.JsonWebToken;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(Urls.FACTOR_2_CHECK)
@SuppressWarnings("ucd")
public class Factor2Check extends B2fApi {

	@RequestMapping(method = RequestMethod.OPTIONS)
	public void processOptions(HttpServletRequest request, HttpServletResponse httpResponse) throws IOException {
		GeneralUtilities generalUtilities = new GeneralUtilities();
		String origin = request.getHeader("origin");
		new DataAccess().addLog("Factor2Check", "OPTION from " + origin);
		httpResponse = generalUtilities.setResponseHeader(httpResponse, origin);
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken processGet(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		ApiResponseWithToken response = new ApiResponseWithToken(Outcomes.FAILURE, "method not allowed");
		return response;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken browserCheckPost(@RequestBody JsRequest jsRequest,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		ApiResponseWithToken response;
		int logLevel = LogConstants.TRACE;
		String encryptedSession = jsRequest.getEncryptedSession();
		String token = jsRequest.getToken();
		String requestType = jsRequest.getReqType();
		String userAgent = jsRequest.getUserAgent();
		String reqUrl = GeneralUtilities.getUrlHost(jsRequest.getReqUrl());
		String deviceId = jsRequest.getDeviceId();
		String companyId = jsRequest.getCompanyIdFromUrl();
		boolean fromBrowser = jsRequest.isFromBrowser();
		GeneralUtilities generalUtilities = new GeneralUtilities();
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.addLog("baseUrl: " + reqUrl, logLevel);
		dataAccess.addLog("requestType: " + requestType, logLevel);
		dataAccess.addLog("browserToken: " + token, logLevel);
		dataAccess.addLog("browserSession: " + encryptedSession, logLevel);
		dataAccess.addLog("deviceId: " + deviceId, logLevel);
		dataAccess.addLog("companyId: " + companyId, logLevel);
		Encryption encryption = new Encryption();
		String session;
		try {
			if (requestType.equals(Constants.FACTOR_2_CHECK)) {
				response = this.checkSecondFactorFromApi(dataAccess, encryption, token, encryptedSession, userAgent,
						reqUrl, companyId);
			} else if (requestType.equals(Constants.FACTOR_2_CHECK_FROM_SERVER)) {
				response = this.checkSecondFactorFromServer(dataAccess, encryption, token, encryptedSession, deviceId,
						userAgent, reqUrl, fromBrowser);
			} else if (requestType.equals(Constants.CONFIRM_TOKEN)) {
				session = encryption.decryptBasedOnBrowserOrServerId(token, encryptedSession, reqUrl);
				response = expireInstance(session, reqUrl);
			} else if (requestType.equals(Constants.SETUP)) {
				response = setupNew(token, encryptedSession, reqUrl);
			} else if (requestType.equals(Constants.HANDLE_FAILURE)) {
				response = handleFailure(token, reqUrl);
			} else if (requestType.equals(Constants.SIGNUP_CHECK)) {
				response = validateSignupCheck(instanceId);
			} else if (requestType.equals(Constants.FINGERPRINT_NOT_AVAILABLE)) {
				// TODO: redundant with SEND_LOUD_PUSH -- cjm
				session = encryption.decryptBasedOnBrowserOrServerId(token, encryptedSession, reqUrl);
				response = performSecondaryAuthentication(session);
			} else if (requestType.equals(Constants.PUSH_CHECK)) {
				session = encryption.decryptBasedOnBrowserOrServerId(token, encryptedSession, reqUrl);
				response = validateAccess(session, reqUrl, companyId);
			} else if (requestType.equals(Constants.DEALLOCATE)) {
				response = deactivateKeysAndBiometrics(dataAccess, encryptedSession, reqUrl);
			} else if (requestType.equals(Constants.SEND_LOUD_PUSH)) {
				session = encryption.decryptBasedOnBrowserOrServerId(token, encryptedSession, reqUrl);
				response = performSecondaryAuthentication(session);
			} else if (requestType.equals(Constants.LOG_IT)) {
				dataAccess.addLog("jsLogging: " + token, LogConstants.IMPORTANT);
				response = new ApiResponseWithToken(Outcomes.SUCCESS, "", "");
			} else {
				response = new ApiResponseWithToken(Outcomes.ERROR, "bad request: " + requestType, "");
			}
			httpResponse = generalUtilities.setResponseHeader(httpResponse, reqUrl);
		} catch (Exception e) {
			dataAccess.addLog(e);
			response = new ApiResponseWithToken(Outcomes.ERROR, e.getMessage(), "");
		}
		dataAccess.addLog(
				"outcome: " + response.getOutcome() + ", reason: " + response.getReason() + ", token: " + token);
		return response;
	}

	private ApiResponseWithToken deactivateKeysAndBiometrics(DataAccess dataAccess, String browserSession,
			String reqUrl) {
		BrowserDbObj browser = dataAccess.getBrowserByToken(browserSession, TokenDescription.BROWSER_SESSION);
		ApiResponseWithToken response;
		if (browser != null) {
			response = rescindKeys(browser, reqUrl);
			response = deactivateFingerprint(browser, reqUrl);
		} else {
			response = new ApiResponseWithToken(Outcomes.API_F2_FAILURE, Constants.BROWSER_NOT_FOUND, "");
		}
		return response;
	}

	public ApiResponseWithToken deactivateFingerprint(BrowserDbObj browser, String reqUrl) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		int outcome = Outcomes.FAILURE;
		if (dataAccess.rescindFingerprintsForBrowser(browser, reqUrl)) {
			outcome = Outcomes.SUCCESS;
			dataAccess.addLog("keys were successfully deactivated");
		}
		return new ApiResponseWithToken(outcome, "", "");
	}

	public ApiResponseWithToken rescindKeys(BrowserDbObj browser, String url) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		int outcome = Outcomes.FAILURE;
		if (dataAccess.rescindKeysForBrowser(browser, url)) {
			outcome = Outcomes.SUCCESS;
			dataAccess.addLog("keys were successfully deactivated");
		}
		return new ApiResponseWithToken(outcome, "", "");
	}

	public ApiResponseWithToken rescindTokens(String tokenId) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		int outcome = Outcomes.FAILURE;
		TokenDbObj token = dataAccess.getToken(tokenId);
		if (token != null) {
			dataAccess.addLog("token found");
			String browserId = token.getBrowserId();
			dataAccess.rescindTokensForBrowser(browserId);
			outcome = Outcomes.SUCCESS;
		} else {
			dataAccess.addLog("token not found with id: " + tokenId);
		}
		return new ApiResponseWithToken(outcome, "", "");
	}

	public ApiResponseWithToken validatePushCheck(String tokenId, String url) {
		String reason = "";
		String tokenStr = "";
		ApiResponseWithToken response;
		if (tokenId != null) {
			TokenDbObj oldToken = null;
			CompanyDataAccess dataAccess = new CompanyDataAccess();
			try {
				dataAccess.addLog("instanceId: " + tokenId);
				DeviceDbObj device = null;
				if (!TextUtils.isBlank(tokenId)) {
					oldToken = dataAccess.getToken(tokenId);
					device = dataAccess.getDeviceByToken(tokenId);
				}
				response = completePushValidation(dataAccess, device, oldToken, url);
			} catch (Exception e) {
				response = new ApiResponseWithToken(ERROR, e.getLocalizedMessage(), tokenStr);
			}
			if (!TextUtils.isBlank(response.getReason())) {
				dataAccess.addLog(reason);
			}
		} else {
			response = new ApiResponseWithToken(Outcomes.FAILURE, "", "");
		}
		return response;
	}

	private ApiResponseWithToken completePushValidation(CompanyDataAccess dataAccess, DeviceDbObj device,
			TokenDbObj oldToken, String url) {
		String reason = "";
		int outcome = Outcomes.FAILURE;
		String tokenStr = "";
		if (device != null) {
			CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
			if (company != null) {
				baseUrl = company.getCompanyBaseUrl();
				ArrayList<CompanyAllowed2FaMethods> authMethods = CompanySettings.getAllowedTypes(device);
				if (authMethods.contains(CompanyAllowed2FaMethods.PUSH)) {
					ConnectedAndConnectionType cct = dataAccess.didGiveAccess(device);
					if (cct.isConnected()) {
						dataAccess.addLog(device.getDeviceId(), "device is allowed");
						TokenDbObj token = dataAccess.addToken(device, oldToken.getBrowserId(),
								TokenDescription.BROWSER_TOKEN, url);
						tokenStr = token.getTokenId();
						outcome = Outcomes.SUCCESS;
					} else {
						reason = "push response not found";
					}
				} else {
					outcome = Outcomes.ERROR;
					reason = "push not allowed by company";
				}
			} else {
				reason = "bad company";
			}
		} else {
			reason = "browser was null";
		}
		return new ApiResponseWithToken(outcome, reason, tokenStr);
	}

	private ApiResponseWithToken performSecondaryAuthentication(String tokenId) {
		String reason = "";
		int outcome = Outcomes.FAILURE;
		String tokenStr = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		String deviceId = "";
		if (tokenId != null) {
			try {
				dataAccess.addLog("instanceId: " + tokenId);
				tokenStr = tokenId;
				DeviceDbObj device = null;
				if (!TextUtils.isBlank(tokenStr)) {
					device = dataAccess.getDeviceByToken(tokenStr);
					if (device != null) {
						outcome = handleNotProximate(device);
					}
				}
			} catch (Exception e) {
				dataAccess.addLog(deviceId, e);
			}
		}
		return new ApiResponseWithToken(outcome, reason, tokenStr);
	}

	private ApiResponseWithTokenAndJwt setupNew(String accessCode, String browserSessionStr, String url) {
		int outcome = Outcomes.ERROR;
		String reason = "";
		String tokenStr = "";
		String jwt = "";
		SamlDataAccess dataAccess = new SamlDataAccess();
		int logLevel = LogConstants.TEMPORARILY_IMPORTANT;
		dataAccess.addLog("called with code: " + accessCode, logLevel);
		try {
			AccessCodeDbObj access = dataAccess.getAccessCodeFromAccessString(accessCode);
			if (access != null) {
				Timestamp accessTimestamp = access.getCreateDate();
				double seconds = DateTimeUtilities.timeDifferenceInSecondsFromNow(accessTimestamp);
				if (seconds < 600) {
					DeviceDbObj device = dataAccess.getDeviceByDeviceId(access.getDeviceId());
					if (device != null) {
						CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
						baseUrl = company.getCompanyBaseUrl();
						dataAccess.addLog("setup is working: login: " + url, logLevel);
						dataAccess.updateAuthTokenWithBrowser(device.getDeviceId(), access.getBrowserId());
						BrowserDbObj browser = dataAccess.getBrowserByAccessCode(access);
						dataAccess.addLog("retrieved browser: " + (browser != null), logLevel);
						TokenDbObj browserSession;
						jwt = new JsonWebToken()
								.buildExpiredJwt(new IdentityObjectFromServer(company, device, browser, true), url);
						TokenDbObj browserToken;
						dataAccess.addLog("jwt: " + jwt, logLevel);
						browserToken = dataAccess.addToken(device, browser.getBrowserId(),
								TokenDescription.BROWSER_TOKEN, url);
						browserSession = dataAccess.addTokenWithId(device, browser.getBrowserId(), browserSessionStr,
								TokenDescription.BROWSER_SESSION, 0, url);
						if (browser.getDescription().equals("temp")) {
							// TODO: we probably should have already rejected a temp device if pushes aren't
							// allowed
							if (company.isPushAllowed()) {
								PushNotifications pushNotification = new PushNotifications();
								pushNotification.sendLoudPushByDevice(device, false);
								outcome = Outcomes.TEMPORARY;
							} else if (company.isTextAllowed()) {
								TextMessage text = new TextMessage();
								text.textCodeToCentral(device, dataAccess);
								outcome = Outcomes.TEMPORARY;
							}
						} else {
							if (device.isMultiUser()) {
								outcome = Outcomes.MULTIUSER;
							} else {
								outcome = Outcomes.SUCCESS;
							}
						}
						reason = browserSession.getTokenId();
						tokenStr = browserToken.getTokenId();
					} else {
						dataAccess.addLog("device was null: " + access.getDeviceId(), LogConstants.WARNING);
					}
				} else {
					dataAccess.addLog("access code was too old: " + seconds + " seconds", LogConstants.WARNING);
					reason = "access code was too old";
				}
			} else {
				dataAccess.addLog("access code not found", LogConstants.WARNING);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return new ApiResponseWithTokenAndJwt(outcome, reason, tokenStr, jwt);
	}

	private ApiResponseWithToken handleFailure(String accessCode, String url) {
		int outcome = Outcomes.ERROR;
		String reason = "";
		String tokenStr = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("called with accessCode: " + accessCode);
		try {
			BrowserDbObj oldBrowser = dataAccess.getBrowserByAccessCode(accessCode);

			if (oldBrowser != null) {
				dataAccess.addLog("browserFound by accessCode");
				String newBrowserTokenId = GeneralUtilities.randomString(32) + "-" + GeneralUtilities.randomString(16)
						+ "-" + GeneralUtilities.randomString(4);
				DeviceDbObj device = dataAccess.getDeviceByDeviceId(oldBrowser.getDeviceId());

				if (device != null) {
					if (device.isActive() && device.getSignedIn()) {
						TokenDbObj browserToken = dataAccess.addBrowserAndTokenWithId(device, newBrowserTokenId,
								TokenDescription.BROWSER_TOKEN, url);
						CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
						TokenDbObj browserIdToken = dataAccess.addToken(device, newBrowserTokenId,
								TokenDescription.BROWSER_SESSION, url);
						reason = browserIdToken.getTokenId();
						baseUrl = company.getCompanyBaseUrl();
						dataAccess.addLog("setup is working: login: " + baseUrl);
						tokenStr = browserToken.getTokenId();
						dataAccess.addLog("setup worked token: " + tokenStr);
						outcome = Outcomes.SUCCESS;
					} else {
						dataAccess.addLog("inactive or signed out");
					}
				} else {
					dataAccess.addLog("device not found");
				}
			} else {
				dataAccess.addLog("browser not found");
			}
		} catch (Exception e) {
			dataAccess.addLog("handleFailure", e);
		}
		return new ApiResponseWithToken(outcome, reason, tokenStr);
	}

	private ApiResponseWithToken validateSignupCheck(String accessCode) {
		String reason = "";
		int outcome = Outcomes.FAILURE;
		String tokenStr = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.addLog("with accessCode: " + accessCode);
		DeviceDbObj device = dataAccess.getDeviceByAccessCode(accessCode);
		if (device != null) {
			AccessCodeDbObj access = dataAccess.getAccessCodeFromAccessString(accessCode);
			if (access != null) {
				if (dataAccess.isProximate(device, false)) {
					access.setActive(false, "validateSignupCheck");
					dataAccess.updateAccessCode(access);
					AccessCodeDbObj newAccessCode = dataAccess.addAccessCodeWithBrowserId(device.getDeviceId(),
							access.getBrowserId(), "validateSignupCheck");
					tokenStr = newAccessCode.getAccessCode();
					outcome = Outcomes.SUCCESS;
				} else {
					reason = "not yet proximate";
				}

			}

		} else {
			reason = "device not found";
		}
		dataAccess.addLog("outcome: " + outcome);
		return new ApiResponseWithToken(outcome, reason, tokenStr);
	}
}
