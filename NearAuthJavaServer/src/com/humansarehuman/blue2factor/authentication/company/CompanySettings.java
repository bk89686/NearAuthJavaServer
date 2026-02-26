package com.humansarehuman.blue2factor.authentication.company;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.AdminSignin;
import com.humansarehuman.blue2factor.entities.CompanyResponseHelper;
import com.humansarehuman.blue2factor.entities.enums.AuthorizationMethod;
import com.humansarehuman.blue2factor.entities.enums.NonMemberStrategy;
import com.humansarehuman.blue2factor.entities.tables.BrandingDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.LdapServerDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlServiceProviderDbObj;

@Controller
@RequestMapping(Urls.SETTINGS)
@SuppressWarnings("ucd")
public class CompanySettings extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String companySettingsGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		CompanyResponseHelper companyResponseHelper;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		AdminSignin adminSignin = checkPermission(request, httpResponse);
		if (adminSignin.isAllowed()) {
			updateCompanyPageCookie(httpResponse, adminSignin.getGroup());
			companyResponseHelper = this.showSettingsPage(adminSignin, request, httpResponse, model, dataAccess);
		} else {
			dataAccess.addLog("allowed");
			model.addAttribute("environment", Constants.ENVIRONMENT.toString());
			companyResponseHelper = new CompanyResponseHelper(httpResponse, model, "signinPage");
		}
		model.addAttribute("pageTitle", "NearAuth.ai Licensing");
		model.addAttribute("action", "/settings");
		httpResponse = addSecureHeaders(httpResponse);
		return companyResponseHelper.getNextPage();
	}

	@RequestMapping(method = RequestMethod.POST)
	public String companySettingsPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		AdminSignin adminSignin = checkPermission(request, httpResponse);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		String nextPage = "signinPage";
		SamlDataAccess dataAccess = new SamlDataAccess();
		try {
			if (adminSignin != null) {
				httpResponse = updateCompanyPageCookie(httpResponse, adminSignin.getGroup());
				CompanyResponseHelper companyResponseHelper = null;
				if (adminSignin.isAllowed()) {
					nextPage = "companySettings";
					String processCode = this.getRequestValue(request, "popupHidden1");
					httpResponse = updateCompanyPageCookie(httpResponse, adminSignin.getGroup());

					if (!TextUtils.isEmpty(processCode)) {
						dataAccess.addLog("Company", "will processCode " + processCode);
						Company company = new Company();
						try {
							company.handleProcess(request, httpResponse, model, processCode,
									adminSignin.getCompany().getCompanyId());
							if (processCode.equals("24598")) {
								AuthorizationMethod authMethod = dataAccess
										.stringToAuthMethod(this.getRequestValue(request, "popupHidden2"));
								this.showSettingsPageAfterUpdate(adminSignin, request, httpResponse, model, dataAccess,
										authMethod);
							} else {
								companyResponseHelper = this.showSettingsPage(adminSignin, request, httpResponse, model,
										dataAccess);
							}
						} catch (Exception e) {
							dataAccess.addLog(e);
						}
					} else {
						companyResponseHelper = this.showSettingsPage(adminSignin, request, httpResponse, model,
								dataAccess);
						dataAccess.addLog("showSettingsReturn ");
					}
					if (companyResponseHelper != null) {
						model = companyResponseHelper.getModel();
						httpResponse = companyResponseHelper.getHttpResponse();
					}
					model.addAttribute("secureUrl", Urls.SECURE_URL);
					model = addServiceProviders(dataAccess, model, adminSignin.getCompany());
				}
				dataAccess.addLog("nextPage: " + nextPage);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return nextPage;
	}

	private ModelMap addServiceProviders(SamlDataAccess dataAccess, ModelMap model, CompanyDbObj company) {
		ArrayList<SamlServiceProviderDbObj> serviceProviders = dataAccess
				.getSamlServiceProvidersbyCompanyId(company.getCompanyId());
		model.addAttribute("serviceProviders", serviceProviders);
		return model;
	}

	private CompanyResponseHelper showSettingsPageAfterUpdate(AdminSignin adminSignin, HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, CompanyDataAccess dataAccess,
			AuthorizationMethod f1Method) {
		CompanyDbObj company = adminSignin.getCompany();
		if (company != null) {
			GroupDbObj group = adminSignin.getGroup();
			model = getLicenseAttributes(dataAccess, model, group, company);
			model = getLdapAttributes(dataAccess, model, company);
			model = setFactor1(dataAccess, model, f1Method, company.getNonMemberStrategy());
			model = setBranding(dataAccess, model, company);
		}
		model.addAttribute("secureUrl", Urls.SECURE_URL);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return new CompanyResponseHelper(httpResponse, model, "companySettings");
	}

	CompanyResponseHelper showSettingsPage(AdminSignin adminSignin, HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, CompanyDataAccess dataAccess) {
		CompanyDbObj company = adminSignin.getCompany();
		dataAccess.addLog("show settings page");
		if (company != null) {
			GroupDbObj group = adminSignin.getGroup();
			model = getLicenseAttributes(dataAccess, model, group, company);
			model = getLdapAttributes(dataAccess, model, company);
			model = setFactor1(dataAccess, model, company);
			model = setBranding(dataAccess, model, company);
			model.addAttribute("groupId", group.getGroupId());

		}
		model.addAttribute("secureUrl", Urls.SECURE_URL);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return new CompanyResponseHelper(httpResponse, model, "companySettings");
	}

	private ModelMap getLdapAttributes(CompanyDataAccess dataAccess, ModelMap model, CompanyDbObj company) {
		LdapServerDbObj ldapServer = dataAccess.getLdapServerFromCompany(company);
		if (ldapServer != null) {
			if (ldapServer.getProviderUrl() != null) {
				model.addAttribute("providerUrl", ldapServer.getProviderUrl());
			}
			if (ldapServer.getProviderUrl() != null) {
				model.addAttribute("searchBase", ldapServer.getSearchBase());
			}
			model.addAttribute("jksUploaded", !TextUtils.isEmpty(ldapServer.getJksFile()));
			model.addAttribute("jksPwUploaded", !TextUtils.isEmpty(ldapServer.getJksPassword()));
		}
		return model;
	}

	private ModelMap setFactor1(CompanyDataAccess dataAccess, ModelMap model, CompanyDbObj company) {
		return setFactor1(dataAccess, model, company.getF1Method(), company.getNonMemberStrategy());
	}

	private ModelMap setFactor1(CompanyDataAccess dataAccess, ModelMap model, AuthorizationMethod authMethod,
			NonMemberStrategy nonMemberStrategy) {
		String f1Method_ldapSelected = "";
		String f1Method_samlSelected = "";
		String f1Method = "";
		if (authMethod.equals(AuthorizationMethod.SAML)) {
			f1Method_samlSelected = "selected";
			f1Method = "saml";
		} else if (authMethod.equals(AuthorizationMethod.LDAP)) {
			f1Method_ldapSelected = "selected";
			f1Method = "ldap";
		}
		dataAccess.addLog("f1Method: " + f1Method);
		model.addAttribute("f1Method_ldapSelected", f1Method_ldapSelected);
		model.addAttribute("f1Method_samlSelected", f1Method_samlSelected);
		model.addAttribute("f1Method", f1Method);

		String noConsoleInSelected = "";
		String noConsoleOutSelected = "";
		String noDeviceInSelected = "";
		String noDeviceOutSelected = "";

		if (nonMemberStrategy == NonMemberStrategy.ALLOW_NOT_SIGNED_UP
				|| nonMemberStrategy == NonMemberStrategy.ALLOW_NOT_SIGNED_UP_OR_NO_DEVICE) {
			noConsoleInSelected = "checked";
		} else {
			noConsoleOutSelected = "checked";
		}

		if (nonMemberStrategy == NonMemberStrategy.ALLOW_NO_DEVICE
				|| nonMemberStrategy == NonMemberStrategy.ALLOW_NOT_SIGNED_UP_OR_NO_DEVICE) {
			noDeviceInSelected = "checked";
		} else {
			noDeviceOutSelected = "checked";
		}

		model.addAttribute("noConsoleInSelected", noConsoleInSelected);
		model.addAttribute("noConsoleOutSelected", noConsoleOutSelected);
		model.addAttribute("noDeviceInSelected", noDeviceInSelected);
		model.addAttribute("noDeviceOutSelected", noDeviceOutSelected);

		return model;
	}

	private ModelMap setBranding(CompanyDataAccess dataAccess, ModelMap model, CompanyDbObj company) {
		String iconUrl = "";
		String foregroundColor = "";
		String backgroundColor = "";
		BrandingDbObj branding = dataAccess.getBranding(company);
		if (branding != null) {
			iconUrl = branding.getIconPath();
			foregroundColor = branding.getForegroundColor();
			backgroundColor = branding.getBackgroundColor();
		}
		model.addAttribute("backgroundColor", backgroundColor);
		model.addAttribute("primaryColor", foregroundColor);
		model.addAttribute("iconUrl", iconUrl);
		return model;
	}
}
