package com.humansarehuman.blue2factor.authentication.saml;

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
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.authentication.failures.Failure;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.UrlAndModel;
import com.humansarehuman.blue2factor.entities.enums.SignatureStatus;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlAuthnRequestDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlIdentityProviderDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.humansarehuman.blue2factor.utilities.saml.Saml;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * 
 * @author cjm10
 *
 */

@Controller
@RequestMapping(Urls.SAML_SIGN_IN)
@SuppressWarnings("ucd")
public class SamlSignin extends B2fApi {
	boolean testing = false;

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	public String samlIdpRelayGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			@PathVariable("apiKey") String apiKey) {
		String next;
		String relayState = this.getRequestValue(request, "RelayState");
		String encryptedAuthnRequest = this.getRequestValue(request, "SAMLRequest");
		SamlDataAccess dataAccess = new SamlDataAccess();
		dataAccess.addLog("relayState: " + relayState);
		dataAccess.addLog("SAMLRequest: " + encryptedAuthnRequest);
		if (!TextUtils.isEmpty(encryptedAuthnRequest) && !TextUtils.isEmpty(relayState)) {
			String authToken = getPersistentToken(request);
			dataAccess.addLog("authToken: " + authToken);
			IdentityObjectFromServer idObj = this.getIdObj(authToken, dataAccess);
			if (idObj == null) {
				next = "notSignedUp";
			} else {
				Saml saml = new Saml();
				SamlAndLdapResponse samlAndLdapResponse = new SamlAndLdapResponse();
				String clientIpAddress = GeneralUtilities.getClientIp(request);
				try {
					String unencrypted = saml.decryptAuthnRequest(encryptedAuthnRequest);
					AuthnRequest incomingAuthnRequest = saml.getAuthnRequest(unencrypted);
					SamlIdentityProviderDbObj samlIdp = dataAccess.getSamlIdpFromCompany(idObj.getCompany());
					AuthnRequestAndSamlDbObj authnRequestAndSamlDbObj = this.buildSamlAuthnObj(httpResponse, dataAccess,
							samlIdp, incomingAuthnRequest, idObj.getCompany(), relayState, encryptedAuthnRequest,
							clientIpAddress, idObj);
					DeviceDbObj device = idObj.getDevice();
					if (device == null || !device.getSignedIn()
							|| !(areCompaniesEqual(idObj.getCompany(), apiKey, dataAccess))) {
						dataAccess.addLog("we need to reauthenticate with main IDP");
						redirectRequestToOurIdp(httpResponse, authnRequestAndSamlDbObj.samlAuthnRequestDbObj, samlIdp,
								idObj.getCompany());
						next = null;
					} else {
						UrlAndModel urlAndModel;
						if (dataAccess.isAccessAllowed(device, "samlIdpRelayGet")) {
							dataAccess.addLog("accessIsAllowed");
							urlAndModel = samlAndLdapResponse.respondToSp(model,
									authnRequestAndSamlDbObj.samlAuthnRequestDbObj, idObj.getGroup(),
									idObj.getCompany().getApiKey(), idObj.getCompany().getEntityIdVal(),
									Outcomes.SUCCESS, dataAccess);
						} else {
							dataAccess.addLog(device.getDeviceId(), "access not allowed yet");
							if (dataAccess.deviceIsTemp(device)) {
								dataAccess.addLog(device.getDeviceId(), "device is temp");
								urlAndModel = sendPushOrTextIfPossible(model, idObj, dataAccess);

							} else {
								dataAccess.addLog(device.getDeviceId(), "looking to push or text");
								urlAndModel = new Failure().checkForPushOrBiometrics(model, idObj, dataAccess);
							}
							urlAndModel = samlAndLdapResponse.addSamlResponseAndRelayState(urlAndModel,
									authnRequestAndSamlDbObj.samlAuthnRequestDbObj, idObj.getGroup(),
									idObj.getCompany().getApiKey(), idObj.getCompany().getEntityIdVal(),
									Outcomes.SUCCESS, dataAccess);

						}
						model = urlAndModel.getModelMap();
						next = urlAndModel.getUrl();
						model.addAttribute("fromIdp", true);

					}
				} catch (Exception e) {
					dataAccess.addLog(e);
					next = "error";
					model.addAttribute("errorMessage", getStackTrace(e));
				}
			}
		} else {
			next = "error";
			model.addAttribute("errorMessage", "invalid request");
		}
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return next;
	}

	public boolean areCompaniesEqual(CompanyDbObj company, String apiKey, CompanyDataAccess dataAccess) {
		boolean success = false;
		CompanyDbObj apiCompany = dataAccess.getCompanyByApiKey(apiKey);
		if (company != null && apiCompany != null) {
			success = company.equals(apiCompany);
		}
		return success;
	}

