package com.humansarehuman.blue2factor.authentication.company;

import java.util.ArrayList;

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
import com.humansarehuman.blue2factor.entities.tables.SamlServiceProviderDbObj;

@Controller
@RequestMapping(Urls.SERVICE_PROVIDERS)
@SuppressWarnings("ucd")
public class ServiceProviders extends BaseController {
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
		model = getServiceProviders(dataAccess, model, company);
		model = this.getLicenseAttributes(dataAccess, model, group, company);

		return "companyServiceProviders";
	}

	public ModelMap getServiceProviders(SamlDataAccess dataAccess, ModelMap model, CompanyDbObj company) {
		ArrayList<SamlServiceProviderDbObj> serviceProviders = dataAccess
				.getSamlServiceProvidersbyCompanyId(company.getCompanyId());
		dataAccess.addLog(serviceProviders.size() + " records found");
		model.addAttribute("serviceProviders", serviceProviders);
		model.addAttribute("serviceProviderCount", serviceProviders.size());
		return model;
	}

	public CompanyResponseHelper showServiceProviderPage(AdminSignin adminSignin, HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, SamlDataAccess dataAccess) {
		CompanyDbObj company = adminSignin.getCompany();
		dataAccess.addLog("show service provider page");
		try {
			if (company != null) {
				GroupDbObj group = adminSignin.getGroup();
				if (group != null) {
					model = getServiceProviders(dataAccess, model, company);
					model.addAttribute("licensesInUse", company.getLicensesInUse());
					model.addAttribute("licenseCount", company.getLicenseCount());
					model.addAttribute("noDevicesRegistered", (group.getDevicesInUse() == 0));
				} else {
					dataAccess.addLog("group was null");
				}
			} else {
				dataAccess.addLog("company was null");
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		model.addAttribute("secureUrl", Urls.SECURE_URL);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return new CompanyResponseHelper(httpResponse, model, "companyServiceProviders");
	}
}
