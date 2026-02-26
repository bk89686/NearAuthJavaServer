package com.humansarehuman.blue2factor.authentication.passe;

import com.humansarehuman.blue2factor.authentication.BaseController;

//@CrossOrigin(origins = "https://www.blue2factor.com", maxAge = 3600)
//@Controller
//@RequestMapping("/ifsc")
public class IframeSetCookie extends BaseController { // NO_UCD (unused code)
//	@RequestMapping(method = RequestMethod.GET)
//	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
//		String b2f = this.getRequestValue(request, "b2f");
//		new DataAccess().addLog("IframeSetCookie", "iFrame set cookie: " + b2f, Constants.LOG_TRACE);
//		model.addAttribute("cookieVal", b2f);
//		model.addAttribute("jsFile", Constants.JS_FILE);
//		httpResponse.setHeader("Access-Control-Allow-Origin", baseUrl);
//		Cookie cookie = new Cookie("b2fIdb", b2f);
//	    cookie.setSecure(true);
//	    cookie.setMaxAge(60 * 60 * 24 * 365 * 3);
//	    cookie.setPath("/");
//	    cookie.setHttpOnly(true);
//	    httpResponse.addCookie(cookie);
//		return "iFrameSetCookie";
//	}
}
