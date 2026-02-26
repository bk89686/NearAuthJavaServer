package com.humansarehuman.blue2factor.authentication;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.authentication.saml.Validate;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.UrlAndModel;
import com.humansarehuman.blue2factor.entities.enums.AuthorizationMethod;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.LdapServerDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlAuthnRequestDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlIdentityProviderDbObj;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.humansarehuman.blue2factor.utilities.saml.Saml;

@Controller
@RequestMapping(Urls.VALIDATE_PERIPHERAL)
@SuppressWarnings("ucd")
public class ValidatePeripheral extends B2fApi {
	@RequestMapping(method = RequestMethod.GET)
	public String validatePeripheralGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		String nextJsp = null;
		SamlDataAccess dataAccess = new SamlDataAccess();
		// TODO: get rid of this once we update the app
		String email = this.getRequestValue(request, Parameters.EMAIL_VALIDATE);
		String devId = this.getRequestValue(request, Parameters.DEV_ID_VALIDATE);
		if (email.equals("")) {
			dataAccess.addLog("email was empty");
			// through here, but also the closing parentheses
			String key = getKey(request);
			String iv = getInitVector(request);
			email = this.getEncryptedRequestValue(request, Parameters.EMAIL_VALIDATE, key, iv).toLowerCase();
			devId = this.getEncryptedRequestValue(request, Parameters.DEV_ID_VALIDATE, key, iv);
		}
		dataAccess.addLog("email: " + email);
		dataAccess.addLog("deviceId: " + devId);
		GroupDbObj group = dataAccess.getGroupByEmail(email);
		if (group == null) {
			group = this.addUserToGroupIfNeeded(email);
		}
		if (group != null) {
			try {
				CompanyDbObj company = dataAccess.getCompanyById(group.getCompanyId());
				if (company != null) {
					UrlAndModel urlAndModel = this.redirectBasedOnCompany(request, httpResponse, model, company, group,
							devId, dataAccess);
					nextJsp = urlAndModel.getUrl();
					model = urlAndModel.getModelMap();
				} else {
					if (company == null) {
						dataAccess.addLog("company null for coId: " + group.getCompanyId());
					}

				}
			} catch (Exception e) {
				dataAccess.addLog(e);
			}
		} else {
			dataAccess.addLog("group not in database yet. We'll see what we can do.");
			String emailDomain = GeneralUtilities.getEmailDomain(email);
			if (emailDomain != null) {
				CompanyDbObj company = dataAccess.getCompanyByEmailDomain(emailDomain);
				if (company != null && company.isAllowAllFromIdp()) {
					UrlAndModel urlAndModel = this.redirectBasedOnCompany(request, httpResponse, model, company, group,
							devId, dataAccess);
					nextJsp = urlAndModel.getUrl();
					model = urlAndModel.getModelMap();
				}
			}
		}
		return nextJsp;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String validatePeripheralPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String authMethod = "";
		String previouslyInstalled = "false"; // in this case, it's whether or not this person has
												// signed up
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String email = this.getEncryptedRequestValue(request, "sahsenthusnhau", key, iv);

			GroupDbObj group = dataAccess.getGroupByEmail(email);
			if (group == null) {
				group = this.addUserToGroupIfNeeded(email);
			}
			if (group != null && group.isActive()) {
				reason = group.getGroupId();
				CompanyDbObj company = dataAccess.getCompanyById(group.getCompanyId());
				if (company != null && company.isActive()) {
					AuthorizationMethod authorizationMethod = company.getF1Method();
					if (authorizationMethod != null) {
						authMethod = authorizationMethod.authMethodName();
						if (authorizationMethod.equals(AuthorizationMethod.SAML)) {
							dataAccess.addLog("auth method is saml");
						}
						outcome = Outcomes.SUCCESS;
					}
					ArrayList<DeviceDbObj> devices = dataAccess.getActiveDevicesByGroupId(group.getGroupId());
					if (devices.size() > 1) {
						previouslyInstalled = "true";
					}
				} else {
					reason = "company was not found or not active";
				}
			} else {
				dataAccess.addLog("group not in database yet. We'll see what we can do.");
				String emailDomain = GeneralUtilities.getEmailDomain(email);
				if (emailDomain != null) {
					CompanyDbObj company = dataAccess.getCompanyByEmailDomain(emailDomain);
					if (company != null && company.isAllowAllFromIdp()) {
						AuthorizationMethod authorizationMethod = company.getF1Method();
						if (authorizationMethod != null) {
							authMethod = authorizationMethod.authMethodName();
							if (authorizationMethod.equals(AuthorizationMethod.SAML)) {
								dataAccess.addLog("auth method is saml");
							}
							outcome = Outcomes.SUCCESS;
						}
					}
				}
			}

