package com.humansarehuman.blue2factor.authentication.saml;

import org.apache.http.util.TextUtils;
import org.springframework.ui.ModelMap;

import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.tables.BrandingDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;

//@Controller
//@RequestMapping(Urls.VALIDATE)
//@SuppressWarnings("ucd")
public class Validate extends SamlAndLdapResponse {
//	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
//	public String validateGetPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
//			@PathVariable("apiKey") String apiKey) {
//		SamlDataAccess dataAccess = new SamlDataAccess();
//		dataAccess.addLog("start");
//		CompanyDbObj company = dataAccess.getCompanyByApiKey(apiKey);
//		String nextJsp = "userPw";
//		try {
//			UrlAndModel urlAndModel = sendToValidationUi(request, httpResponse, model, company,
//					GeneralUtilities.getClientIp(request), dataAccess);
//			nextJsp = urlAndModel.getUrl();
//			model = urlAndModel.getModelMap();
//		} catch (Exception e) {
//			dataAccess.addLog("Validate", e);
//		}
//		model.addAttribute("test", Urls.getTest());
//		return nextJsp;
//	}

//	private UrlAndModel sendToValidationUi(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
//			CompanyDbObj company, String ipAddress, SamlDataAccess dataAccess) throws Exception {
//		String nextJsp = "userPw";
//		if (company != null) {
//			dataAccess.addLog("company found");
//			String requestString = this.getRequestValue(request, "SAMLRequest");
//			String incomingRelayState = this.getRequestValue(request, "RelayState");
//			AuthnRequest incomingAuthnRequest = null;
//			if (!TextUtils.isEmpty(requestString)) {
//				incomingAuthnRequest = new Saml().getAuthnRequest(requestString);
//				TokenDbObj token = this.getPersistentTokenObj(request);
//				AuthorizationMethod authMethod = company.getF1Method();
//				if (token != null && !token.isExpired()) {
//					dataAccess.addLog("token: " + token.getTokenId());
//					UrlModelAndHttpResponse modelUrlAndHttpResponse = checkSecondFactorAndRedirectWithUnencrypted(
//							httpResponse, model, company, incomingAuthnRequest, incomingRelayState, token, ipAddress,
//							requestString, dataAccess);
//					model = modelUrlAndHttpResponse.getModelMap();
//					httpResponse = modelUrlAndHttpResponse.getHttpResponse();
//					nextJsp = modelUrlAndHttpResponse.getUrl();
//				} else {
//					if (incomingAuthnRequest != null) {
//						UrlAndModel urlAndModel = this.serveRequestFromIncomingSaml(httpResponse, model, authMethod,
//								incomingAuthnRequest, company, requestString, incomingRelayState, ipAddress,
//								dataAccess);
//						nextJsp = urlAndModel.getUrl();
//						model = urlAndModel.getModelMap();
//					}
//				}
//			} else {
//				dataAccess.addLog("incoming request wasn't SAML, maybe we're testing");
//				String samlResponse = this.getRequestValue(request, "SAMLResponse");
//				UrlModelAndHttpResponse urlModelAndHttpResponse = evaluateResponse(request, httpResponse, model,
//						samlResponse, incomingRelayState, company.getApiKey());
//				nextJsp = urlModelAndHttpResponse.toString();
//				model = urlModelAndHttpResponse.getModelMap();
//				httpResponse = urlModelAndHttpResponse.getHttpResponse();
//			}
//
//		} else {
//			dataAccess.addLog("company not found");
//		}
//		model.addAttribute("test", Urls.getTest());
//		return new UrlAndModel(nextJsp, model);
//	}

//	private UrlAndModel serveRequestFromIncomingSaml(HttpServletResponse httpResponse, ModelMap model,
//			AuthorizationMethod authMethod, AuthnRequest incomingAuthnRequest, CompanyDbObj company,
//			String requestString, String incomingRelayState, String ipAddress, SamlDataAccess dataAccess)
//			throws Exception {
//		SamlServiceProviderDbObj serviceProvider = dataAccess.getServiceProviderByAcsUrl(
//				incomingAuthnRequest.getAssertionConsumerServiceURL(), company.getCompanyId());
//		String nextJsp = "userPw";
//		if (serviceProvider != null) {
//			Saml saml = new Saml();
//			if (authMethod.equals(AuthorizationMethod.SAML)) {
//				SamlIdentityProviderDbObj samlIdp = dataAccess.getSamlIdpFromCompanyId(company.getCompanyId());
//				String outgoingAuthId = GeneralUtilities.randomString();
//				dataAccess.addLog("SAML");
//				AuthnRequest outgoingAuthnRequest = saml.buildAuthnRequest(samlIdp, company, outgoingAuthId, true);
//				if (incomingAuthnRequest != null) {
//					SamlAuthnRequestDbObj samlAuthnRequest = saml.buildAndSaveAuthnRequestObj(null, company, samlIdp,
//							serviceProvider, incomingAuthnRequest, outgoingAuthnRequest, incomingRelayState, ipAddress,
//							requestString, dataAccess);
//					redirectSamlRequestToIdp(httpResponse, outgoingAuthnRequest, samlAuthnRequest.getTableId(),
//							samlIdp.getRedirectUrl());
//					nextJsp = null;
//				}
//			} else if (authMethod.equals(AuthorizationMethod.LDAP)) {
//				dataAccess.addLog("ldap");
//				LdapServerDbObj ldapServer = dataAccess.getLdapServerFromCompany(company);
//				SamlAuthnRequestDbObj samlAuthnRequest = saml.buildAndSaveAuthnRequestObjForLdap(company,
//						serviceProvider, incomingAuthnRequest, ldapServer, incomingRelayState, ipAddress, requestString,
//						dataAccess);
//				model = showUsernameAndPwScreen(model, company, samlAuthnRequest.getOutgoingRequestId(), dataAccess);
//			}
//		} else {
//			// this service provider needs to be registered
//			model.addAttribute("test", Urls.getTest());
//			nextJsp = "registerServiceProvider";
//		}
//		model.addAttribute("test", Urls.getTest());
//		return new UrlAndModel(nextJsp, model);
//	}
//
//	private UrlModelAndHttpResponse checkSecondFactorAndRedirectWithUnencrypted(HttpServletResponse httpResponse,
//			ModelMap model, CompanyDbObj company, AuthnRequest incomingAuthnRequest, String relayState,
//			TokenDbObj token, String ipAddress, String encryptedRequest, SamlDataAccess dataAccess) throws Exception {
//		String nextPage = "";
//		boolean success = false;
//		DeviceDbObj device = dataAccess.getDeviceByDeviceId(token.getDeviceId(), "checkSecondFactorAndRedirect");
//		GroupDbObj group = dataAccess.getGroupById(token.getGroupId());
//		if (device != null) {
//			success = dataAccess.isAccessAllowed(device, "checkSecondFactorAndRedirect");
//			if (success) {
//				UrlAndModel urlAndModel = respondToSp(model, incomingAuthnRequest, relayState, group, company, success,
//						ipAddress);
//				nextPage = urlAndModel.getUrl();
//				model = urlAndModel.getModelMap();
//			}
//			if (!success) {
//				// create record in SamlAuthnRequestTable
//				// send to biometrics, push, or text
//				Saml saml = new Saml();
//				saml.buildAndSaveIncomingAuthnRequestObj(incomingAuthnRequest, relayState, company, device,
//						encryptedRequest, token.getTokenId(), ipAddress);
//				int outcome = this.handleNotProximate(group, device);
//				if (outcome == Outcomes.FAILURE) {
//					// show failure
//					UrlAndModel urlAndModel = respondToSp(model, incomingAuthnRequest, relayState, group, company,
//							success, ipAddress);
//					model = urlAndModel.getModelMap();
//					nextPage = urlAndModel.getUrl();
//					model.addAttribute("hideCredentials", true);
//					model.addAttribute("message1", "Blue2Factor has blocked access.");
//					model.addAttribute("message2",
//							"We could not find another of your registered devices nearby, and this "
//									+ "device does not support biometrics.");
//				}
//			}
//		}
//
//		return new UrlModelAndHttpResponse(nextPage, model, httpResponse);
//	}

