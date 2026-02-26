package com.humansarehuman.blue2factor.authentication.failures;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.ui.ModelMap;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.authentication.browserSetup.BrowserInstallation;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.UrlAndModel;
import com.humansarehuman.blue2factor.entities.UrlModelAndHttpResponse;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.jwt.JsonWebToken;

public class Failure extends B2fApi {
//    String tempSession = null;

	protected UrlModelAndHttpResponse checkForAuth(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model, IdentityObjectFromServer idObj, CompanyDataAccess dataAccess, String authToken)
			throws IOException {
		String nextPage = "needsResync";
		int logLevel = LogConstants.TRACE;
		if (idObj != null) {
			DeviceDbObj device = idObj.getDevice();
			if (device != null) {
				BrowserDbObj browser = idObj.getBrowser();
				if (browser != null && !browser.isExpired()) {
					dataAccess.addLog("devExists, browserId: " + browser.getBrowserId(), logLevel);
					if (!device.getSignedIn()) {
						dataAccess.addLog(device.getDeviceId(), "not signed in", logLevel);
						new BrowserInstallation().reSignIn(request, httpResponse, model, idObj, dataAccess);
						nextPage = null;
					} else {
						CompanyDbObj company = idObj.getCompany();
						String submitUrl = getReferrer(request, company, dataAccess);
						dataAccess.addLog(device.getDeviceId(), "signed in", logLevel);
						if (dataAccess.isAccessAllowed(device, "checkForAuth")) {
							if (dataAccess.urlMatchesCompany(company, submitUrl)) {
								dataAccess.addLog(device.getDeviceId(), "thumbs up", logLevel);
								UrlAndModel urlAndModel = returnToSenderViaForm(submitUrl, model, idObj, dataAccess);
								nextPage = urlAndModel.getUrl();
								model = urlAndModel.getModelMap();
							} else {
								dataAccess.addLog("url doesn't match", logLevel);
							}
						} else {
							dataAccess.addLog(device.getDeviceId(), "thumbs down", LogConstants.WARNING);
							if (dataAccess.deviceIsTemp(device)) {
								dataAccess.addLog(device.getDeviceId(), "device is temp", logLevel);
								if (company != null) {
									model.addAttribute("company", company.getCompanyId());
									nextPage = "notSignedUp";
								}
							} else {
								dataAccess.addLog(device.getDeviceId(), "looking to push or text");
								UrlAndModel urlAndModel = checkForPushOrBiometrics(model, idObj, dataAccess);
								model = urlAndModel.getModelMap();
								nextPage = urlAndModel.getUrl();
								model.addAttribute("fromIdp", false);
							}
						}
					}
				} else {
					dataAccess.addLog("the browser was null or expired", logLevel);
					CompanyDbObj company = idObj.getCompany();
					if (company != null) {
						model.addAttribute("company", company.getCompanyId());
					}
					nextPage = "notSignedUp";
				}
			} else {
				dataAccess.addLog("device was null or inactive", logLevel);
				CompanyDbObj company = idObj.getCompany();
				if (company != null) {
					model.addAttribute("company", company.getCompanyId());
				}
				nextPage = "notSignedUp";
			}
		} else {
			dataAccess.addLog("idobj was null", logLevel);
		}
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		dataAccess.addLog("nextPage: " + nextPage);
		UrlModelAndHttpResponse urlModelAndHttpResponse = new UrlModelAndHttpResponse(nextPage, model, httpResponse);
		return urlModelAndHttpResponse;
	}

	protected UrlAndModel handleFailureWithIdObj(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model, IdentityObjectFromServer idObj, CompanyDataAccess dataAccess, String authToken,
			String session) {
		String nextPage = "needsResync";
		int logLevel = LogConstants.TRACE;
		DeviceDbObj device = idObj.getDevice();
		if (isTempBrowser(idObj.getBrowser())) {
			dataAccess.addLog("authToken: " + authToken, logLevel);
			CompanyDbObj company = idObj.getCompany();
			if (company == null && !TextUtils.isBlank(session)) {
				company = dataAccess.getCompanyByToken(session);
				idObj.setCompany(company);
			}
			if (company != null) {
				if (device != null) {
					if (device.isMultiUser()) {
						nextPage = "publicDevice";
					}
				}
				model.addAttribute("company", idObj.getCompany().getCompanyId());
				nextPage = "failure";
			}
		} else {
			if (device != null) {
				if (device.isMultiUser()) {
					nextPage = "publicDevice";
				} else {
					dataAccess.addLog("you are on the most common path", logLevel);
					try {
						UrlAndModel urlAndModel = checkForAuth(request, httpResponse, model, idObj, dataAccess,
								authToken);
						nextPage = urlAndModel.getUrl();
						dataAccess.addLog("nextPage: " + nextPage, logLevel);
						model = urlAndModel.getModelMap();
					} catch (Exception e) {
						nextPage = "error";
						model.addAttribute("errorMessage", e.getMessage());
					}
				}
			}
		}
		return new UrlAndModel(nextPage, model);
	}