//
//	private UrlAndModel checkForPreviousResult(HttpServletRequest request, HttpServletResponse httpResponse,
//			ModelMap model, String apiKey) {
//		String src = this.getSession(request, "src");
//		SamlDataAccess dataAccess = new SamlDataAccess();
//		UrlAndModel urlAndModel = null;
//		try {
//			if (!TextUtils.isBlank(src)) {
//				this.setSession(request, "src", "");
//				String[] splitRequest = src.split("&SAMLRequest=");
//				if (splitRequest.length == 2) {
//					String encryptedAuthnRequest = splitRequest[1];
//					String[] splitRequest2 = splitRequest[0].split("RelayState=");
//					if (splitRequest2.length == 2) {
//						String relayState = splitRequest2[1];
//						Saml saml = new Saml();
//						String unencrypted = saml.decryptAuthnRequest(encryptedAuthnRequest);
//						AuthnRequest incomingAuthnRequest = saml.getAuthnRequest(unencrypted);
//						CompanyDbObj company = dataAccess.getCompanyByApiKey(apiKey);
//						if (company != null) {
//							dataAccess.addLog("checkForPreviousResult", "company found");
//							SamlIdentityProviderDbObj samlIdp = dataAccess.getSamlIdpFromCompany(company);
//							IdentityObjectFromServer idObj = this.getIdentityObjectFromCookie(request, apiKey);
//							AuthnRequestAndSamlDbObj authnRequestAndSamlDbObj = this.buildSamlAuthnObj(httpResponse,
//									dataAccess, samlIdp, incomingAuthnRequest, company, relayState,
//									encryptedAuthnRequest, "", idObj);
//							if (idObj != null && idObj.getBrowser() != null & idObj.getCompany() != null
//									&& idObj.getCompany().getCompanyId().equals(company.getCompanyId())
//									&& idObj.getDevice() != null && idObj.getDevice().getSignedIn()) {
//								// verify access and return without visiting the IDP
//								dataAccess.addLog("checkForPreviousResult", "existing cookie works");
//								if (dataAccess.isAccessAllowed(idObj.getDevice())) {
//									// redirect to back SAML service provider
//									SamlResponseFromAuthnRequest samlResp = new SamlResponseFromAuthnRequest();
//
//									urlAndModel = samlResp.respondToSp(model,
//											authnRequestAndSamlDbObj.samlAuthnRequestDbObj, idObj.getGroup(), apiKey,
//											Outcomes.SUCCESS);
//								}
//							}
//						}
//					}
//				}
//			}
//		} catch (Exception e) {
//			dataAccess.addLog("checkForPreviousResult", e);
//		}
//
//		return urlAndModel;
//	}
//
//	private UrlAndModel relayResponse(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
//			String relayState, String encryptedAuthnRequest, String apiKey) {
//		Saml saml = new Saml();
//		SamlDataAccess dataAccess = new SamlDataAccess();
//		UrlAndModel urlAndModel = new UrlAndModel(null, model);
//		try {
//			if (!TextUtils.isBlank(encryptedAuthnRequest)) {
//				dataAccess.addLog("relayResponse", "relayState: " + relayState);
//				String clientIpAddress = GeneralUtilities.getClientIp(request);
//				String unencrypted = saml.decryptAuthnRequest(encryptedAuthnRequest);
//				AuthnRequest incomingAuthnRequest = saml.getAuthnRequest(unencrypted);
//				CompanyDbObj company = dataAccess.getCompanyByApiKey(apiKey);
//				if (testing) {
//					company = getTestCompany();
//				}
//				if (company != null) {
//					dataAccess.addLog("relayResponse", "company found");
//					SamlIdentityProviderDbObj samlIdp = dataAccess.getSamlIdpFromCompany(company);
//					IdentityObjectFromServer idObj = this.getIdentityObjectFromCookie(request, apiKey);
//					AuthnRequestAndSamlDbObj authnRequestAndSamlDbObj = this.buildSamlAuthnObj(httpResponse, dataAccess,
//							samlIdp, incomingAuthnRequest, company, relayState, encryptedAuthnRequest, clientIpAddress,
//							idObj);
//					if (idObj != null && idObj.getBrowser() != null & idObj.getCompany() != null
//							&& idObj.getCompany().getCompanyId().equals(company.getCompanyId())
//							&& idObj.getDevice() != null && idObj.getDevice().getSignedIn()) {
//						// verify access and return without visiting the IDP
//						dataAccess.addLog("relayResponse", "existing cookie works");
//						if (dataAccess.isAccessAllowed(idObj.getDevice())) {
//							// redirect to SAML service provider
//							SamlResponseFromAuthnRequest samlResp = new SamlResponseFromAuthnRequest();
//
//							urlAndModel = samlResp.respondToSp(model, authnRequestAndSamlDbObj.samlAuthnRequestDbObj,
//									idObj.getGroup(), apiKey, Outcomes.SUCCESS);
//						} else {
//							// either redirect to our page for biometric login or send a push
//							// notification
//
//							urlAndModel = checkForPushOrBiometrics(model, idObj, dataAccess);
//							String nextPage = urlAndModel.getUrl();
//							model = urlAndModel.getModelMap();
//							model.addAttribute("environment", Constants.ENVIRONMENT.toString());
//							model.addAttribute("fromIdp", true);
//							String successPage = Urls.SECURE_URL + Urls.SAML_IDP_RELAY.replace("{apiKey}", apiKey)
//									+ "?RelayState=" + relayState + "&SAMLRequest=" + encryptedAuthnRequest;
//							dataAccess.addLog("relayResponse", "successPage: " + successPage);
//							urlAndModel = new UrlAndModel(nextPage, model);
//							failure.setTempSession(request, "src", successPage, 60 * 60 * 12);
//						}
//					} else {
//						dataAccess.addLog("relayResponse", "existing cookie doesn't work checking IDP");
//						if (authnRequestAndSamlDbObj.authnRequest != null
//								&& authnRequestAndSamlDbObj.samlAuthnRequestDbObj != null) {
//							// go to the IDP and set up a new browser token
//							String newRelay = authnRequestAndSamlDbObj.samlAuthnRequestDbObj.getTableId();
//							redirectRequestToOurIdp(httpResponse, samlIdp, company, newRelay);
//						} else {
//							dataAccess.addLog("relayResponse", "relay response was null", LogConstants.WARNING);
//						}
//					}
//				} else {
//					dataAccess.addLog("company not found");
//				}
//			} else {
//				dataAccess.addLog("relayResponse", "encryptedAuthResponse is blank");
//			}
//		} catch (Exception e) {
//			dataAccess.addLog("relayResponse", e);
//		}
//		return urlAndModel;
//	}

	class AuthnRequestAndSamlDbObj {
		public SamlAuthnRequestDbObj samlAuthnRequestDbObj;
		public AuthnRequest authnRequest;

		public AuthnRequestAndSamlDbObj(AuthnRequest authnRequest, SamlAuthnRequestDbObj samlAuthnRequestDbObj) {
			this.authnRequest = authnRequest;
			this.samlAuthnRequestDbObj = samlAuthnRequestDbObj;
		}
	}

	public AuthnRequestAndSamlDbObj buildSamlAuthnObj(HttpServletResponse httpResponse, SamlDataAccess dataAccess,
			SamlIdentityProviderDbObj samlIdp, AuthnRequest incomingAuthnRequest, CompanyDbObj company,
			String incomingRelayState, String encryptedAuthnRequest, String clientIpAddress,
			IdentityObjectFromServer idObj) {
		AuthnRequest authnRequest = null;
		Saml saml = new Saml();
		SamlAuthnRequestDbObj samlAuthnRequest = null;
		if (samlIdp != null) {
			try {
				String authId = "b2f_" + GeneralUtilities.randomString();
				String incomingRequestId = incomingAuthnRequest.getID();
				String identityProviderName = samlIdp.getIdentityProviderName();
				dataAccess.addLog("building new request", LogConstants.TEMPORARILY_IMPORTANT);
				authnRequest = saml.buildAuthnRequest(samlIdp, company, authId, true);
				String tableId = GeneralUtilities.randomString();
				java.sql.Timestamp now = DateTimeUtilities.getCurrentTimestamp();
				String devId = null;
				String browserId = null;
				if (idObj != null) {
					if (idObj.getDevice() != null) {
						devId = idObj.getDevice().getDeviceId();
						if (idObj.getBrowser() != null) {
							browserId = idObj.getBrowser().getBrowserId();
						}
					}
				}
				AccessCodeDbObj accessCode = new AccessCodeDbObj(GeneralUtilities.randomString(),
						company.getCompanyId(), null, devId, 0, true, browserId, false);
				dataAccess.addAccessCode(accessCode, "buildSamlAuthnObj");
				samlAuthnRequest = new SamlAuthnRequestDbObj(tableId, identityProviderName, now, samlIdp.getTableId(),
						null, incomingRequestId, null, incomingRelayState,
						DateTimeUtilities.instantToTimestamp(incomingAuthnRequest.getIssueInstant()),
						incomingAuthnRequest.getIssuer().getValue(),
						incomingAuthnRequest.getAssertionConsumerServiceURL(),
						incomingAuthnRequest.getAssertionConsumerServiceURL(), SignatureStatus.UNVALIDATE,
						encryptedAuthnRequest, clientIpAddress, false, company.getCompanyId(), null, null,
						accessCode.getAccessCode(), Outcomes.INCOMPLETE, incomingAuthnRequest.getIssuer().getValue());
				dataAccess.addSamlAuthnRequest(samlAuthnRequest);
				dataAccess.addLog("buildSamlAuthnObj", "added SamlAuthnRequest");
			} catch (Exception e1) {
				dataAccess.addLog("buildSamlAuthnObj", e1);
			}
		}
		return new AuthnRequestAndSamlDbObj(authnRequest, samlAuthnRequest);
	}

	@SuppressWarnings("unchecked")
	private void redirectRequestToOurIdp(HttpServletResponse httpResponse, SamlAuthnRequestDbObj request,
			SamlIdentityProviderDbObj samlIdp, CompanyDbObj company) throws Exception {
		Saml saml = new Saml();
		SamlDataAccess dataAccess = new SamlDataAccess();
		String authId = "BTF_" + GeneralUtilities.randomNumberString(25);
		String relayState = GeneralUtilities.randomString();
		dataAccess.addLog("authId: " + authId);
		AuthnRequest outgoingAuthnRequest = saml.buildAuthnRequest(samlIdp, company, authId, true);
		request.setOutgoingRelayState(relayState);
		request.setOutgoingRequestId(authId);
		dataAccess.updateSamlAuthRequestByTableId(request);
		dataAccess.addLog("authnReq: " + saml.authnRequestToString(outgoingAuthnRequest, false));
		MessageContext messageContext = new MessageContext();
		messageContext.setMessage(outgoingAuthnRequest);
		XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
		SAMLObjectBuilder<Endpoint> endpointBuilder = (SAMLObjectBuilder<Endpoint>) builderFactory
				.getBuilder(AssertionConsumerService.DEFAULT_ELEMENT_NAME);

		Endpoint samlEndpoint = endpointBuilder.buildObject();
		samlEndpoint.setLocation(samlIdp.getRedirectUrl());

		SAMLBindingSupport.setRelayState(messageContext, relayState);
		SAMLPeerEntityContext peerEntityContext = messageContext.ensureSubcontext(SAMLPeerEntityContext.class);
		SAMLEndpointContext endpointContext = peerEntityContext.ensureSubcontext(SAMLEndpointContext.class);
		endpointContext.setEndpoint(samlEndpoint);

		HTTPRedirectDeflateEncoder httpRedirectDeflateEncoder = new HTTPRedirectDeflateEncoder();
		httpRedirectDeflateEncoder.setMessageContext(messageContext);
		dataAccess.addLog("redirectRequestToOurIdp", "sending to: " + samlIdp.getRedirectUrl());
		httpResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		httpRedirectDeflateEncoder.setHttpServletResponseSupplier(() -> httpResponse);
		httpRedirectDeflateEncoder.initialize();
		httpRedirectDeflateEncoder.encode();
		dataAccess.addLog("redirectRequestToOurIdp", "end");
	}
