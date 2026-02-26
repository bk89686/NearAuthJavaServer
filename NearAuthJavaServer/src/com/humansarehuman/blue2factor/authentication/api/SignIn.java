package com.humansarehuman.blue2factor.authentication.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.serverAuth.FirstFactorSetup;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.enums.AuthorizationMethod;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(value = Urls.SIGN_IN)
public class SignIn extends B2fApi {
	@RequestMapping(method = RequestMethod.GET)
	public String signInProcessGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			@PathVariable("apiKey") String apiKey) {
		SamlDataAccess dataAccess = new SamlDataAccess();
		String next = "failure";
		IdentityObjectFromServer idObj = this.getIdentityObjectFromCookie(request, apiKey);
		if (idObj != null) {
			dataAccess.addLog("idObj found");
			CompanyDbObj company = idObj.getCompany();
			BrowserDbObj browser = idObj.getBrowser();
			DeviceDbObj device = idObj.getDevice();
			if (company != null && browser != null && device != null) {
				dataAccess.addLog("browser, device, and company found");
				if (company.getF1Method().equals(AuthorizationMethod.SAML)) {
					AccessCodeDbObj newAccess = new AccessCodeDbObj(DateTimeUtilities.getCurrentTimestamp(),
							GeneralUtilities.randomString(), company.getCompanyId(), "", device.getDeviceId(), 0, true,
							browser.getBrowserId(), false);
					dataAccess.addAccessCode(newAccess, "SignIn");
					FirstFactorSetup firstFactorSetup = new FirstFactorSetup();
					try {
						if (firstFactorSetup.handleSamlAuth(request, httpResponse, idObj, newAccess,
								company.getCompanyCompletionUrl(), "signIn")) {
							next = handleSignupSecondFactor(request, httpResponse, model, idObj, dataAccess);
						} else {
							next = null; // because they were redirected;
						}
					} catch (Exception e) {
						dataAccess.addLog(e);
					}

				} else {
					model.addAttribute("message1", "Your company does not currently allow this");
				}
			} else {
				model.addAttribute("message1", "");
			}
		} else {
			dataAccess.addLog("idObj NOT found");
			model.addAttribute("message1",
					"Either you do not have NearAuth.ai installed, or you need to resynch your browser.");
		}
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return next;
	}

}
