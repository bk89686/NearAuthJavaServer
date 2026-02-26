package com.humansarehuman.blue2factor.authentication.saml;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Urls;

/**
 * This is the base Saml page that can be used if the company doesn't want to
 * host it themselves
 * 
 * @author blue2factor
 *
 */
@Controller
@RequestMapping(Urls.COMPANY_VALIDATE)
@SuppressWarnings("ucd")
public class CompanyValidate extends B2fApi {
//	/**
//	 * 1. check for cookie a. if cookie then validate i. if proximate close window
//	 * from source ii. either ask for biometrics, push, text, or reject
//	 * 
//	 * b. show not signed up
//	 * 
//	 * 
//	 */
//
//	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
//	public Object samlStart(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
//			@PathVariable("apiKey") String apiKey) {
//		String next = "closeWindow";
//		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
//		SamlDataAccess dataAccess = new SamlDataAccess();
//		CompanyDbObj company = dataAccess.getCompanyByApiKey(apiKey);
//		IdentityObjectFromServer idObj = null;
//		if (company != null) {
//			String samlRequest = this.getRequestValue(request, "SAMLRequest");
//			String samlResponse = this.getRequestValue(request, "SAMLResponse");
//			String relayState = this.getRequestValue(request, "RelayState");
//			if (TextUtils.isEmpty(samlRequest) && TextUtils.isEmpty(samlResponse)) {
//				idObj = this.getIdentityObjectFromCookie(request, apiKey);
//				relayState = this.getCookie(request, "relayState");
//				if (idObj != null & !TextUtils.isEmpty(relayState)) {
//					dataAccess.addLog("relayState from cookie: " + relayState);
//					if (checkFirstFactor(request, company, idObj) == Outcomes.SUCCESS) {
//						try {
//							UrlAndModel urlAndModel = this.checkSecondFactor(httpResponse, model, idObj, relayState,
//									dataAccess);
//							httpResponse = this.setCookie(httpResponse, "", "relayState", 300, false);
//							next = urlAndModel.getUrl();
//							model = urlAndModel.getModelMap();
//						} catch (Exception e) {
//							next = "failure";
//							model.addAttribute("message1",
//									"An error occured processing your SAML request. Here's what we know:");
//							model.addAttribute("message2", "message = '" + e.getLocalizedMessage() + "'");
//							dataAccess.addLog(e);
//						}
//					}
//				} else {
//					next = "failure";
//					model.addAttribute("message1",
//							"You requested a url that is reserved for SAML requests and responses, but you "
//									+ "did not pass either a request or response");
//				}
//			} else {
//				httpResponse = this.setCookie(httpResponse, relayState, "relayState", 300, false);
//				if (!TextUtils.isEmpty(samlRequest)) {
//					idObj = this.getIdentityObjectFromCookie(request, apiKey);
//					try {
//						UrlAndModel urlAndModel = handleRequestFromServiceProvider(request, httpResponse, model, idObj,
//								company, samlRequest, relayState, dataAccess);
//						next = urlAndModel.getUrl();
//						model = urlAndModel.getModelMap();
//					} catch (Exception e) {
//						next = "failure";
//						model.addAttribute("message1",
//								"An error occured processing your SAML request. Here's what we know:");
//						model.addAttribute("message2", "message = '" + e.getLocalizedMessage() + "'");
//						dataAccess.addLog(e);
//					}
//				} else {
//					next = handleResponseFromIdp(request, httpResponse, model, samlResponse, dataAccess);
//				}
//			}
//		} else {
//			next = "failure";
//			model.addAttribute("message1", "It appears the url you are using is not correct.");
//		}
//		return next;
//	}
//
//	public UrlAndModel checkFirstFactor(HttpServletRequest request, ModelMap model, CompanyDbObj company,
//			IdentityObjectFromServer idObj) {
//		int outcome = Outcomes.FAILURE;
//		CompanyDbObj companyFromIdObj = idObj.getCompany();
//		if (companyFromIdObj != null && companyFromIdObj.getCompanyId().equals(company.getCompanyId())) {
//			if (company.isActive()) {
//				DeviceDbObj device = idObj.getDevice();
//				if (device != null && device.isActive()) {
//					if (device.getSignedIn()) {
//						DeviceDataAccess dataAccess = new DeviceDataAccess();
//						if (dataAccess.isAccessAllowed(device)) {
//							outcome = Outcomes.SUCCESS;
//						} else {
//							// try to validate second factor
//						}
////						BrowserDbObj browser = idObj.getBrowser();
////						if (browser != null && !browser.isExpired()) {
////							if (idObj.getBrowserToken() != null && !idObj.getBrowserToken().isExpired()) {
////								outcome = Outcomes.SUCCESS;
////							} else {
////								outcome = Outcomes.API_F1_FAILURE;
////							}
////						} else {
////							outcome = Outcomes.API_F1_FAILURE;
////						}
//					} else {
//						// go to our identity provider
//
//					}
//				}
//			}
//		}
//		return new UrlAndModel("", model);
//	}
//
//	private String handleResponseFromIdp(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
//			String samlResponse, SamlDataAccess dataAccess) {
//
//		return "";
//	}
//
//	public UrlAndModel handleRequestFromServiceProvider(HttpServletRequest request, HttpServletResponse httpResponse,
//			ModelMap model, IdentityObjectFromServer idObj, CompanyDbObj company, String requestString,
//			String relayState, SamlDataAccess dataAccess) throws Exception {
//		// match the request up with the service provider from out db and send it off to
//		// the identity provider
//		int outcome = Outcomes.FAILURE;
//		String nextPage = null;
//		if (!TextUtils.isEmpty(requestString) && !TextUtils.isEmpty(relayState)) {
//			Saml saml = new Saml();
//			String xmlString = saml.decryptAuthnRequest(requestString);
//			AuthnRequest incomingAuthnRequest = saml.getAuthnRequest(xmlString);
//
//			if (incomingAuthnRequest != null) {
//				Issuer issuer = incomingAuthnRequest.getIssuer();
//				SamlServiceProviderDbObj serviceProvider = dataAccess
//						.getSamlServiceProviderFromIssuer(issuer.getValue());
//				boolean serviceProvidersDontNeedToRegister = true;
//				if (serviceProvider != null || serviceProvidersDontNeedToRegister) {
//					outcome = checkFirstFactor(request, company, idObj);
//					String ipAddress = GeneralUtilities.getClientIp(request);
//					boolean test = false;
//					if (outcome == Outcomes.SUCCESS && test != true) {
////						nextPage = checkSecondFactor(httpResponse, model, incomingAuthnRequest, idObj, relayState,
////								ipAddress, dataAccess);
//						UrlAndModel urlAndModel = checkSecondFactor(httpResponse, model, incomingAuthnRequest, idObj,
//								relayState, ipAddress, requestString, dataAccess);
//						if (urlAndModel != null) {
//							nextPage = urlAndModel.getUrl();
//							model = urlAndModel.getModelMap();
//						} else {
//							nextPage = null;
//						}
//					} else if (outcome == Outcomes.API_F1_FAILURE || test == true) {
//						this.redirectSamlRequestToIdp(httpResponse, incomingAuthnRequest, idObj, serviceProvider,
//								relayState, company, ipAddress, requestString, dataAccess);
//					} else {
//						nextPage = "notSignedUp";
//					}
//				} else {
//					nextPage = "serviceProvidedNotSignedUp";
//				}
//			} else {
//				// this wasn't a valid requestt
//				nextPage = "invalidRequest";
//			}
//		}
//		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
//		return new UrlAndModel(nextPage, model);
//	}
//
//	private UrlAndModel checkSecondFactor(HttpServletResponse httpResponse, ModelMap model,
//			AuthnRequest incomingAuthnRequest, IdentityObjectFromServer idObj, String incomingRelayState,
//			String ipAddress, String encodedIncomingRequest, SamlDataAccess dataAccess) throws Exception {
//		DeviceDbObj device = idObj.getDevice();
//		String nextPage = null;
//		if (dataAccess.isAccessAllowed(device, "buildRequestAndCheckSecondFactor")) {
//			SamlAndLdapResponse response = new SamlAndLdapResponse();
//			dataAccess.addLog("sending back to service provider");
//			UrlAndModel urlAndModel = response.respondToSp(model, incomingAuthnRequest, incomingRelayState,
//					idObj.getGroup(), idObj.getCompany(), true, ipAddress, dataAccess);
//			model = urlAndModel.getModelMap();
//			nextPage = urlAndModel.getUrl();
//		} else {
//			Timestamp issueInstant = DateTimeUtilities.instantToTimestamp(incomingAuthnRequest.getIssueInstant());
//			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
//			CompanyDbObj company = idObj.getCompany();
//			SamlIdentityProviderDbObj samlIdp = dataAccess.getSamlIdpFromCompany(company);
//			SamlAuthnRequestDbObj samlAuthnRequest = new SamlAuthnRequestDbObj(samlIdp.getIdentityProviderName(), now,
//					samlIdp.getTableId(), null, incomingAuthnRequest.getID(), null, incomingRelayState, issueInstant,
//					incomingAuthnRequest.getIssuer().getValue(), null,
//					incomingAuthnRequest.getAssertionConsumerServiceURL(), SignatureStatus.SUCCESS,
//					encodedIncomingRequest, ipAddress, false, company.getCompanyId(), null,
//					idObj.getDevice().getDeviceId(), null, Outcomes.API_UNKNOWN_STATUS,
//					incomingAuthnRequest.getAssertionConsumerServiceURL());
//			dataAccess.addSamlAuthnRequest(samlAuthnRequest);
//			UrlAndModel urlAndModel = new Failure().checkForPushOrBiometrics(model, idObj, dataAccess);
//			nextPage = urlAndModel.getUrl();
//			model = urlAndModel.getModelMap();
//			model.addAttribute("fromIdp", false);
//		}
//		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
//		return new UrlAndModel(nextPage, model);
//	}
//
//	private UrlAndModel checkSecondFactor(HttpServletResponse httpResponse, ModelMap model,
//			IdentityObjectFromServer idObj, String relayState, SamlDataAccess dataAccess) throws Exception {
//		DeviceDbObj device = idObj.getDevice();
//		String nextPage = null;
//		if (dataAccess.isAccessAllowed(device, "buildRequestAndCheckSecondFactor")) {
//			SamlAndLdapResponse response = new SamlAndLdapResponse();
//			dataAccess.addLog("sending back to service provider");
//			SamlAuthnRequestDbObj samlAuthnRequest = dataAccess.getAuthRequestByIncomingRelayState(relayState);
//			UrlAndModel urlAndModel = response.respondToSp(model, samlAuthnRequest, idObj.getGroup(),
//					idObj.getCompany().getApiKey(), Outcomes.SUCCESS);
//			model = urlAndModel.getModelMap();
//			nextPage = urlAndModel.getUrl();
//		} else {
//			UrlAndModel urlAndModel = new Failure().checkForPushOrBiometrics(model, idObj, dataAccess);
//			nextPage = urlAndModel.getUrl();
//			model = urlAndModel.getModelMap();
//			model.addAttribute("fromIdp", false);
//		}
//		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
//		return new UrlAndModel(nextPage, model);
//	}
}
