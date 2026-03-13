package com.humansarehuman.blue2factor.authentication.api;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;

import org.apache.http.util.TextUtils;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.springframework.ui.ModelMap;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.authentication.failures.Failure;
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
import com.humansarehuman.blue2factor.entities.AccessAllowedWithAccessType;
import com.humansarehuman.blue2factor.entities.ConnectedAndConnectionType;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.RequestAndResponse;
import com.humansarehuman.blue2factor.entities.UrlAndModel;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.enums.UserType;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponse;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.AuthenticatorDbObj;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlAuthnRequestDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlIdentityProviderDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlServiceProviderDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.humansarehuman.blue2factor.utilities.jwt.JsonWebToken;
import com.humansarehuman.blue2factor.utilities.saml.Saml;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class B2fApi extends BaseController {
	
	protected ApiResponseWithToken expireInstance(String browserId, String url) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		if (browserId != null) {
			CompanyDataAccess dataAccess = new CompanyDataAccess();
			dataAccess.addLog("called on " + browserId);
			TokenDbObj token = dataAccess.getToken(browserId);
			if (token != null) {
				CompanyDbObj company = dataAccess.getCompanyByDevId(token.getDeviceId());
				if (company != null) {
					baseUrl = company.getCompanyBaseUrl();
					dataAccess.expireOtherBrowserTokens(token, url);
					outcome = SUCCESS;
				} else {
					reason = Constants.CO_NOT_FOUND;
				}
			} else {
				reason = Constants.BROWSER_NOT_FOUND;
			}
		}
		return new ApiResponseWithToken(outcome, reason, "");
	}
	
	private ApiResponseWithToken validateCompany(CompanyDataAccess dataAccess, TokenDbObj oldToken,
			IdentityObjectFromServer idObj, String url, String companyIdFromUrl)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		DeviceDbObj device = idObj.getDevice();
		dataAccess.addLog(device.getDeviceId(), "device is allowed");
		dataAccess.addLog("will add token with browserId: " + idObj.getBrowser().getBrowserId());
		TokenDbObj browserSession = dataAccess.addToken(device, oldToken.getBrowserId(),
				TokenDescription.BROWSER_SESSION, url);
		String newSession = browserSession.getTokenId();
		Encryption encryption = new Encryption();

		boolean wrongCompany = false;
		if (!TextUtils.isEmpty(companyIdFromUrl)) {
			dataAccess.addLog(device.getDeviceId(), "companyIdFromUrl: " + companyIdFromUrl);
			CompanyDbObj companyFromUrl = dataAccess.getCompanyByApiKey(companyIdFromUrl);
			if (companyFromUrl != null) {
				if (!companyFromUrl.getCompanyId().equals(idObj.getCompany().getCompanyId())) {
					if (dataAccess.urlMatchesCompany(idObj.getCompany(), url)) {
						dataAccess.addLog(device.getDeviceId(), "wrong company");
						wrongCompany = true;
					}
				}
			}
		}
		if (!wrongCompany) {
			dataAccess.addLog("correct company");
			token = encryption.encryptBrowserData(idObj.getBrowser(), newSession, url);
			reason = new JsonWebToken().buildJwtForJs(idObj, url);
			outcome = Outcomes.SUCCESS;
			dataAccess.addLog("successful outcome");
		} else {
			reason = Constants.COMPANY_USER_MISMATCH;
		}
		return new ApiResponseWithToken(outcome, reason, token);
	}
	
	private AccessAllowedWithAccessType addNewToken(CompanyDataAccess dataAccess, AccessAllowedWithAccessType accessAllowed, 
			DeviceDbObj device, IdentityObjectFromServer idObj, TokenDbObj oldToken, String url) {
		if (oldToken.getDeviceId().equals(device.getDeviceId())) {
			TokenDbObj browserSession = dataAccess.addToken(device, oldToken.getBrowserId(),
					TokenDescription.BROWSER_SESSION, url);
			String newSession = browserSession.getTokenId();
			Encryption encryption = new Encryption();
			try {
				String token = encryption.encryptBrowserData(idObj.getBrowser(), newSession, url);
				String reason = new JsonWebToken().buildJwtForJs(idObj, url);
				accessAllowed.setToken(token);
				accessAllowed.setReason(reason);
			} catch (Exception e) {
				dataAccess.addLog(e);
			} 
			
		}
		return accessAllowed;
	}

	protected ApiResponseWithToken validateAccess(String session, String url, String companyIdFromUrl) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		String reason = "";
		int outcome = Outcomes.FAILURE;
		TokenDbObj oldToken = null;
		String token = "";
		try {
			if (!TextUtils.isBlank(session)) {
				dataAccess.addLog("instanceId: " + session);
				DeviceDbObj device = null;
				oldToken = dataAccess.getTokenByDescriptionAndTokenId(TokenDescription.BROWSER_SESSION, session);
				device = dataAccess.getDeviceByDeviceId(oldToken.getDeviceId(), "validateAccess");
				if (device != null && device.isActive()) {
					if (device.getSignedIn()) {
						dataAccess.addLog("device is signed in");
						CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
						BrowserDbObj browser = dataAccess.getBrowserByToken(session, TokenDescription.BROWSER_SESSION);
						if (company != null) {
							if (browser != null) {
								if (!browser.isExpired()) {
									dataAccess.addLog("browser looks good");
									IdentityObjectFromServer idObj = new IdentityObjectFromServer(browser, true);
									baseUrl = company.getCompanyBaseUrl();
									if (dataAccess.isAccessAllowed(device, "validateAccess")) {
										ApiResponseWithToken resp = validateCompany(dataAccess, oldToken, idObj,
												url, companyIdFromUrl);
										outcome = resp.getOutcome();
										reason = resp.getReason();
										token = resp.getToken();
									} else {
										dataAccess.addLog("access is not allowed");
										reason = Constants.NOT_PERMITTED;
										token = new JsonWebToken().buildExpiredJwt(idObj, url);
									}
								} else {
									reason = Constants.BROWSER_EXPIRED;
								}
							} else {
								reason = Constants.BROWSER_NOT_FOUND;
							}
						} else {
							reason = "bad company ('til the day I die)";
						}
					} else {
						reason = Constants.SIGNED_OUT;
					}
				} else {
					reason = Constants.DEVICE_NOT_FOUND;
				}
			} else {
				reason = Constants.BROWSER_NOT_FOUND;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
			outcome = ERROR;
		}
		dataAccess.addLog("tokenStr: " + token + ", reason: " + reason);
		return new ApiResponseWithToken(outcome, reason, token);
	}
	
	protected AccessAllowedWithAccessType validateAccessFromApi(String session, String url) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		TokenDbObj oldToken = null;
		AccessAllowedWithAccessType response = new AccessAllowedWithAccessType(false, ConnectionType.NONE, 
				DateTimeUtilities.getCurrentTimestamp());
		try {
			if (!TextUtils.isBlank(session)) {
				dataAccess.addLog("instanceId: " + session);
				DeviceDbObj device = null;
				oldToken = dataAccess.getTokenByDescriptionAndTokenId(TokenDescription.BROWSER_SESSION, session);
				device = dataAccess.getDeviceByDeviceId(oldToken.getDeviceId(), "validateAccess");
				if (device != null && device.isActive()) {
					if (device.getSignedIn()) {
						dataAccess.addLog("device is signed in");
						CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
						BrowserDbObj browser = dataAccess.getBrowserByToken(session, TokenDescription.BROWSER_SESSION);
						if (company != null) {
							if (browser != null) {
								if (!browser.isExpired()) {
									dataAccess.addLog("browser looks good");
									IdentityObjectFromServer idObj = new IdentityObjectFromServer(browser, true);
									baseUrl = company.getCompanyBaseUrl();
									response = dataAccess.isAccessAllowedWithConnectionMethod(device, 
											"validateAccessFromApi", false);
									if (response.isAccessAllowed()) {
										if (!dataAccess.urlMatchesCompany(company, url)) {
											response.setAccessAllowed(false);
											response.setReason(Constants.COMPANY_URL_MISMATCH);
										} else {
											response = addNewToken(dataAccess, response, device, idObj, oldToken, url);
										}
									} else {
										dataAccess.addLog("access is not allowed");
										response.setReason(Constants.NOT_PERMITTED);
										response.setToken(new JsonWebToken().buildExpiredJwt(idObj, url));
									}
								} else {
									response.setReason(Constants.BROWSER_EXPIRED);
								}
							} else {
								response.setReason(Constants.BROWSER_NOT_FOUND);
							}
						} else {
							response.setReason("bad company ('til the day I die)");
						}
					} else {
						response.setReason(Constants.SIGNED_OUT);
					}
				} else {
					response.setReason(Constants.DEVICE_NOT_FOUND);
				}
			} else {
				response.setReason(Constants.BROWSER_NOT_FOUND);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			response.setReason(e.getLocalizedMessage());
			response.setAccessAllowed(false);
		}
		if (!TextUtils.isBlank(response.getReason())) {
			dataAccess.addLog(response.getReason(), LogConstants.WARNING);
		}
		return response;
	}
	
	public ApiResponseWithToken checkSecondFactorFromServer(DeviceDataAccess dataAccess, Encryption encryption,
			String sessionFromServer, String encryptedSession, String deviceId, String userAgent, String reqUrl,
			boolean fromBrowser) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId, "checkSecondFactorFromServer");
		if (device != null) {
			if (device.isActive() && device.getSignedIn()) {
				if (!dataAccess.hasSessionBeenUsed(device, sessionFromServer)) {
					if (encryption.verifySignatureWithForegroundKey(device, sessionFromServer, encryptedSession)) {
						if (dataAccess.isAccessAllowed(device, "checkSecondFactorFromServer")) {
							outcome = Outcomes.SUCCESS;
						}
					} else {
						reason = Constants.DEVICE_NOT_LOCAL;
					}
				} else {
					reason = Constants.EXPIRED_TOKEN;
				}
			} else {
				reason = Constants.SIGNED_OUT;
			}
		} else {
			reason = Constants.DEVICE_NOT_FOUND;
		}
		return new ApiResponseWithToken(outcome, reason, "");
	}
	
	public AccessAllowedWithAccessType checkAccessAllowedFromApi(DeviceDataAccess dataAccess, Encryption encryption,
			String token, String session, String reqUrl) {
		AccessAllowedWithAccessType response = new AccessAllowedWithAccessType(false, ConnectionType.NONE, 
				DateTimeUtilities.getCurrentTimestamp());
		try {
			if (session == null) {
				dataAccess.addLog("session NOT found", LogConstants.WARNING);
				response.setReason(Constants.DECRYPTION_ERROR);
			} else if (session == Constants.KEY_NOT_FOUND) {
				dataAccess.addLog("session NOT found - no key", LogConstants.WARNING);
				response.setReason(Constants.KEY_NOT_FOUND);
			} else {
				dataAccess.addLog("session found");
				response = validateAccessFromApi(session, reqUrl);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return response;

	}
	
	public ApiResponseWithToken checkSecondFactorFromApi(DeviceDataAccess dataAccess, Encryption encryption,
			String token, String encryptedSession, String userAgent, String reqUrl, String companyId) {
		ApiResponseWithToken response;
		String session = encryption.decryptBasedOnBrowserOrServerId(token, encryptedSession, reqUrl);
		if (session == null) {
			dataAccess.addLog("session NOT found", LogConstants.WARNING);
			DeviceDbObj device = dataAccess.getDeviceByBrowserToken(token);
			if (device != null) {
				response = new ApiResponseWithToken(Outcomes.API_F2_FAILURE, Constants.DECRYPTION_ERROR, "");
			} else {
				response = new ApiResponseWithToken(Outcomes.FAILURE, Constants.DEV_NOT_FOUND, "");
			}
		} else if (session == Constants.KEY_NOT_FOUND) {
			dataAccess.addLog("session NOT found - no key", LogConstants.WARNING);
			response = new ApiResponseWithToken(Outcomes.FAILURE, session, "");
		} else {
			dataAccess.addLog("session found");
			response = validateAccess(session, reqUrl, companyId);
		}
		return response;

	}

	/*
	 * right now this is for test users from google and apple. They need to be able
	 * to test the sign in without actually having their companies sign up. If this
	 * list gets bigger we may want to make it a table
	 */
	protected GroupDbObj addUserToGroupIfNeeded(String email) {
		email = email.toLowerCase().trim();
		GroupDbObj group = null;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		try {
			if (dataAccess.isTestUser(email, true)) {
				dataAccess.addLog("test user " + email + " is signing up", LogConstants.WARNING);
				String[] nameSplit = email.split("@");
				if (nameSplit.length == 2) {
					String name = nameSplit[0];
					String nameWithCap = name.substring(0, 1).toUpperCase() + name.substring(1);
					dataAccess.addLog("name: " + nameWithCap);
					GroupDbObj defaultGroup = dataAccess.getGroupByEmail("default@blue2factor.com");
					Timestamp now = DateTimeUtilities.getCurrentTimestamp();
					group = new GroupDbObj(defaultGroup.getCompanyId(), GeneralUtilities.randomString(), email, 1, true,
							now, defaultGroup.getTimeoutSecs(), null, null, 0, 0, 0, nameWithCap, now, name,
							UserType.USER, false, true, false);
					dataAccess.addGroup(group);
				} else {
					dataAccess.addLog("test email failure");
				}
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return group;
	}

	protected String doesUrlMatchRegex(CompanyDbObj company, String siteUrl, CompanyDataAccess dataAccess) {
		String entityId = null;
		if (dataAccess.urlMatchesCompany(company, siteUrl)) {
			entityId = Urls.SECURE_URL + Urls.SAML_ENTITY_ID.replace("{apiKey}", company.getApiKey());
		}
		return entityId;
	}

	protected IdentityObjectFromServer getIdObj(String authToken, CompanyDataAccess dataAccess) {
		IdentityObjectFromServer idObj = null;
		try {
			if (!TextUtils.isEmpty(authToken)) {
				TokenDbObj token = dataAccess.getTokenByOrDescriptionAndTokenId(TokenDescription.AUTHENTICATION,
						TokenDescription.ADMIN, authToken);
				if (token != null) {
					CompanyDbObj company = null;
					dataAccess.addLog("getIdObj", "token found");
					DeviceDbObj device = dataAccess.getDeviceByDeviceId(token.getDeviceId(), "getIdObj");
					BrowserDbObj browser = dataAccess.getBrowserById(token.getBrowserId());
					if (browser != null) {
						dataAccess.addLog("getIdObj", "browser found");
						if (device == null) {
							dataAccess.getDeviceByBrowserId(browser.getBrowserId());
						}
					}
					if (device != null) {
						company = dataAccess.getCompanyByDevId(device.getDeviceId());
						if (company != null && company.isActive()) {
							dataAccess.addLog("getIdObj", "company found");
						}
					} else {
						company = dataAccess.getCompanyByGroupId(token.getGroupId());
					}
					idObj = new IdentityObjectFromServer(company, device, browser, true);
				}
			} else {
				dataAccess.addLog("getIdObj", "token was empty");
			}
		} catch (Exception e) {
			dataAccess.addLog("getIdObj", e);
		}
		return idObj;
	}

	public UrlAndModel checkForPushOrBiometrics(ModelMap model, IdentityObjectFromServer idObj,
			CompanyDataAccess dataAccess) {

		String nextPage;
		UrlAndModel response;
		DeviceDbObj device = idObj.getDevice();
		if (deviceHasBiometrics(idObj, dataAccess) && !device.isMultiUser()) {
			dataAccess.addLog("biometrics: true");
			model.addAttribute("biometrics", true);
			nextPage = "jsVerify";
			response = new UrlAndModel(nextPage, model);
		} else {
			ConnectedAndConnectionType cct = dataAccess.didGiveAccess(device, false);
			if (cct.isConnected()) {
				CompanyDbObj company = idObj.getCompany();
				BrowserDbObj browser = idObj.getBrowser();
				if (browser == null) {
					browser = dataAccess.addBrowser(device, "checkForPushOrBiometrics");
					dataAccess.addLog("this is unexpected and should be looked into", LogConstants.WARNING);
				}
				dataAccess.addLog(device.getDeviceId(), "we provided access. send to setupJavascript");
				model.addAttribute("submitUrl", company.getCompleteCompanyLoginUrl());
				model.addAttribute("central", device.isCentral());
				model.addAttribute("bleEnabled", device.getHasBle());
				nextPage = "setupJavascript";
				response = new UrlAndModel(nextPage, model);
			} else {
				UrlAndModel urlAndModel = sendPushOrTextIfPossible(model, idObj, dataAccess);
				response = new UrlAndModel(urlAndModel.getUrl(), urlAndModel.getModelMap());
			}
		}
		return response;
	}

	public UrlAndModel sendPushOrTextIfPossible(ModelMap model, IdentityObjectFromServer idObj,
			CompanyDataAccess dataAccess) {
		String nextPage;
		DeviceDbObj peripheralDevice = idObj.getDevice();
		if (!peripheralDevice.isCentral()) {
			UrlAndModel urlAndModel = sendPushOrText(model, idObj, dataAccess);
			nextPage = urlAndModel.getUrl();
			model = urlAndModel.getModelMap();
			dataAccess.addLog("biometrics: false");
		} else {
			dataAccess.addLog("device is central");
			model.addAttribute("outOfRange", true);
			nextPage = "jsVerify";
		}
		return new UrlAndModel(nextPage, model);
	}

	private UrlAndModel sendPushOrText(ModelMap model, IdentityObjectFromServer idObj, CompanyDataAccess dataAccess) {
		DeviceDbObj peripheralDevice = idObj.getDevice();
		DeviceConnectionDbObj connection = dataAccess.getConnectionForPeripheral(peripheralDevice, true);
		String nextPage;
		if (connection != null) {
			dataAccess.addLog("connection found");
			DeviceDbObj centralDevice = dataAccess.getDeviceByDeviceId(connection.getCentralDeviceId(),
					"sendPushOrText");
			if (dataAccess.isCentralDumbphone(centralDevice)) {
				if (idObj.getGroup().isTextAllowed()) {
					model.addAttribute("outOfRange", false);
					dataAccess.addLog("we are allowed to text");
					TextMessage textMessage = new TextMessage();
					if (textMessage.textCode(idObj.getCompany(), centralDevice, connection)) {
						model.addAttribute("showForm", true);
						model.addAttribute("textSent", true);
						nextPage = "enterTextCode";
					} else {
						nextPage = "error";
						model.addAttribute("errorFound", true);
						model.addAttribute("We tried to text you a code, but it did not work.");
					}
				} else {
					dataAccess.addLog(
							"I don't think there would every be a reason that a dumbphone would be in our DB if pushes aren't allowed",
							LogConstants.WARNING);
					model.addAttribute("outOfRange", false);
					model.addAttribute("dumbphoneNotEnabled", true);
					nextPage = "jsVerify";
				}
			} else if (idObj.getGroup().isPushAllowed()) {
				dataAccess.addLog("will push");
				model = this.sendFailurePush(model, peripheralDevice);
				model.addAttribute("pushSent", true);
				nextPage = "jsVerify";
			} else {
				dataAccess.addLog("connection found", LogConstants.WARNING);
//				model.addAttribute("outOfRange", true);
				nextPage = "jsVerify";
			}
		} else {
			dataAccess.addLog("connection not found", LogConstants.WARNING);
//			model.addAttribute("outOfRange", true);
			nextPage = "jsVerify";
		}
		return new UrlAndModel(nextPage, model);
	}

	protected ModelMap sendFailurePush(ModelMap model, DeviceDbObj device) {
		PushNotifications push = new PushNotifications();
		ApiResponse resp = push.sendLoudPushByDeviceOnce(device, false);
		if (resp.outcome == Outcomes.SUCCESS) {
			model.addAttribute("pushSent", true);
		} else {
			model.addAttribute("outOfRange", true);
		}
		return model;
	}

	public boolean deviceHasBiometrics(IdentityObjectFromServer idObj, CompanyDataAccess dataAccess) {
		boolean hasBiometrics = false;
		boolean biometricsNotAllowed = false;
		if (idObj.getDevice() != null) {
			if (idObj.getDevice().isMultiUser()) {
				biometricsNotAllowed = true;
			}
		}
		if (!biometricsNotAllowed) {
			BrowserDbObj browser = idObj.getBrowser();
			if (browser != null && !TextUtils.isEmpty(browser.getBrowserId())) {
				AuthenticatorDbObj authenticator = dataAccess.getAuthenticatorByBrowserIdAndUrl(browser.getBrowserId(),
						Urls.URL_WITHOUT_PROTOCOL);
				if (authenticator != null) {
					hasBiometrics = !authenticator.isExpired();
				}
			}
		}
		return hasBiometrics;
	}

	protected UrlAndModel reSetup(ModelMap model, IdentityObjectFromServer idObj, CompanyDataAccess dataAccess) {
		String browserId = idObj.getBrowser().getBrowserId();
		TokenDbObj session = dataAccess.addToken(idObj.getDevice(), browserId, TokenDescription.BROWSER_SESSION,
				baseUrl);
		TokenDbObj token = dataAccess.addToken(idObj.getDevice(), browserId, TokenDescription.BROWSER_TOKEN, baseUrl);
		String cookieString = session.getTokenId() + "**" + token.getTokenId();
		model.addAttribute("b2fSetup", cookieString);
		model.addAttribute("central", idObj.getDevice().isCentral());
		dataAccess.addLog("setting b2fSetup to " + cookieString);
		String jwt = new JsonWebToken().buildExpiredJwt(
				new IdentityObjectFromServer(idObj.getCompany(), idObj.getDevice(), idObj.getBrowser(), false),
				idObj.getCompany().getCompleteCompanyLoginUrl());
		dataAccess.addLog("jwt: " + jwt);
		model.addAttribute("jwt", jwt);
		model.addAttribute("submitUrl", idObj.getCompany().getCompleteCompanyLoginUrl());
		String nextPage = "resetJwt";
		return new UrlAndModel(nextPage, model);
	}

	/**
	 * should only be
	 * 
	 * @param idObj
	 * @param browserSession
	 * @param baseUrl
	 * @return
	 */
	protected int handleF2ServerValidationRequest(IdentityObjectFromServer idObj, String browserSession,
			String baseUrl) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("checking factor 2");
		ApiResponseWithToken response = null;
		dataAccess.addLog("f2Token was not blank");
		ApiResponse apiResponse = validateSecondFactor(idObj, browserSession, idObj.getCompany(), baseUrl);
		int outcome = apiResponse.getOutcome();
		if (outcome == Outcomes.SUCCESS) {
			try {
				response = new ApiResponseWithToken(apiResponse, "");
			} catch (Exception e) {
				dataAccess.addLog(e);
			}
			dataAccess.addLog("success: " + response.getOutcome());
			// tell the Client's server to load the requested page
		} else {
			// Either send a push
			// or go back to client server and tell it to try web authn Or we just fail
			dataAccess.addLog("bad outcome: " + apiResponse.getOutcome());
			dataAccess.addLog("adding access code for browser");
			if (!idObj.isFromJs()) {
				outcome = this.handleNotProximate(idObj);
			} else {
				dataAccess.addLog("not pushing because we came from Js");
			}

		}
		return outcome;

	}

	protected boolean isPushOrTextAllowed(CompanyDbObj company) {
		return (company.isPushAllowed() || company.isTextAllowed());
	}

	protected boolean isPushOrTextAllowed(GroupDbObj group) {
		boolean success = false;
		if (group != null) {
			success = (group.isPushAllowed() || group.isTextAllowed());
		}
		return success;
	}

	public void redirectSamlRequestToIdp(HttpServletResponse httpResponse, AuthnRequest samlRequest,
			IdentityObjectFromServer idObj, SamlServiceProviderDbObj serviceProvider, String incomingRelayState,
			CompanyDbObj company, String ipAddress, String requestString, SamlDataAccess dataAccess) throws Exception {
		SamlIdentityProviderDbObj idp = dataAccess.getSamlIdpFromCompany(company);
		if (idp != null) {
			String outgoingAuthId = GeneralUtilities.randomString();
			dataAccess.addLog("SAML");
			Saml saml = new Saml();
			AuthnRequest outgoingAuthnRequest = saml.buildAuthnRequest(idp, company, outgoingAuthId, true);
			SamlAuthnRequestDbObj samlAuthnRequest = saml.buildAndSaveAuthnRequestObj(idObj, company, idp,
					serviceProvider, samlRequest, outgoingAuthnRequest, incomingRelayState, ipAddress, requestString,
					dataAccess);
			redirectSamlRequestToIdp(httpResponse, outgoingAuthnRequest, samlAuthnRequest.getOutgoingRelayState(),
					idp.getRedirectUrl());
		} else {
			dataAccess.addLog("The company didn't have IDP. How is this possible?", LogConstants.ERROR);
		}
	}

	public void redirectSamlRequestToIdp(HttpServletResponse httpResponse, RequestAbstractType samlRequest,
			String relayState, String redirectLoc) throws Exception {
		DataAccess dataAccess = new DataAccess();
		this.redirectSamlRequestToIdp(httpResponse, samlRequest, relayState, redirectLoc, dataAccess);
	}

	public void redirectSamlResponseToSp(HttpServletResponse httpResponse, Response samlResponse, String relayState,
			String redirectLoc, DataAccess dataAccess) throws Exception {
		dataAccess.addLog("start");
		MessageContext messageContext = new MessageContext();
		messageContext.setMessage(samlResponse);
		Saml.initializeSaml();
		XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
		@SuppressWarnings("unchecked")
		SAMLObjectBuilder<Endpoint> endpointBuilder = (SAMLObjectBuilder<Endpoint>) builderFactory
				.getBuilder(AssertionConsumerService.DEFAULT_ELEMENT_NAME);

		Endpoint samlEndpoint = endpointBuilder.buildObject();
		samlEndpoint.setLocation(redirectLoc);
		if (relayState != null) {
			SAMLBindingSupport.setRelayState(messageContext, relayState);
		}
		SAMLPeerEntityContext peerEntityContext = messageContext.ensureSubcontext(SAMLPeerEntityContext.class);
		SAMLEndpointContext endpointContext = peerEntityContext.ensureSubcontext(SAMLEndpointContext.class);
		endpointContext.setEndpoint(samlEndpoint);

		HTTPRedirectDeflateEncoder httpRedirectDeflateEncoder = new HTTPRedirectDeflateEncoder();
		httpRedirectDeflateEncoder.setMessageContext(messageContext);

		dataAccess.addLog("sending to: " + redirectLoc + " with relayState: " + relayState);

		httpResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		httpRedirectDeflateEncoder.setHttpServletResponseSupplier(() -> httpResponse);
		httpRedirectDeflateEncoder.initialize();
		httpRedirectDeflateEncoder.encode();
		dataAccess.addLog("end");
	}

	@SuppressWarnings("unchecked")
	public void redirectSamlRequestToIdp(HttpServletResponse httpResponse, RequestAbstractType samlRequest,
			String relayState, String redirectLoc, DataAccess dataAccess) throws Exception {
		// here we want to check B2F
		dataAccess.addLog("start");
		MessageContext messageContext = new MessageContext();
		messageContext.setMessage(samlRequest);
		Saml.initializeSaml();
		XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
		SAMLObjectBuilder<Endpoint> endpointBuilder = (SAMLObjectBuilder<Endpoint>) builderFactory
				.getBuilder(AssertionConsumerService.DEFAULT_ELEMENT_NAME);

		Endpoint samlEndpoint = endpointBuilder.buildObject();
		samlEndpoint.setLocation(redirectLoc);
		if (relayState != null) {
			SAMLBindingSupport.setRelayState(messageContext, relayState);
		}
		SAMLPeerEntityContext peerEntityContext = messageContext.ensureSubcontext(SAMLPeerEntityContext.class);
		SAMLEndpointContext endpointContext = peerEntityContext.ensureSubcontext(SAMLEndpointContext.class);
		endpointContext.setEndpoint(samlEndpoint);

		HTTPRedirectDeflateEncoder httpRedirectDeflateEncoder = new HTTPRedirectDeflateEncoder();
		httpRedirectDeflateEncoder.setMessageContext(messageContext);

		dataAccess.addLog("sending to: " + redirectLoc + " with relayState: " + relayState + " and issuer: "
				+ samlRequest.getIssuer(), LogConstants.TEMPORARILY_IMPORTANT);

		httpResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		httpRedirectDeflateEncoder.setHttpServletResponseSupplier(() -> httpResponse);
		httpRedirectDeflateEncoder.initialize();
		httpRedirectDeflateEncoder.encode();
		dataAccess.addLog("end");
	}

	@SuppressWarnings("unchecked")
	public void redirectSamlRequest(HttpServletResponse httpResponse, LogoutRequest samlRequest, String relayState,
			String redirectLoc) throws Exception {
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("redirectRequest", "start");
		MessageContext messageContext = new MessageContext();
		// this is the message that came in, which is not what we want
		messageContext.setMessage(samlRequest);
		Saml.initializeSaml();
		XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
		SAMLObjectBuilder<Endpoint> endpointBuilder = (SAMLObjectBuilder<Endpoint>) builderFactory
				.getBuilder(AssertionConsumerService.DEFAULT_ELEMENT_NAME);

		Endpoint samlEndpoint = endpointBuilder.buildObject();
		samlEndpoint.setLocation(redirectLoc);
		if (relayState != null) {
			SAMLBindingSupport.setRelayState(messageContext, relayState);
		}
		SAMLPeerEntityContext peerEntityContext = messageContext.ensureSubcontext(SAMLPeerEntityContext.class);
		SAMLEndpointContext endpointContext = peerEntityContext.ensureSubcontext(SAMLEndpointContext.class);
		endpointContext.setEndpoint(samlEndpoint);

		HTTPRedirectDeflateEncoder httpRedirectDeflateEncoder = new HTTPRedirectDeflateEncoder();
		httpRedirectDeflateEncoder.setMessageContext(messageContext);

		dataAccess.addLog("redirectRequest", "sending to: " + redirectLoc);

		httpResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		httpRedirectDeflateEncoder.setHttpServletResponseSupplier(() -> httpResponse);
		httpRedirectDeflateEncoder.initialize();
		httpRedirectDeflateEncoder.encode();
		dataAccess.addLog("redirectRequest", "end");
	}

	public HttpServletResponse redirectRequest(HttpServletResponse httpResponse, String url) throws IOException {
		httpResponse.sendRedirect(url);
		return httpResponse;
	}

	protected ApiResponse validateSecondFactor(IdentityObjectFromServer idObj, String browserSession,
			CompanyDbObj company, String url) {
		ApiResponseWithToken apiResponse = new ApiResponseWithToken(Outcomes.API_F2_FAILURE, "", "");
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		if (!TextUtils.isBlank(browserSession)) {
			dataAccess.addLog("validateSecondFactor", "b2fToken exists");
			apiResponse = getAccessAllowedByDeviceAndCompany(idObj, url);
		} else {
			dataAccess.addLog("validateSecondFactor", "browserSession is blank");
			dataAccess.addLog("validateSecondFactor", "b2fToken exists");
			apiResponse = getAccessAllowedByDeviceAndCompany(idObj, url);
		}
		if (apiResponse.getOutcome() != Outcomes.SUCCESS && apiResponse.getOutcome() != Outcomes.API_F2_FAILURE) {
			apiResponse.setOutcome(Outcomes.API_F2_FAILURE);
		}

		return apiResponse;
	}

	protected ApiResponseWithToken checkForNewSignup(IdentityObjectFromServer idObj, String browserSession,
			String url) {
		String reason = "";
		int outcome = Outcomes.FAILURE;
		String token = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		DeviceDbObj device = dataAccess.getDeviceByToken(browserSession);
		if (device != null) {
			double seconds = DateTimeUtilities.timeDifferenceInSecondsFromNow(device.getLastReset());
			if (seconds < 600) {
				outcome = Outcomes.SUCCESS;
				TokenDbObj f1Token = dataAccess.addToken(device, idObj.getBrowser().getBrowserId(),
						TokenDescription.F1_SERVER, url);
				TokenDbObj f2Token = dataAccess.addToken(device, idObj.getBrowser().getBrowserId(),
						TokenDescription.F2_SERVER, url);
				token = f1Token.getTokenId() + ":" + f2Token.getTokenId();
				dataAccess.addLog(device.getDeviceId(), "success b/c " + seconds + " seconds ago.");
			} else {
				dataAccess.addLog(device.getDeviceId(), "failed lastSignup: " + seconds + " seconds ago.");
			}
		} else {
			dataAccess.addLog("device not found");
		}
		return new ApiResponseWithToken(outcome, reason, token);
	}

	protected ApiResponseWithToken checkForNewSignup(String browserSession, String url) {
		String reason = "";
		int outcome = Outcomes.FAILURE;
		String token = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		BrowserDbObj browser = dataAccess.getBrowserByToken(browserSession, TokenDescription.BROWSER_SESSION);
		if (browser != null) {
			DeviceDbObj device = dataAccess.getDeviceByToken(browserSession);
			if (device != null) {
				double seconds = DateTimeUtilities.timeDifferenceInSecondsFromNow(device.getLastReset());
				if (seconds < 600) {
					outcome = Outcomes.SUCCESS;
					TokenDbObj f1Token = dataAccess.addToken(device, browser.getBrowserId(), TokenDescription.F1_SERVER,
							url);
					TokenDbObj f2Token = dataAccess.addToken(device, browser.getBrowserId(), TokenDescription.F2_SERVER,
							url);
					token = f1Token.getTokenId() + ":" + f2Token.getTokenId();
					// device.setLastReset(DateTimeUtilities.getCurrentTimestampMinusSeconds(600));
					// dataAccess.updateDevice(device, "checkForNewSignup");
					dataAccess.addLog(device.getDeviceId(), "success b/c " + seconds + " seconds ago.");
				} else {
					reason = Constants.EXPIRED_TOKEN + "-" + seconds + "s";
				}
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
			}
		} else {
			reason = Constants.BROWSER_NOT_FOUND;
		}
		if (!TextUtils.isBlank(reason)) {
			dataAccess.addLog("checkForNewSignup", reason, LogConstants.WARNING);
		}
		return new ApiResponseWithToken(outcome, reason, token);
	}

	protected ApiResponseWithToken getAccessAllowedByTokenAndCompany(String b2fToken, CompanyDbObj company,
			String url) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		DeviceDbObj device = dataAccess.getDeviceByToken(b2fToken);
		ApiResponseWithToken apiResponse;
		if (device != null) {
			GroupDbObj group = dataAccess.getGroupById(device.getGroupId());
			if (group.getCompanyId().equals(company.getCompanyId())) {
				dataAccess.addLog(device.getDeviceId(), "group id looks good");
				apiResponse = getDeviceAccessAllowed(device, url);
			} else {
				dataAccess.addLog(device.getDeviceId(), "bad group id for company");
				apiResponse = new ApiResponseWithToken(Outcomes.FAILURE, Constants.COMPANY_USER_MISMATCH, "");
			}
		} else {
			dataAccess.addLog("device not found for token: " + b2fToken);
			apiResponse = new ApiResponseWithToken(Outcomes.FAILURE, Constants.DEV_NOT_FOUND, "");
		}
		return apiResponse;
	}

	protected ApiResponseWithToken getAccessAllowedByDeviceAndCompany(IdentityObjectFromServer idObj, String url) {
		DataAccess dataAccess = new DataAccess();
		ApiResponseWithToken apiResponse = new ApiResponseWithToken(Outcomes.FAILURE, "", "");
		if (idObj.getDevice() != null) {
			CompanyDataAccess cda = new CompanyDataAccess();
			GroupDbObj group = cda.getGroupById(idObj.getDevice().getGroupId());
			CompanyDbObj company = idObj.getCompany();
			if (group.getCompanyId().equals(company.getCompanyId())) {
				dataAccess.addLog(idObj.getDevice().getDeviceId(), "group id looks good");
				apiResponse = getDeviceAccessAllowed(idObj, url);
			} else {
				dataAccess.addLog(idObj.getDevice().getDeviceId(), "bad group id for company");
				apiResponse = new ApiResponseWithToken(Outcomes.FAILURE, Constants.COMPANY_USER_MISMATCH, "");
			}
		} else {
			dataAccess.addLog("device not found for device: ");
			apiResponse = new ApiResponseWithToken(Outcomes.FAILURE, Constants.DEV_NOT_FOUND, "");
		}
		return apiResponse;
	}

	private ApiResponseWithToken getDeviceAccessAllowed(IdentityObjectFromServer idObj, String url) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		int response = Outcomes.FAILURE;
		String reason = "";
		String tokenStr = "";
		if (idObj.getDevice() != null) {
			if (dataAccess.isAccessAllowed(idObj.getDevice(), "getDeviceAccessAllowed")) {
				response = Outcomes.SUCCESS;
			} else {
				reason = "access not given";
			}
		} else {
			response = Outcomes.ERROR;
			reason = Constants.DEV_NOT_FOUND;
		}
		if (response == Outcomes.SUCCESS) {
			TokenDbObj f1Token = dataAccess.addToken(idObj.getDevice(), idObj.getBrowser().getBrowserId(),
					TokenDescription.F1_SERVER, url);
			TokenDbObj f2Token = dataAccess.addToken(idObj.getDevice(), idObj.getBrowser().getBrowserId(),
					TokenDescription.F2_SERVER, url);
			dataAccess.addLog("added token: " + f1Token.getTokenId());
			tokenStr = f1Token.getTokenId() + ":" + f2Token.getTokenId();
		}
		return new ApiResponseWithToken(response, reason, tokenStr);
	}

	private ApiResponseWithToken getDeviceAccessAllowed(DeviceDbObj device, String url) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		int response = Outcomes.FAILURE;
		String reason = "";
		String tokenStr = "";
		if (device != null) {
			if (dataAccess.isAccessAllowed(device, "getDeviceAccessAllowed")) {
				response = Outcomes.SUCCESS;
			} else {
				reason = "access not given";
			}
		} else {
			response = Outcomes.ERROR;
			reason = Constants.DEV_NOT_FOUND;
		}
		if (response == Outcomes.SUCCESS) {
			TokenDbObj token = dataAccess.addToken(device, TokenDescription.F1_SERVER, url);
			dataAccess.addLog("added token: " + token.getTokenId());
			tokenStr = token.getTokenId();
		}
		return new ApiResponseWithToken(response, reason, tokenStr);
	}

	protected RequestAndResponse setPersistantToken(HttpServletRequest request, HttpServletResponse response,
			SamlDataAccess dataAccess, IdentityObjectFromServer idObj) {
		Timestamp fourYearsFromNow = DateTimeUtilities.getCurrentTimestampPlusDays(1461);
		String browserId = null;
		if (idObj.getBrowser() != null) {
			browserId = idObj.getBrowser().getBrowserId();
		}
		String newTokenStr = GeneralUtilities.randomString();
		dataAccess.addTokenWithId(idObj.getDevice().getGroupId(), idObj.getDevice().getDeviceId(), browserId,
				newTokenStr, TokenDescription.AUTHENTICATION, 0, idObj.getCompany().getCompanyCompletionUrl(),
				fourYearsFromNow);
		dataAccess.addLog("setting B2F_AUTH session to " + newTokenStr + " for "
				+ idObj.getDevice().getOperatingSystem() + " with deviceId: " + idObj.getDevice().getDeviceId());
		request = this.setSession(request, "B2F_AUTH", newTokenStr);
		response = this.setCookie(response, newTokenStr, "B2F_AUTH", 60 * 60 * 24 * 1461, false, dataAccess, "None");
		return new RequestAndResponse(request, response);
	}

	protected String handleSignupSecondFactor(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model, IdentityObjectFromServer idObj, SamlDataAccess dataAccess) {
		String next = "failure";
		Failure failure = new Failure();
		try {
			if (dataAccess.isAccessAllowed(idObj.getDevice(), "signinProcessGet")) {
				dataAccess.addLog("signed in and access allowed");
				// go to log in page or caller page
				this.createAccessCodeWithIdObj(idObj);
				dataAccess.addLog("setCookie");
				RequestAndResponse reqRes = setPersistantToken(request, httpResponse, dataAccess, idObj);
				dataAccess.addLog("persistent token set");
				if (reqRes != null) {
					request = reqRes.getRequest();
					httpResponse = reqRes.getResponse();
				}
				String redirectUrl = failure.getReferrer(request, idObj.getCompany(), dataAccess);
				dataAccess.addLog("redirecting to: " + redirectUrl);
				httpResponse.setHeader("Location", redirectUrl);
				httpResponse.setStatus(302);
				next = null;
			} else {
				dataAccess.addLog("first factor worked, but not the second");
				UrlAndModel urlAndModel = failure.checkForPushOrBiometrics(model, idObj, dataAccess);
				next = urlAndModel.getUrl();
				model = urlAndModel.getModelMap();
				model.addAttribute("fromIdp", false);
			}
		} catch (Exception e) {

		}
		return next;
	}

	protected RequestAndResponse setTemporaryPersistantToken(HttpServletRequest request, HttpServletResponse response,
			SamlDataAccess dataAccess, IdentityObjectFromServer idObj, String newTokenStr) {
		Timestamp fiveHoursFromNow = DateTimeUtilities.getCurrentTimestampPlusSeconds(60 * 60 * 5);
		String browserId = null;
		if (idObj.getBrowser() != null) {
			browserId = idObj.getBrowser().getBrowserId();
		}
		dataAccess.addLog("adding auth token with exp: " + fiveHoursFromNow);
		dataAccess.addLog("groupId: " + idObj.getGroup().getGroupId());
		dataAccess.addTokenWithId(idObj.getGroup().getGroupId(), "", browserId, newTokenStr,
				TokenDescription.AUTHENTICATION, 0, idObj.getCompany().getCompanyCompletionUrl(), fiveHoursFromNow);
		dataAccess.addLog("setting B2F_AUTH session to " + newTokenStr);
		request = this.setSession(request, "B2F_AUTH", newTokenStr);
		response = this.setCookie(response, newTokenStr, "B2F_AUTH", 60 * 60 * 5, false, dataAccess);
		return new RequestAndResponse(request, response);
	}

	public HttpServletRequest setTempSession(HttpServletRequest request, String name, String value, int seconds) {
		if (value.endsWith("null")) {
			value = value.substring(0, value.length() - 4);
		}
		HttpSession session = request.getSession();
		session.setMaxInactiveInterval(seconds);
		session.setAttribute(name, value);
		new DataAccess().addLog("setting tempSession: " + name + ": " + value);
		return request;
	}
}