	protected boolean isTempBrowser(BrowserDbObj browser) {
		boolean temp = false;
		if (browser != null && browser.getDescription() != null && browser.getDescription().equals("temp")) {
			temp = true;
		}
		return temp;
	}

	protected UrlAndModel returnToSenderViaForm(String submitUrl, ModelMap model, IdentityObjectFromServer idObj,
			CompanyDataAccess dataAccess) {
		dataAccess.addLog("submitUrl: " + submitUrl);
		String jwt = new JsonWebToken().buildJwt(idObj);
		model.addAttribute("submitUrl", submitUrl);
		model.addAttribute("jwt", jwt);
		boolean central = idObj.getDevice().isCentral();
		model.addAttribute("central", central);
		UrlAndModel urlAndModel = this.reSetup(model, idObj, dataAccess);
		model = urlAndModel.getModelMap();
		dataAccess.addLog("sending to resetJwt to forward to " + submitUrl);
		return new UrlAndModel("resetJwt", model);
	}

//	protected UrlAndModel returnToSenderViaForm(String submitUrl, ModelMap model, IdentityObjectFromServer idObj,
//			DeviceConnectionDbObj connection, String authToken, CompanyDataAccess dataAccess) {
//		dataAccess.addLog("submitUrl: " + submitUrl);
//		String jwt = new JsonWebToken().buildJwt(idObj);
//		model.addAttribute("submitUrl", submitUrl);
//		model.addAttribute("jwt", jwt);
//		boolean central = idObj.getDevice().isCentral();
//		model.addAttribute("central", central);
//		if (isFirstFailureWithoutBle(idObj.getDevice(), connection, authToken, dataAccess)) {
//			UrlAndModel urlAndModel = this.reSetup(model, idObj, dataAccess);
//			model = urlAndModel.getModelMap();
//		} else {
//			KeyType keyType = KeyType.B2F_SERVER_PRIVATE_KEY_FOR_BROWSER;
//			if (idObj.getBrowser() != null) {
//				KeyDbObj key = dataAccess.getKeyByTypeAndBrowserAndNotOurUrl(keyType, idObj.getBrowser());
//				if (key == null) {
//					// they never finished setting up
//					dataAccess.addLog("completing setup");
//					UrlAndModel urlAndModel = this.reSetup(model, idObj, dataAccess);
//					model = urlAndModel.getModelMap();
//				}
//			}
//		}
//		dataAccess.addLog("sending to resetJwt to forward to " + submitUrl);
//		return new UrlAndModel("resetJwt", model);
//	}

//	private boolean isFirstFailureWithoutBle(DeviceDbObj device, DeviceConnectionDbObj connection, String authToken,
//			CompanyDataAccess dataAccess) {
//		boolean firstWithoutBle = false;
//		BrowserDbObj browser = dataAccess.getBrowserByToken(authToken, TokenDescription.AUTHENTICATION);
//		if (!browser.getHasFailed()) {
//			if (device.getHasBle()) {
//				DeviceDbObj otherDevice = dataAccess.getOtherDeviceInConnection(device, connection);
//				if (!otherDevice.getHasBle()) {
//					firstWithoutBle = true;
//				}
//			} else {
//				firstWithoutBle = true;
//			}
//			browser.setHasFailed(true);
//			dataAccess.updateBrowser(browser);
//		}
//		dataAccess.addLog("firstWithoutBle: " + firstWithoutBle);
//		return firstWithoutBle;
//	}

//	protected String reSignin(HttpServletRequest request, HttpServletResponse httpResponse,
//			IdentityObjectFromServer idObj, CompanyDataAccess dataAccess) {
//		return reSignin(request, httpResponse, idObj.getDevice(), idObj.getBrowser(), dataAccess);
//	}

//	protected String reSignin(HttpServletRequest request, HttpServletResponse httpResponse, DeviceDbObj device,
//			CompanyDataAccess dataAccess) {
//		return reSignin(request, httpResponse, device, null, dataAccess);
//	}
//