			BasicResponse response = new BasicResponse(outcome, reason, authMethod, previouslyInstalled);
			model = this.addBasicResponse(model, response);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return "resultWithInstanceId";
	}

	private UrlAndModel redirectBasedOnCompany(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model, CompanyDbObj company, GroupDbObj group, String deviceId, SamlDataAccess dataAccess) {
		String nextJsp = "error";
		try {
			String ipAddress = GeneralUtilities.getClientIp(request);
			AuthorizationMethod authMethod = company.getF1Method();
			dataAccess.addLog("authorizationMethod: " + authMethod.authMethodName());
			if (authMethod.equals(AuthorizationMethod.SAML)) {
				this.buildAndRedirectSaml(httpResponse, company, group, ipAddress, dataAccess, deviceId);
				nextJsp = null;
			} else if (authMethod.equals(AuthorizationMethod.LDAP)) {
				UrlAndModel urlAndModel = this.buildAndRedirectLdap(httpResponse, model, company, group, ipAddress,
						dataAccess);
				model = urlAndModel.getModelMap();
				nextJsp = urlAndModel.getUrl();
			}
		} catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
		}
		return new UrlAndModel(nextJsp, model);
	}

	public void buildAndRedirectSaml(HttpServletResponse httpResponse, CompanyDbObj company, GroupDbObj group,
			String ipAddress, SamlDataAccess dataAccess, String deviceId) throws Exception {
		SamlIdentityProviderDbObj samlIdp = dataAccess.getSamlIdpFromCompanyId(group.getCompanyId());
		if (samlIdp != null) {
			dataAccess.addLog("saml info found");
			Saml saml = new Saml();
			String authId = GeneralUtilities.randomString();
			AuthnRequest authnRequest = saml.buildAuthnRequest(samlIdp, company, authId, true);
			// not needed
			saml.authnRequestToString(authnRequest, samlIdp.isSignRequest());
			SamlAuthnRequestDbObj samlAuthnRequest = saml.buildAndSaveAuthnRequestObj(group, samlIdp, authId, "app",
					deviceId);
			redirectSamlRequestToIdp(httpResponse, authnRequest, samlAuthnRequest.getOutgoingRelayState(),
					samlIdp.getRedirectUrl());
		} else {
			dataAccess.addLog("samIdp null for coId: " + group.getCompanyId());
		}
	}

	private UrlAndModel buildAndRedirectLdap(HttpServletResponse httpResponse, ModelMap model, CompanyDbObj company,
			GroupDbObj group, String ipAddress, SamlDataAccess dataAccess) {
		Saml saml = new Saml();
		LdapServerDbObj ldapServer = dataAccess.getLdapServerFromCompany(company);
		dataAccess.addLog("ldapServer found");
		SamlAuthnRequestDbObj samlAuthn = saml.buildAndSaveAuthnRequestObjForLdapForInstallVerification(company, group,
				ldapServer, ipAddress, "app", dataAccess);
		Validate validate = new Validate();
		model = validate.showUsernameAndPwScreen(model, company, samlAuthn.getOutgoingRelayState(), dataAccess);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return new UrlAndModel("userPw", model);
	}

}
