package com.humansarehuman.blue2factor.authentication.company;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Enumeration;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.communication.Internet;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.AdminSignin;
import com.humansarehuman.blue2factor.entities.CompanyResponseHelper;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.enums.UserType;
import com.humansarehuman.blue2factor.entities.tables.BrandingDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.LdapServerDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlIdentityProviderDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlServiceProviderDbObj;
import com.humansarehuman.blue2factor.entities.tables.ServerDbObj;
import com.humansarehuman.blue2factor.utilities.Converters;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.humansarehuman.blue2factor.utilities.JsonUtilities;

import net.shibboleth.utilities.java.support.xml.BasicParserPool;

@Controller
@RequestMapping(value = { "", "/", Urls.COMPANY })
@SuppressWarnings("ucd")
public class Company extends BaseController {

	private SamlDataAccess dataAccess = new SamlDataAccess();

	@RequestMapping(method = RequestMethod.GET)
	public String companyPageProcessGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		dataAccess.addLog("entry", LogConstants.TRACE);
		String incorrect = this.getRequestValue(request, "incorrect");
		CompanyResponseHelper companyResponseHelper;
		try {
			if (incorrect != null && incorrect.equals("1")) {
				companyResponseHelper = showPasswordIncorrect(httpResponse, model);
				model = companyResponseHelper.getModel();
			} else {
				AdminSignin adminSignin = checkPermission(request, httpResponse);
				if (adminSignin.isAllowed()) {
					dataAccess.addLog("adminAllowed", LogConstants.TRACE);
					if (dataAccess.getSamlServiceProviderbyCompanyId(adminSignin.getCompany().getCompanyId()) == null) {
						addServiceProvider(adminSignin.getCompany().getCompanyId());
						addCompanyIdpKeys(adminSignin.getCompany().getCompanyId());
					}
					companyResponseHelper = this.showCompanyPage(adminSignin.getGroup(), request, httpResponse, model);
				} else {
					dataAccess.addLog("adminAllowed", LogConstants.TRACE);
					model.addAttribute("environment", Constants.ENVIRONMENT.toString());
					companyResponseHelper = new CompanyResponseHelper(httpResponse, model, "signinPage");
				}
			}
			model.addAttribute("pageTitle", "NearAuth.ai Licensing");
			model.addAttribute("action", "/company");

			httpResponse = addSecureHeaders(httpResponse);
		} catch (Exception e) {
			dataAccess.addLog(e);
			return "error";
		}
		return companyResponseHelper.getNextPage();
	}

	boolean adminTesting = false;

	@RequestMapping(method = RequestMethod.POST)
	@SuppressWarnings("ucd")
	public String companyPageProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model)
			throws IOException {
		int logLevel = LogConstants.TRACE;
		AdminSignin adminSignin = checkPermission(request, httpResponse);
		String nextPage;
		CompanyResponseHelper companyResponseHelper = null;
		try {
			if (adminSignin != null) {
				dataAccess.addLog("isAccessAllowed: " + adminSignin.isAllowed(), logLevel);
				dataAccess.addLog("isAdmin: " + adminSignin.isAdmin());
				if (adminSignin.isAllowed() || (adminTesting && adminSignin.getGroup() != null
						&& adminSignin.getGroup().getGroupName().equals("chris@blue2factor.com"))) {
					String processCode = this.getRequestValue(request, "popupHidden1");
					httpResponse = updateCompanyPageCookie(httpResponse, adminSignin.getGroup());
					if (!TextUtils.isEmpty(processCode)) {
						dataAccess.addLog("will processCode " + processCode, logLevel);
						companyResponseHelper = handleProcess(request, httpResponse, model, processCode,
								adminSignin.getCompany().getCompanyId());
					} else {
						dataAccess.addLog("allowed");
						if (dataAccess
								.getSamlServiceProviderbyCompanyId(adminSignin.getCompany().getCompanyId()) == null) {
							dataAccess.addLog("add sp");
							addCompanyIdpKeys(adminSignin.getCompany().getCompanyId());
						} else {
							dataAccess.addLog("sp exists");
							addCompanyIdpKeysIfNeeded(adminSignin.getCompany().getCompanyId());
						}
						companyResponseHelper = this.showCompanyPage(adminSignin.getGroup(), request, httpResponse,
								model);
					}
				} else {
					dataAccess.addLog("not allowed", logLevel);
					model.addAttribute("environment", Constants.ENVIRONMENT.toString());
					companyResponseHelper = new CompanyResponseHelper(httpResponse, model, "signinPage");
					model.addAttribute("errorMessage", adminSignin.getReason());
				}
				model = companyResponseHelper.getModel();
				model.addAttribute("pageTitle", "NearAuth.ai Licensing");
				model.addAttribute("action", "/company");
				httpResponse = companyResponseHelper.getHttpResponse();
				nextPage = companyResponseHelper.getNextPage();
				dataAccess.addLog("next: " + nextPage);
			} else {
				nextPage = "/company&incorrect=1";
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			return "";
		}
		httpResponse = addSecureHeaders(httpResponse);
		return nextPage;
	}

	@SuppressWarnings("unused")
	private String useB2fSignin(String token, String session) {
		String newToken = null;
		DataAccess dataAccess = new DataAccess();
		JsonUtilities jsonUtil = new JsonUtilities();
		JSONObject proxJson = jsonUtil.getProxJson(token, session);
		dataAccess.addLog("sending: " + proxJson.toString(4));
		Internet internet = new Internet();
		try {
			JSONObject resp = internet.sendApiJson(proxJson);
			if (resp != null) {
				dataAccess.addLog("resp: " + resp);
				Integer outcome = (Integer) resp.get("outcome");
				if (outcome == 0) {
					newToken = (String) resp.get("token");
					JSONObject expireJson = jsonUtil.getExpireJson(newToken);
					internet.sendApiJson(expireJson);
				}
			}
		} catch (JSONException e) {
			dataAccess.addLog("useB2fSignin", e);
		}
		return newToken;
	}

	public CompanyResponseHelper handleProcess(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model, String processCode, String companyId) throws IOException {
		dataAccess.addLog("processCode: " + processCode);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		CompanyResponseHelper companyResponseHelper = new CompanyResponseHelper(httpResponse, model, "signinPage");
		String newBaseUrl;
		String tableId;
		try {
			this.handleRequest(request);
			switch (processCode) {
			case "11839":// reset devices
				String groupToRemoveDevices = this.getRequestValue(request, "popupHidden2");
				companyResponseHelper = removeDevicesAndShow(companyResponseHelper, request, httpResponse, model,
						groupToRemoveDevices);
				break;
			case "14839":// remove user
				String groupToRemove = this.getRequestValue(request, "popupHidden2");
				companyResponseHelper = this.removeGroupAndShowPage(companyResponseHelper, request, httpResponse, model,
						groupToRemove);
				break;
			case "18013":// change role
				String newRole = this.getRequestValue(request, "role");
				String roleGroupId = this.getRequestValue(request, "popupHidden2");
				companyResponseHelper = updateUserRole(companyResponseHelper, request, httpResponse, model, newRole,
						roleGroupId);
				break;
			case "18973":// add uid
				String newUid = this.getRequestValue(request, "popupInput1");
				String userGroupId = this.getRequestValue(request, "popupHidden2");
				companyResponseHelper = updateUserUid(companyResponseHelper, request, httpResponse, model, newUid,
						userGroupId);
				break;
			case "19923":// update base url
				newBaseUrl = this.getRequestValue(request, "popupInput1");
				companyResponseHelper = updateBaseUrl(companyResponseHelper, request, httpResponse, model, newBaseUrl);
				break;
			case "19924":// update base url
				newBaseUrl = this.getRequestValue(request, "popupInput1");
				dataAccess.addLog("newBase: " + newBaseUrl);
				companyResponseHelper = updateCompletionUrl(companyResponseHelper, request, httpResponse, model,
						newBaseUrl);
				companyResponseHelper = updateBaseUrl(companyResponseHelper, request, httpResponse, model, newBaseUrl);
				break;
			case "19925":// update login url
				newBaseUrl = this.getRequestValue(request, "popupInput1");
				dataAccess.addLog("newLoginUrl: " + newBaseUrl, LogConstants.TRACE);
				companyResponseHelper = updateLoginUrl(companyResponseHelper, request, httpResponse, model, newBaseUrl);
				break;
			case "23998":// delete server
				String serverToDelete = this.getRequestValue(request, "popupHidden2");
				ServerDbObj server = dataAccess.getServerByB2fId(serverToDelete);
				if (server != null) {
					server.setActive(false);
					dataAccess.updateServer(server);
				}
				String groupId = getCookie(request, "gid");
				GroupDbObj group = dataAccess.getGroupByToken(groupId);
				companyResponseHelper = showServerPage(group, request, httpResponse, model);
				break;
			case "34339": // change ldap search base
				String newSearchBase = this.getRequestValue(request, "popupInput1");
				companyResponseHelper = this.addSearchBase(request, httpResponse, newSearchBase, model);
				break;
			case "34525": // change ldap url
				String newLdapUrl = this.getRequestValue(request, "popupInput1");
				companyResponseHelper = this.updateLdapUrl(request, httpResponse, newLdapUrl, model);
				break;
			case "34342": // change foreground
				String newFg = this.getRequestValue(request, "popupInput1");
				companyResponseHelper = this.updateForegroundColor(request, httpResponse, newFg, model);
				break;
			case "34343": // change background
				String newBg = this.getRequestValue(request, "popupInput1");
				companyResponseHelper = this.updateBackgroundColor(request, httpResponse, newBg, model);
				break;
			case "34344": // change icon url
				String newIcon = this.getRequestValue(request, "popupInput1");
				companyResponseHelper = this.updateIconUrl(request, httpResponse, newIcon, model);
				break;
			case "88952":// add new user
				String addUserCompany = this.getRequestValue(request, "popupHidden2");
				String userName = this.getRequestValue(request, "popupInput1");
				String email = this.getRequestValue(request, "popupInput2");
				String uid = this.getRequestValue(request, "popupInput3");
				companyResponseHelper = addUserAndShowPage(companyResponseHelper, request, addUserCompany, httpResponse,
						model, userName, email, uid);
				break;
			case "98399":// add service provider
				String serviceProviderName = this.getRequestValue(request, "popupInput1");
				String entityId = this.getRequestValue(request, "popupInput2");
				String acsUrl = this.getRequestValue(request, "popupInput3");
				companyResponseHelper = this.addServiceProviderAndShowPage(companyResponseHelper, request, companyId,
						httpResponse, model, serviceProviderName, entityId, acsUrl);
				break;
			case "23903":// add new server
				String addServerCompany = this.getRequestValue(request, "popupHidden2");
				String serverName = this.getRequestValue(request, "popupInput1");
				String serverCode = this.getRequestValue(request, "popupInput2");
				String description = this.getRequestValue(request, "popupInput3");
				companyResponseHelper = addServerAndShowPage(companyResponseHelper, request, addServerCompany,
						serverName, httpResponse, model, serverCode, description);
				break;
			case "24598":// change f1 method
				String f1Method = this.getRequestValue(request, "popupHidden2");
				companyResponseHelper = updateF1Method(companyResponseHelper, request, httpResponse, model, f1Method);
				break;
			case "24599":// change f2 method
				String f2Method = this.getRequestValue(request, "popupHidden2");
				companyResponseHelper = updateF2Method(companyResponseHelper, request, httpResponse, model, f2Method);
				break;
			case "238989":// new saml metadata
				String metadata = this.getRequestValue(request, "popupTextArea").replace("\n", "").replace("\r", "");
				dataAccess.addLog("metadata: " + metadata);
				companyResponseHelper = updateSamlMetadata(companyResponseHelper, request, httpResponse, model,
						metadata);
				break;
			case "298675": // new web server public key
				String publicKey = this.getRequestValue(request, "popupTextArea").replace("\n", "").replace("\r", "");
				publicKey = publicKey.replaceAll("-----END PUBLIC KEY-----", "")
						.replaceAll("-----BEGIN PUBLIC KEY-----", "");
				dataAccess.addLog("publicKey: " + publicKey);
				companyResponseHelper = updateWebServerPublicKey(companyResponseHelper, request, httpResponse, model,
						publicKey, companyId);
				break;
			case "299866": // edit Service provider
				String spName = this.getRequestValue(request, "popupInput1");
				String spEntityId = this.getRequestValue(request, "popupInput2");
				String spAcsUrl = this.getRequestValue(request, "popupInput3");
				tableId = this.getRequestValue(request, "popupHidden2");
				companyResponseHelper = editServiceProvider(request, httpResponse, model, spName, spEntityId, spAcsUrl,
						tableId);
				break;
			case "299867": // upload service provider metadata
				String spMetadata = this.getRequestValue(request, "popupTextArea").replace("\n", "").replace("\r", "");
				dataAccess.addLog("metadata: " + spMetadata);
				companyResponseHelper = addServiceProviderMetadata(companyResponseHelper, request, httpResponse, model,
						spMetadata);
				break;

			case "299877": // delete Service provider
				tableId = this.getRequestValue(request, "popupHidden2");
				companyResponseHelper = deleteServiceProvider(request, httpResponse, model, tableId);
				break;
			}
		} catch (Exception e) {
			dataAccess.addLog("handleProcess", e);
		}
		return companyResponseHelper;
	}

	private CompanyResponseHelper deleteServiceProvider(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model, String tableId) {
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		if (adminSignin.isAllowed()) {
			dataAccess.deleteServiceProvider(tableId);
			model = this.getLicenseAttributes(dataAccess, model, adminSignin.getGroup(), adminSignin.getCompany());
		}
		ServiceProviders serviceProviders = new ServiceProviders();

		return serviceProviders.showServiceProviderPage(adminSignin, request, httpResponse, model, dataAccess);
	}

	private CompanyResponseHelper editServiceProvider(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model, String spName, String spEntityId, String spAcsUrl, String tableId) {
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		if (adminSignin.isAllowed()) {
			dataAccess.addLog("is allowed");
			SamlServiceProviderDbObj serviceProvider = dataAccess.getServiceProviderByTableId(tableId);
			dataAccess.addLog("serviceProvider = null ? " + (serviceProvider == null));
			if (serviceProvider != null) {
				dataAccess.addLog("serviceProvider found");
				serviceProvider.setServiceProviderName(spName);
				serviceProvider.setAcsUrl(spAcsUrl);
				serviceProvider.setServiceProviderEntityId(spEntityId);
				dataAccess.updateServiceProvider(serviceProvider);
			}
			model = this.getLicenseAttributes(dataAccess, model, adminSignin.getGroup(), adminSignin.getCompany());
		}
		ServiceProviders serviceProviders = new ServiceProviders();
		return serviceProviders.showServiceProviderPage(adminSignin, request, httpResponse, model, dataAccess);
	}

	private CompanyResponseHelper updateLdapUrl(HttpServletRequest request, HttpServletResponse httpResponse,
			String ldapUrl, ModelMap model) {
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		if (adminSignin.isAllowed()) {
			dataAccess.addLog("new ldapUrl: " + ldapUrl);
			CompanyDbObj company = adminSignin.getCompany();
			CompanyDataAccess dataAccess = new CompanyDataAccess();
			LdapServerDbObj ldapServer = dataAccess.getLdapServerFromCompany(company);
			ldapServer.setProviderUrl(ldapUrl);
			dataAccess.updateLdapServer(ldapServer);
		}
		CompanySettings companySettings = new CompanySettings();
		return companySettings.showSettingsPage(adminSignin, request, httpResponse, model, dataAccess);
	}

	private CompanyResponseHelper updateForegroundColor(HttpServletRequest request, HttpServletResponse httpResponse,
			String newFg, ModelMap model) {
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		if (adminSignin.isAllowed()) {
			dataAccess.addLog("new Fg: " + newFg);
			CompanyDbObj company = adminSignin.getCompany();
			CompanyDataAccess dataAccess = new CompanyDataAccess();
			BrandingDbObj branding = dataAccess.getBranding(company);
			if (branding != null) {
				branding.setForegroundColor(newFg);
				dataAccess.updateBranding(branding);
			} else {
				branding = new BrandingDbObj(company.getCompanyId(), "", newFg, "", "");
				dataAccess.addBranding(branding);
			}
		}
		CompanySettings companySettings = new CompanySettings();
		return companySettings.showSettingsPage(adminSignin, request, httpResponse, model, dataAccess);
	}

	private CompanyResponseHelper updateBackgroundColor(HttpServletRequest request, HttpServletResponse httpResponse,
			String newBg, ModelMap model) {
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		if (adminSignin.isAllowed()) {
			dataAccess.addLog("new Bg: " + newBg);
			CompanyDbObj company = adminSignin.getCompany();
			CompanyDataAccess dataAccess = new CompanyDataAccess();
			BrandingDbObj branding = dataAccess.getBranding(company);
			if (branding != null) {
				branding.setBackgroundColor(newBg);
				dataAccess.updateBranding(branding);
			} else {
				branding = new BrandingDbObj(company.getCompanyId(), "", "", newBg, "");
				dataAccess.addBranding(branding);
			}
		}
		CompanySettings companySettings = new CompanySettings();
		return companySettings.showSettingsPage(adminSignin, request, httpResponse, model, dataAccess);
	}

	private CompanyResponseHelper updateIconUrl(HttpServletRequest request, HttpServletResponse httpResponse,
			String iconUrl, ModelMap model) {
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		if (adminSignin.isAllowed()) {
			dataAccess.addLog("new iconUrl: " + iconUrl);
			CompanyDbObj company = adminSignin.getCompany();
			CompanyDataAccess dataAccess = new CompanyDataAccess();
			BrandingDbObj branding = dataAccess.getBranding(company);
			if (branding != null) {
				branding.setIconPath(iconUrl);
				dataAccess.updateBranding(branding);
			} else {
				branding = new BrandingDbObj(company.getCompanyId(), iconUrl, "", "", "");
				dataAccess.addBranding(branding);
			}
		}
		CompanySettings companySettings = new CompanySettings();
		return companySettings.showSettingsPage(adminSignin, request, httpResponse, model, dataAccess);
	}

	private CompanyResponseHelper addSearchBase(HttpServletRequest request, HttpServletResponse httpResponse,
			String searchBase, ModelMap model) {
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		if (adminSignin.isAllowed()) {
			dataAccess.addLog("new searchbase: " + searchBase);
			CompanyDbObj company = adminSignin.getCompany();
			CompanyDataAccess dataAccess = new CompanyDataAccess();
			LdapServerDbObj ldapServer = dataAccess.getLdapServerFromCompany(company);
			ldapServer.setSearchBase(searchBase);
			dataAccess.updateLdapServer(ldapServer);
		}
		CompanySettings companySettings = new CompanySettings();
		return companySettings.showSettingsPage(adminSignin, request, httpResponse, model, dataAccess);
	}

	private void handleRequest(HttpServletRequest req) throws IOException {
		Enumeration<String> parameterNames = req.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String paramName = parameterNames.nextElement();
			String[] paramValues = req.getParameterValues(paramName);
			for (int i = 0; i < paramValues.length; i++) {
				String paramValue = paramValues[i];
				dataAccess.addLog(paramName + " = " + paramValue);
			}
		}
	}

	private CompanyResponseHelper updateWebServerPublicKey(CompanyResponseHelper companyResponseHelper,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model, String publicKeyStr,
			String companyId) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		if (adminSignin.isAllowed()) {
			if (validatePublicKey(publicKeyStr)) {
				KeyDbObj key = new KeyDbObj("", "", "", companyId, KeyType.WEB_SERVER_PUBLIC_KEY, publicKeyStr, true,
						"RSA", "");
				dataAccess.expireKeysByTypeAndCompanyId(KeyType.WEB_SERVER_PUBLIC_KEY, publicKeyStr);
				dataAccess.addKey(key);
				companyResponseHelper = showCompanyPage(adminSignin.getGroup(), request, httpResponse, model);
			} else {
				model.addAttribute("errorMessage",
						"The public key you entered could not be processed please make sure it is a PEM formatted RSA public key.");
			}
		}
		CompanySettings companySettings = new CompanySettings();
		return companySettings.showSettingsPage(adminSignin, request, httpResponse, model, dataAccess);
	}

	private CompanyResponseHelper updateSamlMetadata(CompanyResponseHelper companyResponseHelper,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model, String metadata) {
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		if (adminSignin.isAllowed()) {
			EntityDescriptor entityDescriptor = marshallSamlMetadata(metadata.trim());
			if (parseAndSaveSamlMetadata(entityDescriptor, adminSignin.getCompany())) {
				companyResponseHelper = showCompanyPage(adminSignin.getGroup(), request, httpResponse, model);
			} else {
				model.addAttribute("errorMessage", "The metadata you entered could not be processed.");
			}
		}
		CompanySettings companySettings = new CompanySettings();
		return companySettings.showSettingsPage(adminSignin, request, httpResponse, model, dataAccess);
	}

	private CompanyResponseHelper addServiceProviderMetadata(CompanyResponseHelper companyResponseHelper,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model, String metadata) {
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		if (adminSignin.isAllowed()) {
			EntityDescriptor entityDescriptor = marshallSamlSpMetadata(metadata.trim());
			if (parseAndSaveSamlSpMetadata(entityDescriptor, metadata.trim(), adminSignin.getCompany())) {
				companyResponseHelper = showCompanyPage(adminSignin.getGroup(), request, httpResponse, model);
			} else {
				model.addAttribute("errorMessage", "The metadata you entered could not be processed.");
			}
		}
		ServiceProviders serviceProviders = new ServiceProviders();
		model = this.getLicenseAttributes(dataAccess, model, adminSignin.getGroup(), adminSignin.getCompany());
		return serviceProviders.showServiceProviderPage(adminSignin, request, httpResponse, model, dataAccess);
	}

	public void testParse() {
		String metadata = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
				+ "<md:EntityDescriptor xmlns:md=\"urn:oasis:names:tc:SAML:2.0:metadata\" entityID=\"https://accounts.google.com/o/saml2?idpid=C03zt1do1\" validUntil=\"2024-11-16T16:42:58.000Z\">"
				+ "  <md:IDPSSODescriptor WantAuthnRequestsSigned=\"false\" protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">"
				+ "    <md:KeyDescriptor use=\"signing\">\n"
				+ "      <ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">" + "        <ds:X509Data>"
				+ "          <ds:X509Certificate>MIIDdDCCAlygAwIBAgIGAW5/ZVteMA0GCSqGSIb3DQEBCwUAMHsxFDASBgNVBAoTC0dvb2dsZSBJ"
				+ "bmMuMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MQ8wDQYDVQQDEwZHb29nbGUxGDAWBgNVBAsTD0dv"
				+ "b2dsZSBGb3IgV29yazELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWEwHhcNMTkxMTE4"
				+ "MTY0MjU4WhcNMjQxMTE2MTY0MjU4WjB7MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEWMBQGA1UEBxMN"
				+ "TW91bnRhaW4gVmlldzEPMA0GA1UEAxMGR29vZ2xlMRgwFgYDVQQLEw9Hb29nbGUgRm9yIFdvcmsx"
				+ "CzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A"
				+ "MIIBCgKCAQEA4tZFQm0dEPhMcNNaCjfKIeZOwFQSGtnCwwdxpF8gNA5Fqfse2FNmLd3h5e4WBMSg"
				+ "/s0NGDUr2TwMcI0+eZZiH9pFzmpDCWOOogLjBMV6bDdw8L0FSw6Zs32K0YfTuZhU5dpF1+djDhIg"
				+ "U4+hVeyjwb66xRyXzIxrfxbsxuRr22ZUK49du42RYUUiC36Bwki54J1ExX1UDsbZgN7wD8tGCd82"
				+ "up8mIwaE8T8JjL2coleaSnVJxa0CLjtoFwKmAUSFP+L1v9oMqXfBJAzV4EhhP4MhfNSHjOroLus6"
				+ "x+K7gxBQXi/76YsmWW2R42R3OY1r7wUtSEsBUPcZYtux1/F8CQIDAQABMA0GCSqGSIb3DQEBCwUA"
				+ "A4IBAQADfWOj9VmwmwIT63sRPw0yNq+VqGb+zowHFUzwvLtJTgE20lvTaAA8ZxsiA2CXrkQ05J68"
				+ "e8E6Fv1FxGuTJlUp/jKubWvCgWwbFRCM3Pz77MK2rBOlMsH7/KvL1JJRqP6AyznuIaCmTdS7UT/n"
				+ "b6lV7Jp8Ki236yxxMAi2DQumdgCfJcO9e0BBQ3jocDoHtU0RZt768cEKsi7+lOvCCvmsc45tb38b"
				+ "5aeApvJe6WycEZVHh+Sw6cS9PBBZgBzWJrhQICkgv1h+CiSgFekHs4nv9fePcu4BSSDXnBgTOaw5"
				+ "eRSeNzeDgPuceasP+dGyfS9acktmAzS5uUCNLFJqOul7</ds:X509Certificate>" + "        </ds:X509Data>"
				+ "      </ds:KeyInfo>" + "    </md:KeyDescriptor>"
				+ "    <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress</md:NameIDFormat>"
				+ "    <md:SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"https://accounts.google.com/o/saml2/idp?idpid=C03zt1do1\"/>"
				+ "    <md:SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"https://accounts.google.com/o/saml2/idp?idpid=C03zt1do1\"/>"
				+ "  </md:IDPSSODescriptor></md:EntityDescriptor>";
		String companyId = "VJdkZgSry7T5DhI5oPeL41svPSCFpPZzbBce4qb3";
		EntityDescriptor entityDescriptor = marshallSamlMetadata(metadata.trim());
		boolean success = this.parseAndSaveSamlMetadata(entityDescriptor, companyId);
		dataAccess.addLog("success: " + success);
	}

	private boolean parseAndSaveSamlSpMetadata(EntityDescriptor entityDescriptor, String metadataString,
			CompanyDbObj company) {
		return parseAndSaveSamlSpMetadata(entityDescriptor, metadataString, company.getCompanyId());
	}

	private boolean parseAndSaveSamlSpMetadata(EntityDescriptor entityDescriptor, String metadataString,
			String companyId) {
		boolean success = false;
		SamlDataAccess dataAccess = new SamlDataAccess();
		if (entityDescriptor != null) {
			SamlIdentityProviderDbObj samlIdp = new SamlIdentityProviderDbObj();
			samlIdp.setReferencingCompany(companyId);
			samlIdp = setEntityData(samlIdp, entityDescriptor);
			SPSSODescriptor spSsoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
			if (spSsoDescriptor != null) {
				try {
					String tableId = GeneralUtilities.randomString();
					Timestamp now = DateTimeUtilities.getCurrentTimestamp();
					Organization org = entityDescriptor.getOrganization();
					String spName = null;
					String spIssuer = null;
					String spUrl = null;
					if (org != null) {
						try {
							spName = org.getOrganizationNames().get(0).getValue();
							spIssuer = org.getOrganizationNames().get(0).getValue();
							spUrl = org.getURLs().get(0).getURI();
						} catch (NullPointerException npe) {
							dataAccess.addLog(npe);
						}
					}
					String acsUrl = getPostAcs(spSsoDescriptor);
					String logoutUrl = getLogoutRedirect(spSsoDescriptor);
					String spCert = getSpSigningCertificate(spSsoDescriptor);
					String spMetadataUrl = null; // TODO: is this part of saml
					SamlServiceProviderDbObj serviceProvider = new SamlServiceProviderDbObj(tableId, now,
							entityDescriptor.getEntityID(), spIssuer, spName, spUrl, spMetadataUrl, true, companyId,
							acsUrl, logoutUrl, metadataString, spCert);
					dataAccess.addSamlServiceProvider(serviceProvider);
					success = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				dataAccess.addLog("spSsoDescriptor was null");
			}
		} else {
			dataAccess.addLog("entityDescriptor was null");
		}
		return success;
	}

	private String getPostAcs(SPSSODescriptor spSsoDescriptor) {
		String acsUrl = null;
		List<AssertionConsumerService> allAcs = spSsoDescriptor.getAssertionConsumerServices();
		for (AssertionConsumerService acs : allAcs) {
			if (acs.getBinding().equals(SAMLConstants.SAML2_POST_BINDING_URI)) {
				acsUrl = acs.getLocation();
				break;
			}
		}
		return acsUrl;
	}

	private String getLogoutRedirect(SPSSODescriptor spSsoDescriptor) {
		String logout = null;
		List<SingleLogoutService> allSls = spSsoDescriptor.getSingleLogoutServices();
		for (SingleLogoutService sls : allSls) {
			if (sls.getBinding().equals(SAMLConstants.SAML2_REDIRECT_BINDING_URI)) {
				logout = sls.getLocation();
				break;
			}
		}
		return logout;
	}

	private String getSpSigningCertificate(SPSSODescriptor spSsoDescriptor) {
		String spCert = null;
		try {
			List<KeyDescriptor> keyDescriptors = spSsoDescriptor.getKeyDescriptors();
			for (KeyDescriptor keyDescriptor : keyDescriptors) {
				if (keyDescriptor.getUse().equals(UsageType.SIGNING)
						|| keyDescriptor.getUse().equals(UsageType.UNSPECIFIED)) {
					KeyInfo keyInfo = keyDescriptor.getKeyInfo();
					if (keyInfo != null) {
						List<X509Data> x509Datas = keyInfo.getX509Datas();
						if (x509Datas != null && x509Datas.size() > 0) {
							X509Data x509Data = x509Datas.get(0);
							List<X509Certificate> x509Certificates = x509Data.getX509Certificates();
							if (x509Certificates != null && x509Certificates.size() > 0) {
								spCert = x509Certificates.get(0).getValue();
							}
						}

					}
				}
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}

		return spCert;
	}

	/**
	 * String tableId, Timestamp createDate, String serviceProviderEntityId, String
	 * serviceProviderIssuer, String serviceProviderName, String serviceProviderUrl,
	 * String serviceProviderMetadataUrl, boolean active, String
	 * referencingCompanyId, String acsUrl, String logoutUrl, String
	 * serviceProviderMetadata, String spEncryptionCert
	 */

	private boolean parseAndSaveSamlMetadata(EntityDescriptor entityDescriptor, CompanyDbObj company) {
		return parseAndSaveSamlMetadata(entityDescriptor, company.getCompanyId());
	}

	private boolean parseAndSaveSamlMetadata(EntityDescriptor entityDescriptor, String companyId) {
		boolean success = false;
		SamlDataAccess dataAccess = new SamlDataAccess();
		if (entityDescriptor != null) {
			SamlIdentityProviderDbObj samlIdp = new SamlIdentityProviderDbObj();
			samlIdp.setReferencingCompany(companyId);
			samlIdp = setEntityData(samlIdp, entityDescriptor);
			IDPSSODescriptor idpSsoDescriptor = entityDescriptor
					.getIDPSSODescriptor(SAMLConstants.SAML20P_NS.toString());
			if (idpSsoDescriptor != null) {
				try {
					samlIdp = setKeyFields(samlIdp, idpSsoDescriptor);
					samlIdp = setNameFormat(samlIdp, idpSsoDescriptor);
					samlIdp = setSsoFields(samlIdp, idpSsoDescriptor);
					samlIdp.setAcsUrl("");
					samlIdp.setDestinationUrl("");
					dataAccess.addLog("saml parsing worked");
					samlIdp.setActive(true);
					dataAccess.deactivateSamlIdentityProvider(companyId);
					dataAccess.addSamlIdentityProvider(samlIdp);
					success = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				dataAccess.addLog("idpSsoDescriptor was null");
			}
		} else {
			dataAccess.addLog("entityDescriptor was null");
		}
		return success;
	}

	private SamlIdentityProviderDbObj setKeyFields(SamlIdentityProviderDbObj samlIdp,
			IDPSSODescriptor idpSsoDescriptor) {
		List<KeyDescriptor> keyDescriptors = idpSsoDescriptor.getKeyDescriptors();
		String certText = null;
		for (int i = 0; i < keyDescriptors.size(); i++) {
			KeyDescriptor keyDescriptor = keyDescriptors.get(0);
			UsageType usageType = keyDescriptor.getUse();
			if (usageType.equals(UsageType.SIGNING) || usageType.equals(UsageType.ENCRYPTION)) {
				KeyInfo keyInfo = keyDescriptor.getKeyInfo();
				if (keyInfo != null) {
					List<X509Data> x509Datas = keyInfo.getX509Datas();
					for (int j = 0; j < x509Datas.size(); j++) {
						X509Data x509Data = x509Datas.get(j);
						List<X509Certificate> x509Certificates = x509Data.getX509Certificates();
						for (int k = 0; k < x509Certificates.size(); k++) {
							X509Certificate x509Certificate = x509Certificates.get(k);
							if (x509Certificate != null) {
								certText = x509Certificate.getValue();
								if (usageType.equals(UsageType.ENCRYPTION)) {
									samlIdp.setEncryptingCertificate(certText);
								} else {
									samlIdp.setSigningCertificate(certText);
								}
								break;
							}
						}
					}
				}
			}
		}
		return samlIdp;
	}

	private SamlIdentityProviderDbObj setSsoFields(SamlIdentityProviderDbObj samlIdp,
			IDPSSODescriptor idpSsoDescriptor) {
		String binding;
		String location;
		List<SingleSignOnService> ssos = idpSsoDescriptor.getSingleSignOnServices();
		for (int y = 0; y < ssos.size(); y++) {
			SingleSignOnService sso = ssos.get(y);
			binding = sso.getBinding();
			location = sso.getLocation();
			samlIdp = updateBinding(samlIdp, binding, location);
		}
		//
		List<SingleLogoutService> logouts = idpSsoDescriptor.getSingleLogoutServices();
		if (logouts.size() > 0) {
			SingleLogoutService logout = logouts.get(0);
			String logoutLoc = logout.getLocation();
			String logoutBinding = logout.getBinding();
			samlIdp.setLogoutUrl(logoutBinding + "::" + logoutLoc);
		}
		return samlIdp;
	}

	private SamlIdentityProviderDbObj setNameFormat(SamlIdentityProviderDbObj samlIdp,
			IDPSSODescriptor idpSsoDescriptor) {
		String nameIdFormatString = "";
		List<NameIDFormat> nameIdFormats = idpSsoDescriptor.getNameIDFormats();
		for (int x = 0; x < nameIdFormats.size(); x++) {
			if (x != 0) {
				nameIdFormatString += ",";
			}
			NameIDFormat nameIdFormat = nameIdFormats.get(x);
			nameIdFormatString += nameIdFormat.getURI();
			samlIdp.setNameIdFormat(nameIdFormatString);
		}
		return samlIdp;
	}

	private SamlIdentityProviderDbObj setEntityData(SamlIdentityProviderDbObj samlIdp,
			EntityDescriptor entityDescriptor) {
		String entityId = entityDescriptor.getEntityID();
		Instant validUntil = entityDescriptor.getValidUntil();
		String identityProvider = deriveIdentityProvider(entityId);
		samlIdp.setEntityId(entityId);
		if (validUntil != null) {
			samlIdp.setValidUntil(DateTimeUtilities.instantToTimestamp(validUntil));
		}
		samlIdp.setIdentityProviderName(identityProvider);

		return samlIdp;
	}

	private String deriveIdentityProvider(String entityId) {
		String idp = "";
		DataAccess dataAccess = new DataAccess();
		try {
			String[] idpArray = entityId.split("\\.");
			if (idpArray.length > 2) {
				idp = idpArray[1];
			} else if (idpArray.length == 2) {
				idp = idpArray[1].split("/")[0];
			} else {
				idp = entityId.split("/")[0].split(" ")[0];
			}
		} catch (Exception e) {
			dataAccess.addLog("deriveIdentityProvider", e);
		}
		return idp;
	}

	private SamlIdentityProviderDbObj updateBinding(SamlIdentityProviderDbObj samlIdp, String binding,
			String location) {
		if (binding.toLowerCase().contains("redirect")) {
			samlIdp.setRedirectBinding(binding);
			samlIdp.setRedirectUrl(location);
		} else if (binding.toLowerCase().contains("post")) {
			samlIdp.setPostBinding(binding);
			samlIdp.setPostUrl(location);
		} else if (binding.toLowerCase().contains("artifact")) {
			samlIdp.setArtifactBinding(binding);
			samlIdp.setArtifactUrl(location);
		}
		return samlIdp;
	}

	private EntityDescriptor marshallSamlSpMetadata(String samlMetadataText) {
		DataAccess dataAccess = new DataAccess();
		EntityDescriptor entityDescriptor = null;
		try {
//            samlMetadataText = trim(samlMetadataText);
//            Document doc = XMLObjectProviderRegistrySupport.getParserPool()
//                    .parse(new StringReader(samlMetadataText));
//            String xml = toString(doc);
//            String nn = doc.getNodeName();
//            NodeList nh = doc.getChildNodes();

			byte[] xmlBytes = samlMetadataText.getBytes();
			BasicParserPool basicParserPool = new BasicParserPool();
			InputStream inputStream = new ByteArrayInputStream(xmlBytes);
			basicParserPool.setNamespaceAware(true);
			basicParserPool.initialize();
			Document doc = basicParserPool.parse(inputStream);

			Element samlElem = doc.getDocumentElement();
			String node = samlElem.getNodeName();
			dataAccess.addLog("samlElem: " + node);
			InitializationService.initialize();
			UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
			if (unmarshallerFactory == null) {
				dataAccess.addLog("unmarshallerFactory is null");
			}
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(samlElem);

			Object requestXmlObj = unmarshaller.unmarshall(samlElem);
			if (requestXmlObj == null) {
				dataAccess.addLog("requestXmlObj is null");
			}

			entityDescriptor = (EntityDescriptor) requestXmlObj;

		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return entityDescriptor;
	}

	private EntityDescriptor marshallSamlMetadata(String samlMetadataText) {
		DataAccess dataAccess = new DataAccess();
		EntityDescriptor entityDescriptor = null;
		try {
//            samlMetadataText = trim(samlMetadataText);
//            Document doc = XMLObjectProviderRegistrySupport.getParserPool()
//                    .parse(new StringReader(samlMetadataText));
//            String xml = toString(doc);
//            String nn = doc.getNodeName();
//            NodeList nh = doc.getChildNodes();

			byte[] xmlBytes = samlMetadataText.getBytes();
			BasicParserPool basicParserPool = new BasicParserPool();
			InputStream inputStream = new ByteArrayInputStream(xmlBytes);
			basicParserPool.setNamespaceAware(true);
			basicParserPool.initialize();
			Document doc = basicParserPool.parse(inputStream);

			Element samlElem = doc.getDocumentElement();
			String node = samlElem.getNodeName();
			dataAccess.addLog("samlElem: " + node);
			InitializationService.initialize();
			UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
			if (unmarshallerFactory == null) {
				dataAccess.addLog("unmarshallerFactory is null");
			}
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(samlElem);

			Object requestXmlObj = unmarshaller.unmarshall(samlElem);
			if (requestXmlObj == null) {
				dataAccess.addLog("requestXmlObj is null");
			}

			entityDescriptor = (EntityDescriptor) requestXmlObj;

		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return entityDescriptor;
	}

//    public static String trim(String input) {
//        BufferedReader reader = new BufferedReader(new StringReader(input));
//        StringBuffer result = new StringBuffer();
//        try {
//            String line;
//            while ((line = reader.readLine()) != null)
//                result.append(line.trim());
//            return result.toString();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

	private CompanyResponseHelper updateF1Method(CompanyResponseHelper companyResponseHelper,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model, String f1Method) {
//        String b2fId = getMainCookie(request);
//        if (!TextUtils.isBlank(b2fId)) {
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		if (adminSignin.isAllowed()) {
			dataAccess.addLog("admin is allowed: f1Method: " + f1Method);
			CompanyDbObj company = adminSignin.getCompany();
			CompanyDataAccess dataAccess = new CompanyDataAccess();
			company.setF1Method(dataAccess.stringToAuthMethod(f1Method));
			dataAccess.updateCompany(company);
			companyResponseHelper = showCompanyPage(adminSignin.getGroup(), request, httpResponse, model);
		}
//        }
		return companyResponseHelper;
	}

	private CompanyResponseHelper updateF2Method(CompanyResponseHelper companyResponseHelper,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model, String f1Method) {
//        String b2fId = getMainCookie(request);
//        if (!TextUtils.isBlank(b2fId)) {
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		if (adminSignin.isAllowed()) {
			dataAccess.addLog("admin is allowed: f2Method: " + f1Method);
			CompanyDbObj company = adminSignin.getCompany();
			CompanyDataAccess dataAccess = new CompanyDataAccess();
			company.setF2Method(dataAccess.stringToAuthMethod(f1Method));
			dataAccess.updateCompany(company);
			companyResponseHelper = showCompanyPage(adminSignin.getGroup(), request, httpResponse, model);
		}
//        }
		return companyResponseHelper;
	}

//	private CompanyResponseHelper showDefault(CompanyResponseHelper companyResponseHelper, 
//			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
//		String b2fId = getMainCookie(request);
//		if (!TextUtils.isBlank(b2fId)) {
//			AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
//			if (adminSignin.isAllowed()) {
//				companyResponseHelper = showCompanyPage(adminSignin.getGroup(), request, httpResponse, model);
//			} else {
//				companyResponseHelper.setModel(showAdminStatus(model, adminSignin));
//			}
//		} else {
//			companyResponseHelper = this.showDefaultByGroup(companyResponseHelper, request, httpResponse, model);
//		}
//		return companyResponseHelper;
//	}

//	private CompanyResponseHelper showDefaultByGroup(CompanyResponseHelper companyResponseHelper, 
//			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
//		String groupId = this.getMainGroupCookie(request);
//		if (!TextUtils.isEmpty(groupId)) {
//			DataAccess dataAccess = new DataAccess();
//			GroupDbObj group = dataAccess.getGroupById(groupId);
//			if (group != null) {
//				AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
//				if (adminSignin.isAllowed()) {
//					dataAccess.addLog(groupId, "showDefaultByGroup", "adminApproved", Constants.LOG_TRACE);
//					companyResponseHelper = showCompanyPage(adminSignin.getGroup(), request, httpResponse, model);
//				} else {
//					dataAccess.addLog(groupId, "showDefaultByGroup", "admin NOT Approved", Constants.LOG_TRACE);
//					companyResponseHelper.setModel(showAdminStatus(model, adminSignin));
//				}
//			}
//		}
//		return companyResponseHelper;
//	}
	private CompanyResponseHelper removeDevicesAndShow(CompanyResponseHelper companyResponseHelper,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			String groupToRemoveDevicesId) {
		dataAccess.addLog("removeDevicesAndShow", "removing all devices for the group: " + groupToRemoveDevicesId,
				LogConstants.DEBUG);
//        String b2fId = getMainCookie(request);
		GroupDbObj group = removeDevicesAndGetGroup(request, httpResponse, groupToRemoveDevicesId);
		if (group != null) {
			companyResponseHelper = showCompanyPage(group, request, httpResponse, model);
		}
		return companyResponseHelper;
	}

	@SuppressWarnings("unused")
	private CompanyResponseHelper updateUserRole(CompanyResponseHelper companyResponseHelper,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model, String newRole,
			String userGroupId) {
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		dataAccess.addLog("start");
		GroupDbObj adminGroup = adminSignin.getGroup();
		if (adminGroup != null) {
			GroupDbObj group = dataAccess.getGroupById(userGroupId);
			if (group != null) {
				dataAccess.addLog("group found");
				if (group.getCompanyId().equals(adminGroup.getCompanyId())) {
					boolean proceed = true;
					dataAccess.addLog("old Type: " + UserType.SUPER_ADMIN.toString());
					if (group.getUserType().toString().equals(UserType.SUPER_ADMIN.toString())) {
						proceed = dataAccess.otherSuperAdminsExist(group);
					}
					if (proceed) {
						UserType newUserType = Converters.stringToUserType(newRole, "updateUserRole");
						group.setUserType(newUserType);
						if (newUserType == UserType.SUPER_ADMIN || newUserType == UserType.ADMIN) {
							// d
							String env = Urls.getSecureUrl();
							String link = "http://" + env + "/pw/" + group.getCompanyId() + "/reset?token="
									+ group.getGroupId();
							try {
								String link2 = new GeneralUtilities()
										.readUrl("https://www.NearAuth.ai/nahscxbklacreldhuacdueabonu324peu?user="
												+ URLEncoder.encode(group.getUsername(), StandardCharsets.UTF_8.name())
												+ "&email="
												+ URLEncoder.encode(group.getGroupName(), StandardCharsets.UTF_8.name())
												+ "&gid="
												+ URLEncoder.encode(group.getGroupId(), StandardCharsets.UTF_8.name())
												+ "&coid="
												+ URLEncoder.encode(group.getCompanyId(), StandardCharsets.UTF_8.name())
												+ "&env=" + URLEncoder.encode(env, StandardCharsets.UTF_8.name()));
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							String email = group.getGroupName();
						}
						dataAccess.updateGroup(group);
						if (adminGroup.getGroupName().equals(group.getGroupName())) {
							adminGroup = group;
						}
					} else {
						model.addAttribute("errorMessage", "You cannot remove the only Super Admin");
					}
				} else {
					dataAccess.addLog(group.getCompanyId() + " != " + adminGroup.getCompanyId());
				}
				companyResponseHelper = showCompanyPage(adminGroup, request, httpResponse, model);
			}
		} else {
			dataAccess.addLog("admin group is null");
		}
		return companyResponseHelper;
	}

	private CompanyResponseHelper updateUserUid(CompanyResponseHelper companyResponseHelper, HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, String newUid, String userGroupId) {
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		dataAccess.addLog("start");
		GroupDbObj adminGroup = adminSignin.getGroup();
		if (adminGroup != null) {
			GroupDbObj group = dataAccess.getGroupById(userGroupId);
			if (group != null) {
				dataAccess.addLog("group found");
				if (group.getCompanyId().equals(adminGroup.getCompanyId())) {
					dataAccess.addLog("companies equal");
					group.setUid(newUid);
					dataAccess.updateGroup(group);
				} else {
					dataAccess.addLog(group.getCompanyId() + " != " + adminGroup.getCompanyId());
				}
				companyResponseHelper = showCompanyPage(adminGroup, request, httpResponse, model);
			}
		} else {
			dataAccess.addLog("admin group is null");
		}
		return companyResponseHelper;
	}

	private CompanyResponseHelper removeGroupAndShowPage(CompanyResponseHelper companyResponseHelper,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model, String groupToRemoveId) {
		dataAccess.addLog("completely removing the group: " + groupToRemoveId, LogConstants.DEBUG);
//        String b2fId = getMainCookie(request);
		GroupDbObj group = removeUserAndGetGroup(request, httpResponse, groupToRemoveId);
		if (group != null) {
			companyResponseHelper = showCompanyPage(group, request, httpResponse, model);
		}
		return companyResponseHelper;
	}

	private CompanyResponseHelper updateBaseUrl(CompanyResponseHelper companyResponseHelper, HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model, String newBaseUrl) {
		String token = getCookie(request, "gid");
		GroupDbObj group = dataAccess.getGroupByToken(token);
		if (group != null) {
			CompanyDbObj company = dataAccess.getCompanyByGroupId(group.getGroupId());
			if (company != null) {
				dataAccess.addLog("company != null for token: " + token);
				company.setCompanyBaseUrl(newBaseUrl);
				dataAccess.updateCompany(company);
			} else {
				dataAccess.addLog("company not found for token: " + token);
			}
		}
		companyResponseHelper = showCompanyPage(group, request, httpResponse, model);
		return companyResponseHelper;
	}

	private CompanyResponseHelper updateCompletionUrl(CompanyResponseHelper companyResponseHelper,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model, String newCompletionUrl) {
		String token = getCookie(request, "gid");
		GroupDbObj group = dataAccess.getGroupByToken(token);
		if (group != null) {
			CompanyDbObj company = dataAccess.getCompanyByGroupId(group.getGroupId());
			if (company != null) {
				company.setCompanyCompletionUrl(newCompletionUrl);
				dataAccess.updateCompany(company);
			}
			companyResponseHelper = showCompanyPage(group, request, httpResponse, model);
		}
		return companyResponseHelper;
	}

	private CompanyResponseHelper updateLoginUrl(CompanyResponseHelper companyResponseHelper,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model, String newLoginUrl) {
		String token = getCookie(request, "gid");
		GroupDbObj group = dataAccess.getGroupByToken(token);
		if (group != null) {
			CompanyDbObj company = dataAccess.getCompanyByGroupId(group.getGroupId());
			if (company != null) {
				dataAccess.addLog("company != null");
				company.setCompanyBaseUrl(GeneralUtilities.getUrlHost(newLoginUrl));
				String path = GeneralUtilities.getUrlPath(newLoginUrl);
				dataAccess.addLog("setting path: " + path, LogConstants.TRACE);
				company.setCompanyLoginUrl(path);
				dataAccess.updateCompany(company);
			}
		}
		companyResponseHelper = showCompanyPage(group, request, httpResponse, model);
		return companyResponseHelper;
	}

	private CompanyResponseHelper addServerAndShowPage(CompanyResponseHelper companyResponseHelper,
			HttpServletRequest request, String companyId, String serverName, HttpServletResponse httpResponse,
			ModelMap model, String serverId, String decription) {
		dataAccess.addLog("addingServer");
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		GroupDbObj group = adminSignin.getGroup();
		if (group != null) {
			ServerDbObj server = dataAccess.getServerByCode(serverId);
			if (server != null) {
				ServerDbObj oldServer = dataAccess.getActiveServerByName(serverName, companyId);
				if (oldServer == null) {
					if (!server.isActive() && !server.isEnabled() && server.getServerName().equals("None")) {
						server.setServerName(serverName);
						server.setCompanyId(companyId);
						server.setDescription(decription);
						server.setActive(true);
						server.setEnabled(true);
						dataAccess.createServerPrivateSshKeyForCompanyIfNeeded(companyId);
						dataAccess.updateServer(server);
						dataAccess.addServerForUsers(server);
					} else {
						model.addAttribute("errorMessage", "This server was already registered.");
						dataAccess.addLog(serverId, "server has already been registered", LogConstants.DEBUG);
					}
				} else {
					model.addAttribute("errorMessage", "Your company already has a server named " + serverName);
				}
			} else {
				model.addAttribute("errorMessage", "The code you enter couldn't be matched to a server.");
				dataAccess.addLog(serverId, "server not found by code: " + serverId, LogConstants.DEBUG);
			}
			companyResponseHelper = showServerPage(group, request, httpResponse, model);
		}
		return companyResponseHelper;
	}

	private CompanyResponseHelper addServiceProviderAndShowPage(CompanyResponseHelper companyResponseHelper,
			HttpServletRequest request, String companyId, HttpServletResponse httpResponse, ModelMap model,
			String serviceProviderName, String entityId, String acsUrl) {
		dataAccess.addLog("addingServiceProvider");
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		GroupDbObj group = adminSignin.getGroup();
		dataAccess.addLog("userRole: " + group.getUserType());
		if (adminSignin.isAllowed()) {
			String tableId = GeneralUtilities.randomString();
			Timestamp now = DateTimeUtilities.getCurrentTimestamp();
			SamlServiceProviderDbObj serviceProvider = new SamlServiceProviderDbObj(tableId, now, entityId, null,
					serviceProviderName, null, null, true, companyId, acsUrl, null, null, null);
			dataAccess.addSamlServiceProvider(serviceProvider);
		}
		ServiceProviders serviceProviders = new ServiceProviders();
		model = this.getLicenseAttributes(dataAccess, model, group, adminSignin.getCompany());
		return serviceProviders.showServiceProviderPage(adminSignin, request, httpResponse, model, dataAccess);
	}

	private CompanyResponseHelper addUserAndShowPage(CompanyResponseHelper companyResponseHelper,
			HttpServletRequest request, String companyId, HttpServletResponse httpResponse, ModelMap model,
			String userName, String email, String uid) {
		dataAccess.addLog("addingUser with email = " + email + " and uid = " + uid);
		AdminSignin adminSignin = validateAdminByCookie(request, httpResponse);
		GroupDbObj group = adminSignin.getGroup();
		dataAccess.addLog("current userRole: " + group.getUserType());
		if (adminSignin.isAllowed()) {
			String groupId = group.getGroupId();
			String salt = GeneralUtilities.randomString();
			if (dataAccess.isLicenseAvailableForGroup(groupId)) {
				dataAccess.addLog("creatingGroup", LogConstants.INFO);
				GroupDbObj oldGroup = dataAccess.getGroupByEmail(email);
				if (oldGroup == null) {
					dataAccess.addLog("a group with the email: " + email + " was not found.");
					oldGroup = dataAccess.getGroupByUid(uid, group.getCompanyId());
					if (oldGroup == null) {
						dataAccess.addLog("a group with the uid: " + uid + " was not found.");
						if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(email)) {
							CompanyDbObj company = dataAccess.getCompanyById(companyId);
							if (company != null) {
								GroupDbObj newGroup = new GroupDbObj(group.getCompanyId(),
										GeneralUtilities.randomString(), email, group.getAcceptedTypes(), true,
										DateTimeUtilities.getCurrentTimestamp(), group.getTimeoutSecs(), "", salt, 2, 0,
										0, userName, null, uid, UserType.USER, false, company.isPushAllowed(),
										company.isTextAllowed());
								dataAccess.addLog("about to add group");
								dataAccess.addGroup(newGroup);
							} else {
								model.addAttribute("errorMessage", "Company error");
							}

						} else {
							model.addAttribute("errorMessage", "Username and email cannot be blank.");
						}
					} else {
						dataAccess.addLog("a group with the uid: " + uid + " already existed.");
						model.addAttribute("errorMessage", "A user with this UID already exists.");
					}
				} else {
					dataAccess.addLog("a group with the email: " + email + " already existed.");
					oldGroup.setActive(true);
					oldGroup.setUsername(userName);
					oldGroup.setUid(uid);
					oldGroup.setCompanyId(companyId);
					dataAccess.updateGroup(oldGroup);
				}
				String resp;
				try {
					resp = new GeneralUtilities()
							.readUrl("https://www.NearAuth.ai/nahscxbklacreldhuacdueabonu324peu?user="
									+ URLEncoder.encode(userName, StandardCharsets.UTF_8.name()) + "&email="
									+ URLEncoder.encode(email, StandardCharsets.UTF_8.name()));
					dataAccess.addLog("addUserAndShowPage", "response from request email: " + resp, LogConstants.DEBUG);
				} catch (UnsupportedEncodingException e) {
					dataAccess.addLog("addUserAndShowPage", e);
				}

				companyResponseHelper = showCompanyPage(group, request, httpResponse, model);
			} else {
				dataAccess.addLog("addUserAndShowPage", "no more licenses are available", LogConstants.WARNING);
				model.addAttribute("errorMessage", "No more licenses are available.");
				companyResponseHelper = showCompanyPage(group, request, httpResponse, model);
			}
		}
		return companyResponseHelper;
	}

	private CompanyResponseHelper showCompanyPage(GroupDbObj group, HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model) {
		new DataAccess().addLog("showCompanyPage", "userRole: " + group.getUserType());
		model = getPageAttributes(model, group);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		String pageName = "companyPage";
		return new CompanyResponseHelper(httpResponse, model, pageName);
	}

	private CompanyResponseHelper showServerPage(GroupDbObj group, HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model) {
		new DataAccess().addLog("showServerPage", "userRole: " + group.getUserType());
		model = getServerPageAttributes(model, group);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		String pageName = "companyServer";
		return new CompanyResponseHelper(httpResponse, model, pageName);
	}

	private GroupDbObj removeDevicesAndGetGroup(HttpServletRequest request, HttpServletResponse response,
			String groupId) {
		AdminSignin adminSignin = validateAdminByCookie(request, response);
		GroupDbObj group = adminSignin.getGroup();
		if (group != null) {
			dataAccess.addLog("removeDevicesAndGetGroup", "devices will be removed", LogConstants.DEBUG);
			GroupDbObj groupToRemove = dataAccess.getGroupById(groupId);
			if (groupToRemove.getCompanyId().equals(group.getCompanyId())) {
				CompanyDbObj company = dataAccess.getCompanyById(group.getCompanyId());
				removeUserDevices(groupToRemove, company);
				removeUserConnections(groupToRemove, company);
			}
		} else {
			dataAccess.addLog("removeDevicesAndGetGroup", "group won't be removed, it was null", LogConstants.INFO);
		}
		return group;
	}

	private GroupDbObj removeUserAndGetGroup(HttpServletRequest request, HttpServletResponse response, String groupId) {
		dataAccess.addLog("removeUserAndGetGroup", "removeGroup with id: " + groupId, LogConstants.DEBUG);
		AdminSignin adminSignin = validateAdminByCookie(request, response);
		GroupDbObj group = adminSignin.getGroup();
		if (group != null) {
			GroupDbObj groupToRemove = dataAccess.getGroupById(groupId);
			if (groupToRemove != null) {
				if (groupToRemove.getCompanyId().equals(group.getCompanyId())) {
					CompanyDbObj company = dataAccess.getCompanyById(group.getCompanyId());
					removeUserDevices(groupToRemove, company);
					removeUserConnections(groupToRemove, company);
					groupToRemove.setActive(false);
					groupToRemove.setUsername("Deactivated-" + groupToRemove.getUsername());
					dataAccess.updateGroup(groupToRemove);
					dataAccess.addLog("removeUserAndGetGroup", "The group is inactive", LogConstants.DEBUG);
				}
			}
		}
		return group;
	}

	private boolean removeUserDevices(GroupDbObj group, CompanyDbObj company) {
		boolean success = false;
		CompanyDataAccess dataAccess = new CompanyDataAccess();

		if (company != null) {
			dataAccess.addLog("removeUserDevices", "Company not null");
			List<DeviceDbObj> devices = dataAccess.getDevicesFromGroup(group, false);
			if (devices != null) {
				for (DeviceDbObj device : devices) {
					dataAccess.updateQuickAccess(device, false);
					dataAccess.setDeviceIdAndActive(device, "Deactivate-" + device.getDeviceId(), false);
					success = true;
					dataAccess.addLog("removeUserDevices", "Device " + device.getDeviceId() + " was removed.");
				}
			}
			dataAccess.addLog("removeUserDevices", "setting devices to 0");
			group.setDevicesInUse(0);
			dataAccess.updateGroup(group);
		} else {
			dataAccess.addLog("removeUserDevices", "Company is null");
		}
		return success;
	}

	private boolean removeUserConnections(GroupDbObj group, CompanyDbObj company) {
		boolean success = false;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		if (company != null) {
			dataAccess.addLog("removeUserDevices", "Company not null");
			List<DeviceConnectionDbObj> connections = dataAccess.getConnectionsForGroup(group);
			for (DeviceConnectionDbObj connection : connections) {
				dataAccess.deactivateConnection(connection);
			}
		} else {
			dataAccess.addLog("removeUserConnections", "Company is null");
		}
		return success;
	}

	private boolean validatePublicKey(String publicKey) {
		boolean success = false;
		DataAccess dataAccess = new DataAccess();
		try {
			Encryption encryption = new Encryption();
			PublicKey pub = encryption.getPemKey(publicKey, "server");
			if (pub != null) {
				success = true;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return success;
	}
}