//
//	public CompanyDbObj getTestCompany() {
//		return new CompanyDbObj("Qbojvg56bLlCVKy27Yr3sIgCiwiuP9eL8FRTx1Nj", "NearAuth.ai", "", 0, true, new Date(),
//				"https://christophermclain.com", "https://christophermclain.com", "VYG2694OO55", 10, 2,
//				"https://christophermclain.com", "https://www.christophermclain.com/testSignin",
//				AuthorizationMethod.SAML, AuthorizationMethod.API, "", "", NonMemberStrategy.ALLOW_NOT_SIGNED_UP, false,
//				false, 86400, 86400, 18000);
//	}
//
//	public SamlIdentityProviderDbObj getTestIdp() {
//		return new SamlIdentityProviderDbObj(GeneralUtilities.randomString(), DateTimeUtilities.getCurrentTimestamp(),
//				"https://accounts.google.com/o/saml2?idpid=C03zt1do1", "google",
//				"MIIDdDCCAlygAwIBAgIGAW5/ZVteMA0GCSqGSIb3DQEBCwUAMHsxFDASBgNVBAoTC0dvb2dsZSBJ\n"
//						+ "bmMuMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MQ8wDQYDVQQDEwZHb29nbGUxGDAWBgNVBAsTD0dv\n"
//						+ "b2dsZSBGb3IgV29yazELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWEwHhcNMTkxMTE4\n"
//						+ "MTY0MjU4WhcNMjQxMTE2MTY0MjU4WjB7MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEWMBQGA1UEBxMN\n"
//						+ "TW91bnRhaW4gVmlldzEPMA0GA1UEAxMGR29vZ2xlMRgwFgYDVQQLEw9Hb29nbGUgRm9yIFdvcmsx\n"
//						+ "CzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A\n"
//						+ "MIIBCgKCAQEA4tZFQm0dEPhMcNNaCjfKIeZOwFQSGtnCwwdxpF8gNA5Fqfse2FNmLd3h5e4WBMSg\n"
//						+ "/s0NGDUr2TwMcI0+eZZiH9pFzmpDCWOOogLjBMV6bDdw8L0FSw6Zs32K0YfTuZhU5dpF1+djDhIg\n"
//						+ "U4+hVeyjwb66xRyXzIxrfxbsxuRr22ZUK49du42RYUUiC36Bwki54J1ExX1UDsbZgN7wD8tGCd82\n"
//						+ "up8mIwaE8T8JjL2coleaSnVJxa0CLjtoFwKmAUSFP+L1v9oMqXfBJAzV4EhhP4MhfNSHjOroLus6\n"
//						+ "x+K7gxBQXi/76YsmWW2R42R3OY1r7wUtSEsBUPcZYtux1/F8CQIDAQABMA0GCSqGSIb3DQEBCwUA\n"
//						+ "A4IBAQADfWOj9VmwmwIT63sRPw0yNq+VqGb+zowHFUzwvLtJTgE20lvTaAA8ZxsiA2CXrkQ05J68\n"
//						+ "e8E6Fv1FxGuTJlUp/jKubWvCgWwbFRCM3Pz77MK2rBOlMsH7/KvL1JJRqP6AyznuIaCmTdS7UT/n\n"
//						+ "b6lV7Jp8Ki236yxxMAi2DQumdgCfJcO9e0BBQ3jocDoHtU0RZt768cEKsi7+lOvCCvmsc45tb38b\n"
//						+ "5aeApvJe6WycEZVHh+Sw6cS9PBBZgBzWJrhQICkgv1h+CiSgFekHs4nv9fePcu4BSSDXnBgTOaw5\n"
//						+ "eRSeNzeDgPuceasP+dGyfS9acktmAzS5uUCNLFJqOul7",
//				null, "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress",
//				"https://accounts.google.com/o/saml2/idp?idpid=C03zt1do1",
//				"https://accounts.google.com/o/saml2/idp?idpid=C03zt1do1", "", true, null, false, false,
//				Urls.SECURE_URL + "/SAML2/SSO/fromIdp", Urls.SECURE_URL + "/SAML2/SSO/fromIdp", "", "", "",
//				DateTimeUtilities.getCurrentTimestamp(), "");
//	}
//
//	@RequestMapping(method = RequestMethod.POST)
//	public void samlIdpRelayProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
//			@PathVariable("apiKey") String apiKey) {
//		String token = this.getRequestValue(request, "RelayState");
//		String samlResponse = this.getRequestValue(request, "SAMLResponse");
//		relayResponse(request, httpResponse, model, token, samlResponse, apiKey);
//	}
}