	public ModelMap showUsernameAndPwScreen(ModelMap model, CompanyDbObj company, String relayState,
			SamlDataAccess dataAccess) {
		return this.showUsernameAndPwScreen(model, company, relayState, dataAccess, "");
	}

	public ModelMap showUsernameAndPwScreen(ModelMap model, CompanyDbObj company, String relayState,
			SamlDataAccess dataAccess, String error) {
		try {
			dataAccess.addLog("looking for branding");
			BrandingDbObj branding = dataAccess.getBranding(company);
			String icon = "";
			int height = 0;
			int width = 0;
			String action = Urls.SECURE_URL + Urls.LDAP_SUBMIT.replace("{apiKey}", company.getApiKey());
			String backgroundColor = "darkblue";
			String foregroundColor = "white";
			if (branding != null) {
				dataAccess.addLog("branding row found");
				icon = branding.getIconPath();
				backgroundColor = branding.getBackgroundColor();
				foregroundColor = branding.getForegroundColor();
				if (!TextUtils.isEmpty(icon)) {
					height = 50;
					width = 50;
				}
			}
			boolean showError = !TextUtils.isBlank(error);
			model.addAttribute("showError", showError);
			model.addAttribute("errorText", error);
			model.addAttribute("backgroundImage", icon);
			model.addAttribute("iconHeight", height);
			model.addAttribute("iconWidth", width);
			model.addAttribute("backgroundColor", backgroundColor);
			model.addAttribute("foregroundColor", foregroundColor);
			model.addAttribute("action", action);
			model.addAttribute("relayState", relayState);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return model;
	}

}
