package com.humansarehuman.blue2factor.authentication.passe;

import com.humansarehuman.blue2factor.authentication.BaseController;

//@Controller
//@RequestMapping("/complete")
public class InstallationCompletion extends BaseController { // NO_UCD (unused code)
//    @RequestMapping(method = RequestMethod.GET)
//    public String processGet(HttpServletRequest request, HttpServletResponse httpResponse,
//            ModelMap model) {
//        DeviceDbObj device = null;
//        CompanyDataAccess dataAccess = new CompanyDataAccess();
//        String deviceId = this.getRequestValue(request, "deviceId");
//        String token = this.getRequestValue(request, "deviceKey");
//        if (TextUtils.isBlank(deviceId)) {
//            dataAccess.addLog("InstallationComplete", "qrCode blank: ", Constants.LOG_TRACE);
//            if (!TextUtils.isBlank(token)) {
//                device = dataAccess.getDeviceByBrowserId(token);
//            }
//        } else {
//            device = dataAccess.getDeviceByDeviceId(deviceId);
//            dataAccess.addLog("InstallationComplete", "qrCode found: " + deviceId,
//                    Constants.LOG_TRACE);
//        }
//        if (device != null) {
//            device.setLastReset(DateTimeUtilities.getCurrentTimestamp());
//            dataAccess.updateDevice(device, "installationCompletion");
//            CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
//            String baseUrl = company.getCompanyBaseUrl();
//            ;
//            model.addAttribute("peripheralId", deviceId);
//            model.addAttribute("companySigninPage", company.getCompanyLoginUrl());
//            dataAccess.addLog(
//                    device.getDeviceId(), "InstallationComplete", "newToken: " + deviceId
//                            + "; companySigninPage: " + company.getCompanyLoginUrl(),
//                    Constants.LOG_TRACE);
//            httpResponse.setContentType("application/json; charset=utf-8");
//            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
//            httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
//            httpResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT");
//            httpResponse.setHeader("Access-Control-Allow-Origin", baseUrl);
//        } else {
//            dataAccess.addLog("InstallationComplete", "device was null", Constants.LOG_TRACE);
//        }
//        return "installationCompletion";
//    }
//
//    @RequestMapping(method = RequestMethod.POST)
//    public String processPost(HttpServletRequest request, HttpServletResponse httpResponse,
//            ModelMap model) {
//        String qrCode = this.getRequestValue(request, "did");
//        String cookVal = this.getRequestValue(request, "vi");
//        CompanyDataAccess dataAccess = new CompanyDataAccess();
//        DeviceDbObj device = dataAccess.getDeviceByToken(cookVal);
//        if (device != null) {
//            device.setLastReset(DateTimeUtilities.getCurrentTimestamp());
//            dataAccess.updateDevice(device, "installationCompletion");
//            CompanyDbObj company = dataAccess.getCompanyByToken(cookVal);
//            String baseUrl = company.getCompanyBaseUrl();
//            ;
//            model.addAttribute("qrCodeValue", qrCode);
//            model.addAttribute("companySigninPage", company.getCompanyLoginUrl());
//            httpResponse.setContentType("application/json; charset=utf-8");
//            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
//            httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
//            httpResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT");
//            httpResponse.setHeader("Access-Control-Allow-Origin", baseUrl);
//
//            Cookie cookie = new Cookie("b2fIdb", cookVal);
//            cookie.setSecure(true);
//            cookie.setMaxAge(60 * 60 * 24 * 365 * 3);
//            cookie.setPath("/");
//            cookie.setHttpOnly(true);
//            httpResponse.addCookie(cookie);
//        }
//        return "installationCompletion";
//    }
}
