package com.humansarehuman.blue2factor.authentication.fingerprint;

//import java.sql.Timestamp;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.http.util.TextUtils;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.ModelMap;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import com.humansarehuman.blue2factor.authentication.BaseController;
//import com.humansarehuman.blue2factor.constants.Urls;
//import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
//import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
//import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
//import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
//import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
//import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

/**
 * I think this is not used
 * @author cjm10
 *
 */
//@CrossOrigin(origins = "*", allowedHeaders = "*")
//@Controller
//@RequestMapping(Urls.FINGERPRINT_WEBPAGE)
public class FingerprintWebpage {//extends BaseController {
//    @RequestMapping(method = RequestMethod.GET)
//    public String processGet(HttpServletRequest request, HttpServletResponse httpResponse,
//            ModelMap model) {
//        String outputPage = "registration";
//        CompanyDataAccess dataAccess = new CompanyDataAccess();
//        String url = this.getRequestValue(request, "url");
//        String setupToken = getRequestValue(request, "sut");
//        if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(setupToken)) {
//            dataAccess.addLog("FingerprintWebpage", "first time");
//            this.setCookie(httpResponse, url, "previousUrl", 60 * 60, false);
//            TokenDbObj token = dataAccess
//                    .getActiveTokenByDescriptionAndTokenId(TokenDescription.BROWSER_SESSION, setupToken);
//            if (token != null) {
//                dataAccess.addLog("FingerprintWebpage", "accessCode was found");
//                Timestamp accessTimestamp = token.getAuthorizationTime();
//                double seconds = DateTimeUtilities.timeDifferenceInSecondsFromNow(accessTimestamp);
//                if (seconds < 600) {
//                    DeviceDbObj device = dataAccess.getDeviceByDeviceId(token.getDeviceId());
//                    if (device != null) {
//                        CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
//                        baseUrl = company.getCompanyBaseUrl();
//                        dataAccess.addLog("FingerprintWebpage", "setup is working: login: " + url);
//                        String browserId = token.getBrowserId();
//                        dataAccess.addLog("FingerprintWebpage", "browserId:" + browserId);
//                        TokenDbObj browserSession = dataAccess.addToken(device, browserId,
//                                TokenDescription.BROWSER_SESSION, Urls.URL_WITHOUT_PROTOCOL);
//                        TokenDbObj browserToken = dataAccess.addToken(device, browserId,
//                                TokenDescription.BROWSER_TOKEN, Urls.URL_WITHOUT_PROTOCOL);
//                        this.setCookie(httpResponse, url, "previousUrl", 60 * 60 * 24 * 365 * 10,
//                                false);
//                        this.setCookie(httpResponse, browserSession.getTokenId(), "browserSession",
//                                60 * 60 * 24 * 365 * 10, false);
//                        this.setCookie(httpResponse, browserToken.getTokenId(), "browserToken",
//                                60 * 60 * 24 * 365 * 10, false);
//                        outputPage = "redirect:regsb2f";
//                        // redirect to site without parameters in url
//                    }
//                }
//            }
//        } else {
//            dataAccess.addLog("FingerprintWebpage", "second time");
//        }
//        return outputPage;
//    }
}