	protected UrlAndModel addNewToken(ModelMap model, IdentityObjectFromServer idObj, CompanyDataAccess dataAccess) {
		dataAccess.addLog("start");
		String nextPage = "resetJwt";
		try {
			if (idObj.getBrowser() != null) {
				String browserId = idObj.getBrowser().getBrowserId();
				dataAccess.addLog("adding token with browserId:" + browserId);
				TokenDbObj session = dataAccess.addToken(idObj.getDevice(), browserId, TokenDescription.BROWSER_SESSION,
						baseUrl);
				TokenDbObj token = dataAccess.addToken(idObj.getDevice(), browserId, TokenDescription.BROWSER_TOKEN,
						baseUrl);
				String cookieString = session.getTokenId() + "**" + token.getTokenId() + "**false";
				model.addAttribute("b2fSetup", cookieString);
				model.addAttribute("central", idObj.getDevice().isCentral());
				dataAccess.addLog("sending to resetJwt");
				String jwt = new JsonWebToken().buildExpiredJwt(
						new IdentityObjectFromServer(idObj.getCompany(), idObj.getDevice(), idObj.getBrowser(), false));
				dataAccess.addLog("jwt: " + jwt);
				model.addAttribute("jwt", jwt);
				model.addAttribute("submitUrl", idObj.getCompany().getCompleteCompanyLoginUrl());

				nextPage = "resetJwt";
			} else {
				model.addAttribute("errorMessage", "browser was null");
				nextPage = "error";
			}
		} catch (Exception e) {
			dataAccess.addLog("error");
			model.addAttribute("errorMessage", e.getMessage());
			nextPage = "error";
		}
		return new UrlAndModel(nextPage, model);
	}

	public String getReferrer(HttpServletRequest request, CompanyDbObj company, CompanyDataAccess dataAccess) {
		String src = this.getSession(request, "referrer");
		if (TextUtils.isEmpty(src)) {
			src = this.getSession(request, "url");
			if (TextUtils.isEmpty(src)) {
				src = company.getCompleteCompanyLoginUrl();
			}
		}
		if (!dataAccess.urlMatchesCompany(company, src)) {
			dataAccess.addLog("trying to set up on bad urlQ " + src, LogConstants.ERROR);
			src = null;
		}
		dataAccess.addLog(src);
		return src;
	}

//	public String getReferrerAndClear(HttpServletRequest request, CompanyDbObj company, DataAccess dataAccess) {
//		String src = this.getSession(request, "referrer");
//		if (TextUtils.isEmpty(src)) {
//			src = this.getSession(request, "url");
//			// this is sloppy - cjm
//			if (TextUtils.isEmpty(src)) {
//				src = company.getCompanyLoginUrl();
//			} else {
//				clearReferrer(request);
//			}
//		} else {
//			clearReferrer(request);
//		}
//		dataAccess.addLog("getReferrerAndClear", src);
//		return src;
//	}

	public HttpServletResponse returnToSender(HttpServletRequest request, HttpServletResponse response,
			CompanyDbObj company, CompanyDataAccess dataAccess) throws IOException {
		String src = getReferrer(request, company, dataAccess);
		request = clearReferrer(request);
		dataAccess.addLog("sending to: " + src);
		response.sendRedirect(src);
		dataAccess.addLog("did it work?");
		return response;
	}

	protected HttpServletRequest setReferrer(HttpServletRequest request, String source) {
		new DataAccess().addLog("referrer set to " + source);
		if (!TextUtils.isEmpty(source)) {
			this.setTempSession(request, "referrer", source, 60 * 60 * 12);
		}
		return request;
	}

	public HttpServletRequest clearReferrer(HttpServletRequest request) {
		new DataAccess().addLog("comment out clearReferrer for now");
//		this.setTempSession(request, "referrer", "", 0);
//		this.setTempSession(request, "url", "", 0);
		return request;
	}

}
