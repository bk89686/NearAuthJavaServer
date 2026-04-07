package com.humansarehuman.blue2factor.authentication.serverAuth;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.authentication.failures.Failure;
import com.humansarehuman.blue2factor.authentication.saml.Validate;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.UrlAndModel;
import com.humansarehuman.blue2factor.entities.enums.AuthorizationMethod;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.LdapServerDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlAuthnRequestDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlIdentityProviderDbObj;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.humansarehuman.blue2factor.utilities.saml.Saml;

@Controller
@RequestMapping(value = Urls.FIRST_FACTOR_SETUP)
@SuppressWarnings("ucd")
public class FirstFactorSetup extends B2fApi {
	@RequestMapping(method = RequestMethod.GET)
	public RedirectView processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		RedirectView redirectView = null;
		SamlDataAccess dataAccess = new SamlDataAccess();
		String companyId = this.getRequestValue(request, "CoId");
		String encText = this.getRequestValue(request, "ljn");
		Encryption encryption = new Encryption();
		dataAccess.addLog("FirstFactorSetup", "first factor setup, coId: " + companyId);
		CompanyDbObj company = dataAccess.getCompanyByApiKey(companyId);
		boolean redirected = false;
		String sender = "";
		if (company != null) {
			try {
				encText = URLDecoder.decode(encText, StandardCharsets.UTF_8.toString()).replace(" ", "+");
				dataAccess.addLog("FirstFactorSetup", "urlDecoded: " + encText);
				String unencryptedText = encryption.decryptClientServerText(company, encText);
				if (unencryptedText != null) {
					dataAccess.addLog("FirstFactorSetup", "unencryptedText: " + unencryptedText);
					String tokenId = getValueFromParamString("token", unencryptedText);
					sender = getValueFromParamString("sender", unencryptedText);
					try {
						AccessCodeDbObj accessCode = dataAccess.getAccessCodeFromAccessString(tokenId);
						if (accessCode != null) {
							if (company.getF1Method().equals(AuthorizationMethod.SAML)) {
								handleInitialSamlAuth(request, httpResponse, company, accessCode, sender, "setup");
								redirected = true;
							} else if (company.getF1Method().equals(AuthorizationMethod.NONE)) {
								// if this is allowed we need to setup a process - cjm
							}
						}
					} catch (Exception e) {
						dataAccess.addLog("FirstFactorSetup", e);
					}
				}
			} catch (Exception e1) {
				dataAccess.addLog("FirstFactorSetup", e1);
			}
		}
		if (!redirected && redirectView == null) {
			redirectView = new RedirectView("/setupFailure?src=" + sender);
		}
		return redirectView;
	}

	public void handleInitialSamlAuth(HttpServletRequest request, HttpServletResponse httpResponse,
			CompanyDbObj company, AccessCodeDbObj accessCode, String sender, String incomingRequestId)
			throws Exception {
		int logLevel = LogConstants.TRACE;
		SamlDataAccess dataAccess = new SamlDataAccess();
		String authId = GeneralUtilities.randomString();
		Saml saml = new Saml();
		dataAccess.addLog("getting saml idp", logLevel);
		SamlIdentityProviderDbObj samlIdp = dataAccess.getSamlIdpFromCompanyId(company.getCompanyId());
		
		AuthnRequest authnRequest = saml.buildAuthnRequest(samlIdp, company, authId, true);
		String relayState = GeneralUtilities.randomString();
		String referrer = new Failure().getReferrer(request, company, dataAccess);
		dataAccess.addLog("referrer (1): " + referrer + ", sender: " + sender, logLevel);
		SamlAuthnRequestDbObj samlAuthn = saml.buildAndSaveAuthnRequestObj(company, accessCode, samlIdp, authId,
				relayState, referrer, incomingRequestId);
		dataAccess.addLog("redirecting to IDP", logLevel);
		redirectSamlRequestToIdp(httpResponse, authnRequest, samlAuthn.getOutgoingRelayState(),
				samlIdp.getRedirectUrl());
	}

	public boolean handleSamlAuth(HttpServletRequest request, HttpServletResponse httpResponse, CompanyDbObj company,
			DeviceDbObj device, AccessCodeDbObj accessCode, String sender, String incomingRequestId) throws Exception {
		SamlDataAccess dataAccess = new SamlDataAccess();
		boolean validated = false;
		if (!alreadyVerified(device)) {
			String authId = GeneralUtilities.randomString();

			Saml saml = new Saml();
			SamlIdentityProviderDbObj samlIdp = dataAccess.getSamlIdpFromCompanyId(company.getCompanyId());
			AuthnRequest authnRequest = saml.buildAuthnRequest(samlIdp, company, authId, true);
			String relayState = GeneralUtilities.randomString();
			String referrer = new Failure().getReferrer(request, company, dataAccess);
			dataAccess.addLog("referrer (2): " + referrer + ", sender: " + sender, LogConstants.TRACE);
			SamlAuthnRequestDbObj samlAuthn = saml.buildAndSaveAuthnRequestObj(company, accessCode, samlIdp, authId,
					relayState, referrer, incomingRequestId);
			redirectSamlRequestToIdp(httpResponse, authnRequest, samlAuthn.getOutgoingRelayState(),
					samlIdp.getRedirectUrl());
		} else {
			dataAccess.addLog("already validated");
			validated = true;
		}
		return validated;
	}

	public boolean handleSamlAuth(HttpServletRequest request, HttpServletResponse httpResponse,
			IdentityObjectFromServer idObj, AccessCodeDbObj accessCode, String sender, String incomingRequestId)
			throws Exception {
		boolean validated = false;
		SamlDataAccess dataAccess = new SamlDataAccess();
		if (!alreadyVerified(idObj.getDevice())) {
			CompanyDbObj company = idObj.getCompany();
			String authId = GeneralUtilities.randomString();

			Saml saml = new Saml();
			SamlIdentityProviderDbObj samlIdp = dataAccess.getSamlIdpFromCompanyId(company.getCompanyId());
			AuthnRequest authnRequest = saml.buildAuthnRequest(samlIdp, company, authId, true);
			String relayState = GeneralUtilities.randomString();
			String referrer = new Failure().getReferrer(request, company, dataAccess);
			dataAccess.addLog("referrer: " + referrer + ", sender: " + sender);
			SamlAuthnRequestDbObj samlAuthn = saml.buildAndSaveAuthnRequestObj(company, accessCode, samlIdp, authId,
					relayState, referrer, incomingRequestId);
			redirectSamlRequestToIdp(httpResponse, authnRequest, samlAuthn.getOutgoingRelayState(),
					samlIdp.getRedirectUrl());

		} else {
			dataAccess.addLog("already validated");
			validated = true;
		}
		return validated;
	}

	/**
	 * If we are coming from a peripheral that has already been verified, then we
	 * wont re-authenticate
	 * 
	 * @return
	 */
	public boolean alreadyVerified(DeviceDbObj device) {
		boolean verified = false;
		if (device != null) {
			if (device.getSignedIn()) {
				verified = true;
			}
		}
		return verified;
	}

	public UrlAndModel handleLdapAuth(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			CompanyDbObj company, AccessCodeDbObj accessCode, String sender, String incomingRequestId)
			throws Exception {
		String authId = GeneralUtilities.randomString();
		SamlDataAccess dataAccess = new SamlDataAccess();
		Saml saml = new Saml();
		LdapServerDbObj ldapServer = dataAccess.getLdapServerFromCompany(company);
		String requestId = GeneralUtilities.randomString();
		String referrer = new Failure().getReferrer(request, company, dataAccess);
		SamlAuthnRequestDbObj samlAuthnRequest = saml.buildAndSaveAuthnRequestObjForLdap(company, accessCode,
				ldapServer, authId, requestId, referrer, incomingRequestId);
		model = new Validate().showUsernameAndPwScreen(model, company, samlAuthnRequest.getOutgoingRelayState(),
				dataAccess);
		String redirect = "userPw";
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return new UrlAndModel(redirect, model);
	}

	private String getValueFromParamString(String key, String paramsString) {
		String returnVal = null;
		String[] splitStr = paramsString.split(key + "=");
		DataAccess dataAccess = new DataAccess();
		if (splitStr.length > 1) {
			returnVal = splitStr[1].split("&")[0];
			dataAccess.addLog("getValueFromText", "key = " + returnVal);
		}
		return returnVal;
	}
}
