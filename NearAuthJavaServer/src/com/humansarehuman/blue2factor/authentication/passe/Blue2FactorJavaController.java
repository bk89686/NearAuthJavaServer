package com.humansarehuman.blue2factor.authentication.passe;

//@Controller
//@RequestMapping("/completeUserInstall")
public class Blue2FactorJavaController { // NO_UCD (unused code)
//	
//	@RequestMapping(method = RequestMethod.GET)
//	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
//		model = showBlue2FactorPage(request, model, true);
//		httpResponse.addCookie(new Cookie("b2fIdb", (String) model.get("token")));
//		return "blue2factorPage";
//	}
//	
//	@RequestMapping(method = RequestMethod.POST)
//	public String processPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
//		model = showBlue2FactorPage(request, model, false);
//		return "blue2factorPage";
//	}
//	
//	private ModelMap showBlue2FactorPage(HttpServletRequest request, ModelMap model, boolean fromGet) {
//		Blue2FactorJava b2f = new Blue2FactorJava();
//		String deviceId = b2f.getRequestValue(request, "deviceId");
//		String deviceVal = b2f.getRequestValue(request, "deviceKey");
//		String b2fId = b2f.getRequestValue(request, "b2fid");
//		boolean mobile = b2f.getRequestValue(request, "mob").equals("1");
//		boolean mobileInstall = false;
//		boolean fromJs = b2f.getBooleanRequestValue(request, "fromJs");
//		boolean fromQr;
//		if (fromGet && !TextUtils.isBlank(b2fId)) {
//			fromQr = true;
//		} else {
//			fromQr = b2f.getBooleanRequestValue(request, "fromQr");
//		}
//		boolean retry = b2f.getBooleanRequestValue(request, "retry");
//		boolean showOutcome = false;
//		B2fResponse response = null;
//		if (deviceId.equals("")) {
//			//if device id exists then we show a qr code
//			if (!b2fId.equals("") && mobile) {
//				//if this is the case, then it's the end the installation process on a mobile device
//				mobileInstall = true;
//			} else {
//				showOutcome = true;
//				if (b2fId.equals("")) {
//					b2fId = b2f.getB2fCookie(request);
//				}
//				response = b2f.b2fValidated(request);
//			}
//		}
//		model = setModelValues(model, response, deviceId, deviceVal, b2fId, mobileInstall, fromJs, fromQr, showOutcome, retry);
//		return model;
//	}
//	
//	private ModelMap setModelValues(ModelMap model, B2fResponse response, String deviceId, String deviceVal, 
//			String b2fId, boolean mobileInstall, boolean fromJs, boolean fromQr, boolean showOutcome, boolean retry) {
//		model.addAttribute("coId", Blue2FactorJava.COMPANY_KEY);
//		model.addAttribute("fromQr", fromQr);
//		model.addAttribute("fromJs", fromJs);
//		model.addAttribute("mobileInstall", mobileInstall);
//		model.addAttribute("deviceVal", deviceVal);
//		model.addAttribute("deviceId", deviceId);
//		model.addAttribute("showOutcome", showOutcome);
//		if (response != null) {
//			model.addAttribute("token", response.getToken());
//			int outcome = response.getOutcome();
//			String outcomeString = "";
//			if (!fromQr || retry) {
//				outcomeString = getOutcomeHtml(outcome, fromQr);
//			}
//			model.addAttribute("outcomeString", outcomeString);
//			model.addAttribute("outcome", response.getOutcome());
//			String baseUrl = response.getCompletionUrl();
//			if (TextUtils.isBlank(baseUrl)) {
//				baseUrl = "/";
//			}
//			model.addAttribute("baseUrl", response.getCompletionUrl());
//		} else {
//			model.addAttribute("token", b2fId);
//		}
//		return model;
//	}
//	
//	private String getOutcomeHtml(int outcome, boolean fromQr) {
//		String outcomeString = "";
//		switch (outcome) {
//		case 1:
//			outcomeString = SUCCESS;
//			break;
//		case -1:
//			outcomeString = ACCESS_DENIED;
//			break;
//		case -2:
//			outcomeString = BLOCKED;
//			break;
//		case -3:
//			outcomeString = RESYNC;
//			break;
//		case -5:
//			if (!fromQr) {
//				outcomeString = PUSHING;
//			} else {
//				outcomeString = LOOKING;
//			}
//			break;
//		case -6:
//			outcomeString = UNLOCK;
//			break;
//		}
//		return outcomeString;
//	}
//	
//	private static final String SUCCESS = "Success! Blue2Factor 2FA has been successfully setup.";
//	private static final String ACCESS_DENIED = "Access Denied";
//	private static final String BLOCKED = "Blue2Factor has blocked access to this page because you do not " + 
//										"have another registered device that could be found nearby.<br/><br/>To learn more click " + 
//										"<a href='//www.blue2factor.com/help'>here</a>.";
//	private static final String RESYNC = "Blue2Factor needs to be resynched on this browser.<br/><br/>To learn more click " + 
//										"<a href='//www.blue2factor.com/help'>here</a>.";
//	private static final String PUSHING = "A push notification was sent to your phone.  Please respond to access this site.<br/><br/>" +
//										"To learn more click <a href='//www.blue2factor.com/help'>here</a>.";
//	private static final String LOOKING = "We're still looking for another device nearby.<br/><br/>To learn more click " + 
//										"<a href='//www.blue2factor.com/help'>here</a>.";
//	private static final String UNLOCK = "You must open Blue2Factor on your phone to gain access to this site.<br/><br/>To learn more click " + 
////			"<a href='//www.blue2factor.com/help'>here</a>.";
}
