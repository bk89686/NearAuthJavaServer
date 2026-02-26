package com.humansarehuman.blue2factor.authentication.company;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.AdminSignin;
import com.humansarehuman.blue2factor.entities.CompanyResponseHelper;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;

@Controller
@RequestMapping(Urls.ADD_USERS_DOC)
@SuppressWarnings("ucd")
public class AddUserDocumentation extends BaseController {
	private DataAccess dataAccess = new DataAccess();

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		dataAccess.addLog("companySigninPage", "entry", LogConstants.DEBUG);
		String incorrect = this.getRequestValue(request, "incorrect");
		CompanyResponseHelper companyResponseHelper;
		if (incorrect != null && incorrect.equals("1")) {
			companyResponseHelper = showPasswordIncorrect(httpResponse, model);
			model = companyResponseHelper.getModel();
		} else {
			AdminSignin adminSignin = checkPermission(request, httpResponse);
			if (adminSignin.isAllowed()) {
				updateCompanyPageCookie(httpResponse, adminSignin.getGroup());
				companyResponseHelper = this.showAddUsersPage(adminSignin.getGroup(), request, httpResponse, model);
			} else {
				dataAccess.addLog("Documentation", "allowed");
				model.addAttribute("environment", Constants.ENVIRONMENT.toString());
				companyResponseHelper = new CompanyResponseHelper(httpResponse, model, "signinPage");
			}
		}
		model.addAttribute("pageTitle", "NearAuth.ai - Adding Users");
		model.addAttribute("action", "/addUsersDoc");
		httpResponse = addSecureHeaders(httpResponse);
		return companyResponseHelper.getNextPage();
	}

	@RequestMapping(method = RequestMethod.POST)
	public String addUserDocumentationProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		CompanyResponseHelper companyResponseHelper;
		AdminSignin adminSignin = checkPermission(request, httpResponse);
		if (adminSignin.isAllowed()) {
			updateCompanyPageCookie(httpResponse, adminSignin.getGroup());
			dataAccess.addLog("allowed");
			companyResponseHelper = this.showAddUsersPage(adminSignin.getGroup(), request, httpResponse, model);
		} else {
			dataAccess.addLog("not allowed");
			model.addAttribute("environment", Constants.ENVIRONMENT.toString());
			companyResponseHelper = new CompanyResponseHelper(httpResponse, model, "signinPage");
		}
		model = companyResponseHelper.getModel();
		model.addAttribute("pageTitle", "NearAuth.ai - Adding Users");
		model.addAttribute("action", "/documentation");
		httpResponse = companyResponseHelper.getHttpResponse();
		httpResponse = addSecureHeaders(httpResponse);
		return companyResponseHelper.getNextPage();
	}

	public CompanyResponseHelper showAddUsersPage(GroupDbObj group, HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model) {
		model = getPageAttributes(model, group);
		model.addAttribute("secureUrl", Urls.SECURE_URL);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		String pageName = "addUsersDoc";
		return new CompanyResponseHelper(httpResponse, model, pageName);
	}
}
