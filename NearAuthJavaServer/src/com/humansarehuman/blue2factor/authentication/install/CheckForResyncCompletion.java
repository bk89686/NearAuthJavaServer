package com.humansarehuman.blue2factor.authentication.install;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;

@Controller
@RequestMapping(Urls.CHECK_FOR_RESYNC_COMPLETION)
@SuppressWarnings("ucd")
public class CheckForResyncCompletion extends BaseController {
    @RequestMapping(method = RequestMethod.GET)
    public String processGet(HttpServletRequest request, HttpServletResponse httpResponse,
            ModelMap model) {
        BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
        model = this.addBasicResponse(model, response);
        return "result";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String checkForResyncCompletionProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
            ModelMap model) {
        int outcome = Outcomes.FAILURE;
        String reason = "";
        DeviceDataAccess dataAccess = new DeviceDataAccess();
        try {
            String key = getKey(request);
            String iv = getInitVector(request);
            String deviceId = this.getEncryptedRequestValue(request, "devId", key, iv);
            dataAccess.addLog("devId: " + deviceId);
            DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
            if (device != null) {
                TokenDbObj token = dataAccess.getTokenByDeviceAndDescription(deviceId,
                        TokenDescription.RESYNC);
                if (token == null) {
                    outcome = Outcomes.SUCCESS;
                } else {
                    reason = Constants.PROCESSING;
                }
            } else {
                reason = "device not found";
            }
        } catch (Exception e) {
            dataAccess.addLog(e);
        }
        BasicResponse response = new BasicResponse(outcome, reason);
        model = this.addBasicResponse(model, response);
        return "result";
    }

}
