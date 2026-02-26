package com.humansarehuman.blue2factor.authentication.passe;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import com.humansarehuman.blue2factor.authentication.BaseController;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping("/b2f-prox")
@SuppressWarnings("ucd")
public class Factor2CheckRetired extends BaseController {

//	@RequestMapping(method = RequestMethod.GET)
//	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
//		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
//		model = this.addBasicResponse(model, response);
//		return "result";
//	}
//
//	@RequestMapping(method = RequestMethod.POST)
//	public String processPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
//		int outcome = Outcomes.FAILURE;
//		String reason = "";
//		String newToken = "";
//		BasicResponse basicResponse = new BasicResponse(outcome, reason, newToken);
//		DataAccess dataAccess = new DataAccess();
//		try {
//
//			String backendVersion = this.getRequestValue(request, "b2fbe");
//			String coPublic = this.getRequestValue(request, "coId");
//			String token = this.getRequestValue(request, "tok");
//			boolean validate = this.getRequestValue(request, "validate").equals("true");
//			dataAccess.addLog("proximityCheck",
//					"backendVersion: " + backendVersion + "; coPublic: " + coPublic + "; tok: " + token,
//					LogConstants.DEBUG);
//			if (validate) {
//				// this just tells us that the browser received the new token, so we can expire
//				// the
//				// old one
//				if (!TextUtils.isEmpty(token)) {
//					basicResponse = handleValidateB2fId(token);
//				}
//			} else {
//				Boolean fromJs = false;
//				try {
//					fromJs = this.getRequestValue(request, "fromJs").equals("true");
//				} catch (Exception e) {
//					dataAccess.addLog("proximityCheck", "no fromJs variable");
//					dataAccess.addLog("processPost", e);
//				}
//				if (backendVersion.contains("bak")) {
//					// this is from a server
//					basicResponse = this.handleServerRequest(request, token, coPublic, fromJs);
//				} else if (backendVersion.contains("scr")) {
//					// this is from client side script
//					basicResponse = this.handleJsRequest(request, token);
//				} else if (backendVersion.contains("svr")) {
//					String serverId = coPublic;
//					String deviceId = this.getRequestValue(request, "dId");
//					basicResponse = this.handleCoServer(token, deviceId, serverId);
//				} else {
//					dataAccess.addLog("proximityCheck", "illegal backend version: " + backendVersion,
//							LogConstants.DEBUG);
//				}
//			}
//		} catch (Exception e) {
//			dataAccess.addLog("proximityCheck", e);
//		}
//		logNonNullReason(basicResponse);
//		model = this.addBasicResponse(model, basicResponse);
//		return "result";
//	}
//
//	private BasicResponse handleValidateB2fId(String tokenId) {
//		DeviceDataAccess dataAccess = new DeviceDataAccess();
//		dataAccess.addLog("handleValidateB2fId", "handling validateB2f - entrance");
//		int outcome = Outcomes.FAILURE;
//		String reason = "";
//
//		DeviceDbObj device = dataAccess.getDeviceByToken(tokenId);
//		if (device != null) {
//			outcome = Outcomes.SUCCESS;
//			TokenDbObj token = dataAccess.getToken(tokenId);
//			String url = token.getBaseUrl();
//			dataAccess.expireTokensExcept(tokenId, TokenDescription.BROWSER_SESSION, "handleValidateB2fId", url);
//		} else {
//			reason = "device was null";
//		}
//		dataAccess.addLog("handleValidateB2fId", "outcome: " + outcome + ", reason: " + reason);
//		BasicResponse basicResponse = new BasicResponse(outcome, reason, "");
//		return basicResponse;
//	}
//
//	private BasicResponse handleServerRequest(HttpServletRequest request, String token, String coPublic,
//			boolean fromJs) {
//		int outcome = Outcomes.FAILURE;
//		String reason = "";
//		String newToken = "";
//		BasicResponse basicResponse = new BasicResponse(outcome, reason, newToken);
//		CompanyDataAccess dataAccess = new CompanyDataAccess();
//		dataAccess.addLog("handleServerRequest", "tok: " + token);
//		if (token != null && !token.equals("")) {
//			String referrer = this.getRequestValue(request, "referrer");
//			dataAccess.addLog("proximityCheck",
//					"call from server with referrer: " + referrer + " - tok: " + token + " - coPublic: " + coPublic);
//			CompanyDbObj company = dataAccess.getCompanyByPublicKey(coPublic);
//			if (company != null) {
//				String userIp = this.getRequestValue(request, "uip");
//				DeviceDbObj device = dataAccess.getDeviceByToken(token);
//				dataAccess.addLog("proximityCheck", "device null? " + (device == null), LogConstants.DEBUG);
//				if (device != null) {
//					basicResponse = this.validateServerRequest(request, device, token, userIp, company, referrer,
//							fromJs);
//					// TODO: basicResponse = checkForPushResponse(device, basicResponse);
//				} else {
//					basicResponse.setReason(Constants.DEVICE_NOT_FOUND);
//				}
//			} else {
//				dataAccess.addLog("proximityCheck", "call from server: Company is null", LogConstants.DEBUG);
//			}
//		} else {
//			dataAccess.addLog("proximityCheck", "call from server: token is null", LogConstants.DEBUG);
//			basicResponse.setReason(Constants.GENERIC_FAILURE);// Constants.NO_COOKIE);
//		}
//		return basicResponse;
//	}
//
//	private BasicResponse checkForPushResponse(DeviceDbObj device, BasicResponse basicResponse) {
//		DeviceDataAccess dataAccess = new DeviceDataAccess();
//		dataAccess.addLog("checkForPushResponse", "entry", LogConstants.DEBUG);
//		if (basicResponse.getOutcome() != Outcomes.SUCCESS) {
//			if (device != null) {
//				if (device.getDevicePriority() >= 300) {
//					device = dataAccess.getConnectedCentral(device);
//				}
//				dataAccess.addLog("checkForPushResponse", "not successful", LogConstants.DEBUG);
//				if (device.isPushFailure()) {
//					dataAccess.addLog("checkForPushResponse", "pushFailure", LogConstants.DEBUG);
//					basicResponse.setOutcome(Outcomes.PUSH_FAILURE);
//				} else if (device.isPushLoud()) {
//					dataAccess.addLog("checkForPushResponse", "pushLoud", LogConstants.DEBUG);
//					basicResponse.setOutcome(Outcomes.LOUD_PUSH_SENT);
//				}
//			} else {
//				dataAccess.addLog("checkForPushResponse", "how did I get here?", LogConstants.WARNING);
//			}
//		}
//		return basicResponse;
//
//	}
//
//	private BasicResponse handleCoServer(String token, String deviceId, String serverId) {
//		int outcome = Outcomes.FAILURE;
//		String reason = "";
//		BasicResponse basicResponse = new BasicResponse(outcome, reason);
//		DataAccess dataAccess = new DataAccess();
//		dataAccess.addLog("handleCoServer", "handleCoServer - entry");
//		if (token != null && !token.equals("")) {
//			dataAccess.addLog("handleCoServer", "call from server: token found: " + token, LogConstants.DEBUG);
//			basicResponse = dataAccess.isServerConnectionAllowed(token, deviceId, serverId);
//		} else {
//			reason = "You need to run the command in Blue2Factor to access this server.";
//		}
//		return basicResponse;
//	}
//
//	private BasicResponse handleJsRequest(HttpServletRequest request, String token) {
//		int outcome = Outcomes.FAILURE;
//		String reason = "";
//		String newToken = "";
//		BasicResponse basicResponse = new BasicResponse(outcome, reason, newToken);
//		DeviceDbObj incomingDevice = this.setupIncomingDevice(request, token);
//		DeviceDataAccess dataAccess = new DeviceDataAccess();
//		dataAccess.addLog("handleJsRequest", "handleJsRequest - entry");
//		if (token != null && !token.equals("")) {
//			dataAccess.addLog("handleJsRequest", "call from javascript: token found: " + token, LogConstants.DEBUG);
//			DeviceDbObj device = dataAccess.getDeviceByToken(token);
//			if (device != null) {
//				String clientIpAddress = GeneralUtilities.getClientIp(request);
//				String rnd = this.getRequestValue(request, "b2fRd");
//				basicResponse = validateFromJs(incomingDevice, device, clientIpAddress, token, rnd, "");
//				basicResponse = checkForPushResponse(device, basicResponse);
//			} else {
//				dataAccess.addLog("handleJsRequest", "deviceNotFound", LogConstants.DEBUG);
//				basicResponse.setReason(Constants.DEVICE_NOT_FOUND);
//			}
//		} else {
//			dataAccess.addLog("handleJsRequest", "call from javascript: token not found", LogConstants.DEBUG);
//			basicResponse.setReason(Constants.NO_COOKIE);
//		}
//		dataAccess.addLog("handleJsRequest", "handleJsRequest - exit");
//		return basicResponse;
//	}
//
//	private BasicResponse validateServerRequest(HttpServletRequest request, DeviceDbObj device, String tokenId,
//			String userIpAddress, CompanyDbObj company, String referrer, boolean fromJs) {
//		DeviceDataAccess dataAccess = new DeviceDataAccess();
//		int outcome = Outcomes.FAILURE;
//		String reason = "";
//		String newToken = "";
//		dataAccess.addLog("validateServerRequest", "validate server request", LogConstants.DEBUG);
//		if (dataAccess.isProximate(device)) {
//			BrowserDbObj browser = dataAccess.getBrowserByToken(tokenId, TokenDescription.F2_SERVER);
//			TokenDbObj token = dataAccess.addToken(device, browser.getBrowserId(), TokenDescription.F2_SERVER,
//					referrer);
//			newToken = token.getTokenId();
//			outcome = Outcomes.SUCCESS;
//		} else {
//			if (pushCentralIfNeeded(request, device, company, referrer, fromJs)) {
//				outcome = Outcomes.LOUD_PUSH_SENT;
//			}
//			reason = company.getCompanyBaseUrl();
//			;
//		}
//
//		BasicResponse response = new BasicResponse(outcome, reason, newToken);
//		return response;
//	}
//
//	private boolean pushCentralIfNeeded(HttpServletRequest request, DeviceDbObj device, CompanyDbObj company,
//			String referrer, boolean fromJs) {
//		boolean pushSent = false;
//		DeviceDataAccess dataAccess = new DeviceDataAccess();
////		String base = getBaseUrlFromString(referrer);
////		dataAccess.addLog(device.getDeviceId(), "pushCentralIfNeeded", "coBase: " + base, Constants.LOG_DEBUG);
////		String coBase = getBaseUrlFromString(company.getCompanyCompletionUrl());
//		dataAccess.addLog(device.getDeviceId(), "fromJs: " + fromJs, LogConstants.DEBUG);
//
//		if (!fromJs) {
//			DeviceDbObj pushDevice;
//			boolean recentConnectionAttempt = false;
//			if (device.getDevicePriority() < 300) {
//				// this is a central device
//				pushDevice = device;
//				// recentConnectionAttempt = dataAccess.wasRecentCheckByCentral(pushDevice);
//			} else {
//				pushDevice = dataAccess.getConnectedCentral(device);
//				// recentConnectionAttempt = dataAccess.wasRecentCheckByPeripheral(pushDevice);
//			}
//			if (recentConnectionAttempt || !fromJs) {
//				if (pushDevice.getFcmId() != null) {// && pushDevice.isPushLoud()) {
//					dataAccess.addLog(pushDevice.getDeviceId(), "sending loud push", LogConstants.DEBUG);
////					PushNotifications pushNotifications = new PushNotifications();
////					TODO
////					if (pushNotifications.sendLoudPush(pushDevice)) {
////						pushSent = true;
////					}
//				} else {
//					dataAccess.addLog(pushDevice.getDeviceId(), "didn't send push b/c it wasn't wanted",
//							LogConstants.DEBUG);
//				}
//			} else {
//				dataAccess.addLog(device.getDeviceId(),
//						"push wasn't sent because the server hasn't attempted a connection recently",
//						LogConstants.DEBUG);
//			}
//		} else {
//			dataAccess.addLog(device.getDeviceId(),
//					"not sending push because we are coming from the same url (this is not a hard reload)",
//					LogConstants.DEBUG);
//		}
//		return pushSent;
//	}
//
//	@SuppressWarnings("unused")
//	private String getBaseUrlFromString(String sUrl) {
//		String base = "";
//		if (sUrl != null && sUrl != "") {
//			try {
//				URL url = new URL(sUrl);
//				base = url.getProtocol() + "://" + url.getHost();
//			} catch (MalformedURLException e) {
//				new DataAccess().addLog("getBaseUrlFromString :" + sUrl, e);
//			}
//		}
//		return base;
//	}
//
//	@SuppressWarnings("unused")
//	private String buildNewToken() {
//		return GeneralUtilities.randomString(255);
//	}
//
//	private DeviceDbObj setupIncomingDevice(HttpServletRequest request, String token) {
//		DeviceDbObj incomingDevice = new DeviceDbObj();
//		Integer gmtOffset = this.getGmtOffset(request, "gmtOs");
//		String userLanguage = this.getRequestValue(request, "lang");
//		String operatingSystem = this.getRequestValue(request, "userOs");
//		String osVersion = this.getRequestValue(request, "osVer").replaceAll("\\.", "_");
//		String screenSize = this.getRequestValue(request, "ss");
//		String userAgent = this.getRequestValue(request, "ngt");
//		DataAccess dataAccess = new DataAccess();
//		dataAccess.addLog("setupIncomingDevice", "gmtOffset=" + gmtOffset);
//		dataAccess.addLog("setupIncomingDevice", "userLanguage=" + userLanguage);
//		dataAccess.addLog("setupIncomingDevice", "operatingSystem, version=" + operatingSystem + ", " + osVersion);
//		dataAccess.addLog("setupIncomingDevice", "screenSize=" + screenSize);
//		dataAccess.addLog("setupIncomingDevice", "userAgent=" + userAgent);
//		incomingDevice.setUserLanguage(userLanguage);
//		incomingDevice.setLastGmtOffset(gmtOffset);
//		incomingDevice.setOperatingSystem(OsClass.valueOf(operatingSystem.toUpperCase()));
//		incomingDevice.setOsVersion(osVersion);
//		incomingDevice.setScreenSize(screenSize);
//		return incomingDevice;
//	}
}
