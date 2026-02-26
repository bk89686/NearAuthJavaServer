package com.humansarehuman.blue2factor.authentication.browserSetup;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.authentication.serverAuth.FirstFactorSetup;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.UrlAndModel;
import com.humansarehuman.blue2factor.entities.enums.AuthorizationMethod;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(value = Urls.ONE_TIME_ACCESS)
public class OneTimeAccess extends BaseController {

	@RequestMapping(method = RequestMethod.GET)
	public Object oneTimeAccessProcessGet(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		String resp = "setupFailure";
		String companyId = this.getRequestValue(request, "company");
		this.printAllRequestParams(request);
		dataAccess.addLog("coId: " + companyId);
		if (!TextUtils.isBlank(companyId)) {
			CompanyDbObj company = dataAccess.getCompanyById(companyId);
			if (company != null) {
				UrlAndModel urlAndModel = signInForOneTimeAccess(request, httpResponse, company, model, dataAccess);
				model = urlAndModel.getModelMap();
				resp = urlAndModel.getUrl();
			}
		}
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return resp;
	}

	@RequestMapping(method = RequestMethod.POST)
	public Object oneTimeAccessProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		String resp = "setupFailure";
		String companyId = this.getRequestValue(request, "company");
		this.printAllRequestParams(request);
		dataAccess.addLog("OneTimeAccess", "coId: " + companyId);
		if (!TextUtils.isBlank(companyId)) {
			CompanyDbObj company = dataAccess.getCompanyById(companyId);
			if (company != null) {
				UrlAndModel urlAndModel = signInForOneTimeAccess(request, httpResponse, company, model, dataAccess);
				model = urlAndModel.getModelMap();
				resp = urlAndModel.getUrl();
			}
		}
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return resp;
	}

	private UrlAndModel signInForOneTimeAccess(HttpServletRequest request, HttpServletResponse httpResponse,
			CompanyDbObj company, ModelMap model, CompanyDataAccess dataAccess) {
		dataAccess.addLog("signInForOneTimeAccess", "start");
		String next = "";
		String deviceId = "";
		String browserId = "";
		try {

			String authToken = getPersistentToken(request);
			if (!TextUtils.isBlank(authToken)) {
				TokenDbObj token = dataAccess.getActiveTokenByOrDescriptionAndTokenId(TokenDescription.AUTHENTICATION,
						TokenDescription.ADMIN, authToken);
				if (token != null) {
					browserId = token.getBrowserId();
					deviceId = token.getDeviceId();
				}
			}

			AccessCodeDbObj newAccess = new AccessCodeDbObj(DateTimeUtilities.getCurrentTimestamp(),
					GeneralUtilities.randomString(), company.getCompanyId(), "", deviceId, 1, true, browserId, true);
			dataAccess.addAccessCode(newAccess, "signInForOneTimeAccess");
			if (company.getF1Method().equals(AuthorizationMethod.SAML)) {
				dataAccess.addLog("signInForOneTimeAccess", "company method is SAML");
				FirstFactorSetup firstFactorSetup = new FirstFactorSetup();
				firstFactorSetup.handleInitialSamlAuth(request, httpResponse, company, newAccess,
						company.getCompanyCompletionUrl(), "oneTimeAccess");
				next = null;
			} else {
				model.addAttribute("message1", "One time access is not allowed by your company");
				next = "failure";
			}

		} catch (Exception e) {
			dataAccess.addLog(e);
			next = "/genericFailure?src=" + company.getCompanyCompletionUrl();
		}
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return new UrlAndModel(next, model);
	}

}
