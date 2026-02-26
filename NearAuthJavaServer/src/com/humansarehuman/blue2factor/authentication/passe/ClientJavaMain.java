package com.humansarehuman.blue2factor.authentication.passe;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This is for use on the client server, and not used as part of Blue2Factor. It should not rely on
 * any other parts of the B2F code which is not part of the same code base.
 * 
 * @author cjm10
 *
 */

@Controller
@RequestMapping("/clientJavaMain")
public class ClientJavaMain { // NO_UCD (unused code)

//    @RequestMapping(method = RequestMethod.GET)
//    public String showSamplePage(HttpServletRequest request, HttpServletResponse httpResponse,
//            ModelMap model) {
//
//        return "sample";
//    }
//
//    public String validateFirstAndSecondFactorAuthentication(HttpServletRequest request,
//            String requestedPage, String coKey) {
//        String returnPage;
//        String f1Token = getFactor1Token(request);
//        String f2Token = getFactor2Token(request);
//        String b2fSession = getBrowserToken(request);
//        if (!TextUtils.isBlank(f1Token)) {
//            ValidityResponse validity = getF1AndF2Validity(f1Token, f2Token, b2fSession, coKey);
//            if (validity == ValidityResponse.SUCCESS) {
//                returnPage = null;
//            } else if (validity == ValidityResponse.F1_INVALID) {
//                returnPage = "loadRecheck";
//            } else {
//                returnPage = "loadB2fInstall";
//            }
//        } else {
//            returnPage = "loadGetUserAndPassword";
//        }
//        return returnPage;
//    }
//
//    private ValidityResponse getF1AndF2Validity(String f1Token, String f2Token, String b2fSession,
//            String coKey) {
//        UrlRequest urlRequest = new UrlRequest();
//        ValidityResponse response = urlRequest.verifyTwoFactors(f1Token, f2Token, b2fSession,
//                coKey);
//        return response;
//    }
//
//    private String getFactor1Token(HttpServletRequest request) {
//        return getCookie(request, "b2ff1");
//    }
//
//    private String getFactor2Token(HttpServletRequest request) {
//        return getCookie(request, "b2ff2");
//    }
//
//    private String getBrowserToken(HttpServletRequest request) {
//        return getCookie(request, "b2fsu");
//    }
//
//    public String getCookie(HttpServletRequest request, String cookieName) {
//        String cookieVal = null;
//        Cookie thisCookie = WebUtils.getCookie(request, cookieName);
//        if (thisCookie != null) {
//            cookieVal = thisCookie.getValue();
//        } else {
//            Cookie[] cookies = request.getCookies();
//            if (cookies != null) {
//                for (Cookie cookie : cookies) {
//                    if (cookie.getName().equals(cookieName)) {
//                        cookieVal = cookie.getValue();
//                        break;
//                    }
//                }
//            }
//        }
//        return cookieVal;
//    }
}
