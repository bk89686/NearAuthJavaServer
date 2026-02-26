package com.humansarehuman.blue2factor.authentication.serverAuth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.saml.SamlAndLdapResponse;
import com.humansarehuman.blue2factor.authentication.saml.Validate;
import com.humansarehuman.blue2factor.communication.Ldap;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.UrlAndModel;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlAuthnRequestDbObj;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.LDAP_SUBMIT)
@SuppressWarnings("ucd")
public class LdapSubmit extends SamlAndLdapResponse {
	boolean testing = false;

	@RequestMapping(method = RequestMethod.POST)
	public String ldapSubmitPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			@PathVariable("apiKey") String apiKey) {
		String nextPage = "notSignedUp";
		SamlDataAccess dataAccess = new SamlDataAccess();
		CompanyDbObj company = dataAccess.getCompanyByApiKey(apiKey);
		String requestId = getRequestValue(request, "relayState");
		try {
			if (company != null) {
				String username = this.getRequestValue(request, "username");
				String pw = this.getRequestValue(request, "pw");
				SamlAuthnRequestDbObj samlAuthnRequest = dataAccess.getAuthRequestByOutgoingRelayState(requestId);
				Ldap ldap = new Ldap();
				if (ldap.validateLdap(username, pw, company)) {
					dataAccess.addLog("validated");
					String referrer;
					IdentityObjectFromServer idObj = this.getIdentityObjectFromCookie(request, company.getApiKey());
					GroupDbObj group = dataAccess.getGroupByEmail(username);
					if (group != null) {
						dataAccess.addLog("group found");
						String sender = "";
						if (samlAuthnRequest != null) {
							sender = samlAuthnRequest.getSender();
							dataAccess.addLog("sender: " + sender);
						}
						if (!TextUtils.isEmpty(requestId)) {
							if (sender.equals("setup") || sender.equals("app")) {
								UrlAndModel urlAndModel = getSetupRedirect(model, Outcomes.SUCCESS, username,
										dataAccess, idObj, "", null);
								samlAuthnRequest.setOutcome(Outcomes.SUCCESS);
								dataAccess.updateSamlAuthnRequestOutcome(samlAuthnRequest, Outcomes.SUCCESS);
								nextPage = urlAndModel.getUrl();
								model = urlAndModel.getModelMap();
							} else {
								OutcomeRequestResponseNextPageIdObjEmailAndModel outcomeRequestResponseAndNextPage;
								String ipAddress = GeneralUtilities.getClientIp(request);
								if (samlAuthnRequest != null) {
									referrer = samlAuthnRequest.getIncomingAcsUrl();
									outcomeRequestResponseAndNextPage = this.respondToServiceProviderFromLdap(request,
											httpResponse, model, samlAuthnRequest, company, ipAddress, username,
											dataAccess);
									UrlAndModel urlAndModel = this.respondToSp(outcomeRequestResponseAndNextPage.model,
											samlAuthnRequest, group, idObj.getCompany().getApiKey(),
											idObj.getCompany().getEntityIdVal(), Outcomes.SUCCESS);
									nextPage = urlAndModel.getUrl();
									model = urlAndModel.getModelMap();
								} else {
									outcomeRequestResponseAndNextPage = this.handleNullSamlAuthnRequestFromLdap(request,
											httpResponse, model, company, apiKey, ipAddress, username, dataAccess);
									nextPage = outcomeRequestResponseAndNextPage.nextPage;
									model = outcomeRequestResponseAndNextPage.model;
								}
								httpResponse = outcomeRequestResponseAndNextPage.httpServletResponse;
								request = outcomeRequestResponseAndNextPage.httpServletRequest;
								model.addAttribute(ipAddress);
							}
						} else {
							referrer = this.getCookie(request, "referrer");
							if (TextUtils.isBlank(referrer)) {
								referrer = company.getCompleteCompanyLoginUrl();
							}
						}
					} else {
						// see if people are allowed without validation
					}
					// buildSamlResponse
					// check second factor based on username

					// go to setup

				} else {
					model = new Validate().showUsernameAndPwScreen(model, company,
							samlAuthnRequest.getOutgoingRelayState(), dataAccess,
							"please double check your email and password");
					model.addAttribute("environment", Constants.ENVIRONMENT.toString());
					nextPage = "userPw";
				}
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return nextPage;
	}
}
