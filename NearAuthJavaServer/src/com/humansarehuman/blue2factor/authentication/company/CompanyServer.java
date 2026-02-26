package com.humansarehuman.blue2factor.authentication.company;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.AdminSignin;
import com.humansarehuman.blue2factor.entities.CompanyResponseHelper;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;

@Controller
@RequestMapping(value = { Urls.SERVER, Urls.SERVERS })
@SuppressWarnings("ucd")
public class CompanyServer extends BaseController {

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	public String companyServerProcessGet(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		SamlDataAccess dataAccess = new SamlDataAccess();
		AdminSignin adminSignin = checkPermission(request, httpResponse);
		GroupDbObj group = null;
		CompanyDbObj company = null;
		if (adminSignin.isAllowed()) {
			httpResponse = updateCompanyPageCookie(httpResponse, adminSignin.getGroup());
			group = adminSignin.getGroup();
			company = adminSignin.getCompany();
			model.addAttribute("userRole", group.getUserType());
		} else {
			model.addAttribute("environment", Constants.ENVIRONMENT.toString());
			CompanyResponseHelper companyResponseHelper = new CompanyResponseHelper(httpResponse, model, "signinPage");
			model.addAttribute("action", "/servers");
			return companyResponseHelper.getNextPage();
		}
		model = getServerAttributes(dataAccess, model, company);
		model = getServerPageAttributes(model, group);
		model = this.getLicenseAttributes(dataAccess, model, group, company);

		return "companyServer";
	}
}
