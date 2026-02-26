package com.humansarehuman.blue2factor.authentication.browserSetup;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.authentication.failures.Failure;
import com.humansarehuman.blue2factor.authentication.serverAuth.FirstFactorSetup;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.RequestAndResponse;
import com.humansarehuman.blue2factor.entities.UrlAndModel;
import com.humansarehuman.blue2factor.entities.UrlModelAndHttpResponse;
import com.humansarehuman.blue2factor.entities.enums.AuthorizationMethod;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.SamlAuthnRequestDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

//set up browser keys
//get fingerprint
//go back to original page and set cookie
@Controller
@RequestMapping(value = Urls.BROWSER_INSTALL)
public class BrowserInstallation extends B2fApi {
	@RequestMapping(method = RequestMethod.GET)
	public Object browserInstallationGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		String resp = "setupFailure";
		String accessCode = this.getRequestValue(request, "b2fid");
		int logLevel = LogConstants.TRACE;
		SamlDataAccess dataAccess = new SamlDataAccess();

		boolean bleFailed = this.getRequestValueBoolean(request, "bf", false);
		dataAccess.addLog(request.getRequestURL().toString() + "?" + request.getQueryString(), logLevel);
		if (!TextUtils.isEmpty(accessCode)) {
			AccessCodeDbObj accessCodeObj = dataAccess.getAccessCodeFromAccessString(accessCode);
			DeviceDbObj device = dataAccess.getDeviceByAccessCode(accessCode);
			if (accessCodeObj != null && device != null) {
				String signature = this.getRequestValue(request, "b2fs");
				signature = signature.replace(" ", "+");
				signature = signature.replace("%3D", "=");
				CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
				if (company != null) {
					Encryption encryption = new Encryption();
					if (encryption.verifySignatureWithForegroundKey(device,
							device.getDeviceId() + Constants.APPENDED_STRING, signature)) {
						dataAccess.addLog("signature verified - nice", logLevel);
						// do the install
						dataAccess.addLog("active: " + device.isActive() + ", signedIn: " + device.getSignedIn()
								+ ", bleFailed: " + bleFailed, logLevel);
						if (device.isActive()) {
							UrlModelAndHttpResponse urlModelAndHttpResponse = installB2fForBrowser(request,
									httpResponse, device, company, accessCodeObj, model, dataAccess);
							resp = urlModelAndHttpResponse.getUrl();
							model = urlModelAndHttpResponse.getModelMap();
							httpResponse = urlModelAndHttpResponse.getHttpResponse();
						}
					} else {
						dataAccess.addLog(device.getDeviceId(), "the signature could not be verified - " + signature,
								LogConstants.ERROR);
					}
				} else {
					dataAccess.addLog(device.getDeviceId(), "company was null", LogConstants.ERROR);
				}
				accessCodeObj.setActive(false, "BrowserInstallation");
				dataAccess.updateAccessCode(accessCodeObj);
			} else {
				dataAccess.addLog("device was null", LogConstants.ERROR);
			}
		} else {
			dataAccess.addLog("access code was empty or expired", LogConstants.ERROR);
		}
		dataAccess.addLog("returning: " + resp, logLevel);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return resp;
	}

	@RequestMapping(method = RequestMethod.POST)
	public Object browserInstallationPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		String resp = "setupFailure";
		String accessCode = this.getRequestValue(request, "b2fid");
		int logLevel = LogConstants.TRACE;
		SamlDataAccess dataAccess = new SamlDataAccess();

		boolean bleFailed = this.getRequestValueBoolean(request, "bf", false);
		dataAccess.addLog(request.getRequestURL().toString() + "?" + request.getQueryString(), logLevel);
		if (!TextUtils.isEmpty(accessCode)) {
			AccessCodeDbObj accessCodeObj = dataAccess.getAccessCodeFromAccessString(accessCode);
			DeviceDbObj device = dataAccess.getDeviceByAccessCode(accessCode);
			if (accessCodeObj != null && device != null) {
				String signature = this.getRequestValue(request, "b2fs");
				signature = signature.replace(" ", "+");
				signature = signature.replace("%3D", "=");
				CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
				if (company != null) {
					Encryption encryption = new Encryption();
					if (encryption.verifySignatureWithForegroundKey(device,
							device.getDeviceId() + Constants.APPENDED_STRING, signature)) {
						dataAccess.addLog("signature verified - nice", logLevel);
						// do the install
						dataAccess.addLog("active: " + device.isActive() + ", signedIn: " + device.getSignedIn()
								+ ", bleFailed: " + bleFailed, logLevel);
						if (device.isActive()) {
							UrlModelAndHttpResponse urlModelAndHttpResponse = installB2fForBrowser(request,
									httpResponse, device, company, accessCodeObj, model, dataAccess);
							resp = urlModelAndHttpResponse.getUrl();
							model = urlModelAndHttpResponse.getModelMap();
							httpResponse = urlModelAndHttpResponse.getHttpResponse();
						}
					} else {
						dataAccess.addLog(device.getDeviceId(), "the signature could not be verified - " + signature,
								LogConstants.ERROR);
					}
				} else {
					dataAccess.addLog(device.getDeviceId(), "company was null", LogConstants.ERROR);
				}
				accessCodeObj.setActive(false, "BrowserInstallation");
				dataAccess.updateAccessCode(accessCodeObj);
			} else {
				dataAccess.addLog("device was null", LogConstants.ERROR);
			}
		} else {
			dataAccess.addLog("access code was empty or expired", LogConstants.ERROR);
		}
		dataAccess.addLog("returning: " + resp, logLevel);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return resp;
	}

	private UrlModelAndHttpResponse installB2fForBrowser(HttpServletRequest request, HttpServletResponse httpResponse,
			DeviceDbObj device, CompanyDbObj company, AccessCodeDbObj accessCode, ModelMap model,
			SamlDataAccess dataAccess) {
		String next = "";
		int logLevel = LogConstants.TRACE;
		UrlModelAndHttpResponse urlModelAndHttpResponse;
		BrowserDbObj browser = dataAccess.addBrowser(device, "installB2fForBrowser");
		if (didWeJustAuthenticateOnInstall(device, dataAccess)) {
			dataAccess.addLog("skipping auth because we just did it", logLevel);
			urlModelAndHttpResponse = this.setupWithoutSaml(request, httpResponse, browser, device, company, model);
		} else {
			urlModelAndHttpResponse = new UrlModelAndHttpResponse(next, model, httpResponse);
			try {
				AccessCodeDbObj newAccess = new AccessCodeDbObj(DateTimeUtilities.getCurrentTimestamp(),
						GeneralUtilities.randomString(), company.getCompanyId(), "", accessCode.getDeviceId(),
						accessCode.getPermissions(), true, browser.getBrowserId(), false);
				dataAccess.addAccessCode(newAccess, "installB2fForBrowser");
				FirstFactorSetup firstFactorSetup = new FirstFactorSetup();
				if (company.getF1Method().equals(AuthorizationMethod.SAML)) {
					dataAccess.addLog("company method is SAML", logLevel);
					firstFactorSetup.handleInitialSamlAuth(request, httpResponse, company, newAccess,
							company.getCompanyCompletionUrl(), "setup");
					// return null so we are redirected and not sent to a jsp
					urlModelAndHttpResponse.setUrl(null);
				} else if (company.getF1Method().equals(AuthorizationMethod.LDAP)) {
					dataAccess.addLog("company method is Ldap", logLevel);
					UrlAndModel urlAndModel = firstFactorSetup.handleLdapAuth(request, httpResponse, model, company,
							newAccess, company.getCompanyCompletionUrl(), "setup");
					urlModelAndHttpResponse.setUrl(urlAndModel.getUrl());
					urlModelAndHttpResponse.setModelMap(urlAndModel.getModelMap());
				} else {
					dataAccess.addLog("company method is: " + company.getF1Method(), logLevel);
					urlModelAndHttpResponse = this.setupWithoutSaml(request, httpResponse, browser, device, company,
							model);
				}
			} catch (Exception e) {
				dataAccess.addLog(e);
				urlModelAndHttpResponse.setUrl("/genericFailure?src=" + company.getCompanyCompletionUrl());
			}
		}
		return urlModelAndHttpResponse;
	}

	public UrlModelAndHttpResponse reSignIn(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model, IdentityObjectFromServer idObj, CompanyDataAccess dataAccess) {
		UrlModelAndHttpResponse urlModelAndHttpResponse = null;
		CompanyDbObj company = idObj.getCompany();
		DeviceDbObj device = idObj.getDevice();
		BrowserDbObj browser = idObj.getBrowser();
		dataAccess.addLog("browserId: " + browser.getBrowserId());
		String accessCodeId = GeneralUtilities.randomString();
		AccessCodeDbObj accessCode = new AccessCodeDbObj(accessCodeId, company.getCompanyId(), null,
				device.getDeviceId(), 0, true, browser.getBrowserId(), false);
		dataAccess.addAccessCode(accessCode, "reSignin");
		FirstFactorSetup firstFactorSetup = new FirstFactorSetup();
		try {
			if (company.getF1Method().equals(AuthorizationMethod.SAML)) {
				dataAccess.addLog("company method is SAML");
				firstFactorSetup.handleInitialSamlAuth(request, httpResponse, company, accessCode,
						company.getCompanyCompletionUrl(), "setup");
				// return null so we are redirected and not sent to a jsp
				urlModelAndHttpResponse = new UrlModelAndHttpResponse(null, model, httpResponse);
			} else if (company.getF1Method().equals(AuthorizationMethod.LDAP)) {
				dataAccess.addLog("company method is Ldap");
				UrlAndModel urlAndModel = firstFactorSetup.handleLdapAuth(request, httpResponse, model, company,
						accessCode, company.getCompanyCompletionUrl(), "setup");
				urlModelAndHttpResponse = new UrlModelAndHttpResponse(urlAndModel.getUrl(), urlAndModel.getModelMap(),
						httpResponse);
			} else {
				dataAccess.addLog("company method is: " + company.getF1Method());
				urlModelAndHttpResponse = this.setupWithoutSaml(request, httpResponse, browser, device, company, model);
			}
		} catch (Exception e) {
			urlModelAndHttpResponse = new UrlModelAndHttpResponse("error",
					model.addAttribute("errorMessage", e.getMessage()), httpResponse);
		}
		return urlModelAndHttpResponse;
	}

	private boolean didWeJustAuthenticateOnInstall(DeviceDbObj device, SamlDataAccess dataAccess) {
		boolean recentlyAuthenticated = false;
		SamlAuthnRequestDbObj request = dataAccess.recentlyAuthenticatedOnInstall(device);
		if (request != null) {
			recentlyAuthenticated = true;
			request.setSender("appDone");
			dataAccess.updateSamlAuthRequestByRelayState(request);
		}
		return recentlyAuthenticated;
	}

	private UrlModelAndHttpResponse setupWithoutSaml(HttpServletRequest request, HttpServletResponse httpResponse,
			BrowserDbObj browser, DeviceDbObj device, CompanyDbObj company, ModelMap model) {
		SamlDataAccess dataAccess = new SamlDataAccess();
		AccessCodeDbObj accessCode = new AccessCodeDbObj(GeneralUtilities.randomString(), company.getCompanyId(),
				browser.getBrowserId(), device.getDeviceId(), 0, true, browser.getBrowserId(), false);
		model.addAttribute("accessCode", accessCode.getAccessCode());
		dataAccess.addAccessCode(accessCode, "setupWithoutSaml");
		dataAccess.addLog("adding token with browserId:" + browser.getBrowserId());
		TokenDbObj session = dataAccess.addToken(device, browser.getBrowserId(), TokenDescription.BROWSER_SESSION,
				baseUrl);
		TokenDbObj token = dataAccess.addToken(device, browser.getBrowserId(), TokenDescription.BROWSER_TOKEN, baseUrl);

		String cookieString = session.getTokenId() + "**" + token.getTokenId();
		IdentityObjectFromServer idObj = new IdentityObjectFromServer(company, device, browser, accessCode);
		String nextPage;
		if (device.isMultiUser()) {
			UrlAndModel urlAndModel = new Failure().checkForPushOrBiometrics(model, idObj, dataAccess);
			nextPage = urlAndModel.getUrl();
			model = urlAndModel.getModelMap();
		} else {
			nextPage = "setupJavascript";
			model.addAttribute("submitUrl", company.getCompleteCompanyLoginUrl());
			model.addAttribute("central", device.isCentral());
			model.addAttribute("bleEnabled", device.getHasBle());
			dataAccess.addLog("setting b2fSetup to " + cookieString);
		}
		model.addAttribute("b2fSetup", cookieString);
		RequestAndResponse reqRes = setPersistantToken(request, httpResponse, dataAccess, idObj);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return new UrlModelAndHttpResponse(nextPage, model, reqRes.getResponse());
	}
}
