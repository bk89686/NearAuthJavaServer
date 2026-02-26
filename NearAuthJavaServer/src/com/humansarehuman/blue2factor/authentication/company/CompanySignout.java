package com.humansarehuman.blue2factor.authentication.company;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.CompanyResponseHelper;

@Controller
@RequestMapping(Urls.COMPANY_SIGNOUT)
@SuppressWarnings("ucd")
public class CompanySignout extends Company {

	CompanyDataAccess dataAccess = new CompanyDataAccess();

	@Override
	@RequestMapping(method = RequestMethod.GET)
	public String companyPageProcessGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		dataAccess.addLog("companySignoutPage", "entry", LogConstants.DEBUG);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		CompanyResponseHelper companyResponseHelper = new CompanyResponseHelper(httpResponse, model, "signinPage");
		this.setMainGroupCookie(httpResponse, "");
		model.addAttribute("pageTitle", "NearAuth.ai Licensing");
		model.addAttribute("action", "/company");
		httpResponse = addSecureHeaders(httpResponse);
		return companyResponseHelper.getNextPage();
	}

}
