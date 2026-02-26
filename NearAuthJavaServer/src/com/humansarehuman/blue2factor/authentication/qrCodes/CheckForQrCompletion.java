package com.humansarehuman.blue2factor.authentication.qrCodes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@CrossOrigin(maxAge = 3600)
@Controller
@RequestMapping(Urls.CHECK_FOR_QR_COMPLETION)
@SuppressWarnings("ucd")
public class CheckForQrCompletion extends BaseController {
    @RequestMapping(method = RequestMethod.GET)
    public String processGet(HttpServletRequest request, HttpServletResponse httpResponse,
            ModelMap model) {
        BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
        model = this.addBasicResponse(model, response);
        return "result";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String checkForQrCompletionProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
            ModelMap model) {
        int outcome = Outcomes.FAILURE;
        String reason = "";
        CompanyDataAccess dataAccess = new CompanyDataAccess();
        try {
            String deviceId = this.getRequestValue(request, "devId");
            dataAccess.addLog("checking if " + deviceId + " is active.");
            DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
            if (device != null && device.isActive()) {
                outcome = Outcomes.SUCCESS;
                CompanyDbObj company = dataAccess.getCompanyByDevId(deviceId);
                reason = company.getCompanyBaseUrl();
                ;
            }
        } catch (Exception e) {
            dataAccess.addLog(e);
        }
        BasicResponse response = new BasicResponse(outcome, reason);
        model = this.addBasicResponse(model, response);
        return "result";
    }

}
