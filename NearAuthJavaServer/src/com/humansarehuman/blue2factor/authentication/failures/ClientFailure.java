package com.humansarehuman.blue2factor.authentication.failures;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.serverAuth.FirstFactorSetup;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.UrlAndModel;
import com.humansarehuman.blue2factor.entities.enums.NonMemberStrategy;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(Urls.CLIENT_FAILURE)
@SuppressWarnings("ucd")
public class ClientFailure extends Failure {
	@RequestMapping(method = RequestMethod.GET)
	public Object clientFailureProcessGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			@PathVariable("CompanyID") String apiKey) {
		int logLevel = LogConstants.TRACE;
		String nextPage = "needsResync";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		CompanyDbObj company = dataAccess.getCompanyByApiKey(apiKey);
		if (company != null) {
			model.addAttribute("company", company.getCompanyId());
		}
		dataAccess.addLog("failure reported", logLevel);
		try {
			String url = this.getRequestValue(request, "url");
			request = setReferrer(request, url);
			String authToken = getPersistentToken(request);
			dataAccess.addLog("authToken: " + authToken, logLevel);
			IdentityObjectFromServer idObj = null;
			if (!TextUtils.isEmpty(authToken) && !authToken.equals("null")) {
				idObj = this.getIdObj(authToken, company, dataAccess);
			}
			if (idObj == null || idObj.getBrowser() == null || isTempBrowser(idObj.getBrowser())) {
				dataAccess.addLog("authToken not found", logLevel);
				model.addAttribute("environment", Constants.ENVIRONMENT.toString());
				if (company != null) {
					NonMemberStrategy nms = company.getNonMemberStrategy();
					boolean newlySignedUpUsers = dataAccess.areThereNewlySignedUpUsers(company);
					if (nms != NonMemberStrategy.ALLOW_AUTHENTICATED_ONLY || newlySignedUpUsers) {
						AccessCodeDbObj newAccess = new AccessCodeDbObj(DateTimeUtilities.getCurrentTimestamp(),
								GeneralUtilities.randomString(), company.getCompanyId(), "", "", 0, true, "", false);
						dataAccess.addAccessCode(newAccess, "clientFailureProcessGet");
						FirstFactorSetup firstFactorSetup = new FirstFactorSetup();
						firstFactorSetup.handleInitialSamlAuth(request, httpResponse, company, newAccess,
								company.getCompanyCompletionUrl(), "setup");
						dataAccess.addLog("we are redirecting based on the non-member strategy");
						nextPage = null;
					}
				}
				nextPage = "notSignedUp";
			} else {
				dataAccess.addLog("browser was found", logLevel);
				UrlAndModel urlAndModel = this.handleFailureWithIdObj(request, httpResponse, model, idObj, dataAccess,
						authToken, "");
				nextPage = urlAndModel.getUrl();
				model = urlAndModel.getModelMap();
			}
		} catch (Exception e) {
			dataAccess.addLog("ClientFailure", e);
			model.addAttribute("message", e.getMessage());
		}
		dataAccess.addLog("nextPage: " + nextPage, logLevel);
		dataAccess.addLog("forward to: " + model.getAttribute("submitUrl"), logLevel);
		return nextPage;
	}
}
