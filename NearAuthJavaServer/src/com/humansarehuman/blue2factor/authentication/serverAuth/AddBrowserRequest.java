package com.humansarehuman.blue2factor.authentication.serverAuth;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = Urls.ADD_BROWSER_REQUEST)
@SuppressWarnings("ucd")
public class AddBrowserRequest extends B2fApi {
    @RequestMapping(method = RequestMethod.POST)
    public String postJson(HttpServletRequest request, HttpServletResponse httpResponse,
            ModelMap model) throws IOException {
        int outcome = Outcomes.FAILURE;
        String reason = "";
        DeviceDataAccess dataAccess = new DeviceDataAccess();
        try {
            String key = getKey(request);
            String iv = getInitVector(request);
            String deviceId = this.getEncryptedRequestValue(request, "hshcoa", key, iv);
            if (!TextUtils.isBlank(deviceId)) {
                DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
                if (device != null) {
                    dataAccess.setupAddBrowser(device);
                    outcome = Outcomes.SUCCESS;
                } else {
                    reason = Constants.DEVICE_NOT_FOUND;
                }
            } else {
                reason = Constants.DEVICE_ID_WAS_BLANK;
            }
        } catch (Exception e) {
            dataAccess.addLog("AddBrowser", e);
            reason = e.getLocalizedMessage();
        }
        BasicResponse response = new BasicResponse(outcome, reason);
        model = this.addBasicResponse(model, response);
        return "result";
    }

}
