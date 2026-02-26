package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.LOOKUP_BLUETOOTH_TYPE)
@SuppressWarnings("ucd")
public class LookupBluetoothType extends BaseController {
    @RequestMapping(method = RequestMethod.GET)
    public String processGet(HttpServletRequest request, HttpServletResponse httpResponse,
            ModelMap model) {
        BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
        model = this.addBasicResponse(model, response);
        return "result";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String lookupBluetoothTypeProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
            ModelMap model) {
        DeviceDataAccess dataAccess = new DeviceDataAccess();
        int outcome = Outcomes.FAILURE;
        String btType = "UNKNOWN";
        String key = getKey(request);
        String iv = getInitVector(request);
        String deviceId = this.getEncryptedRequestValue(request, "crguoe", key, iv);
        DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
        if (device != null) {
            if (device.isCentral()) {
                btType = "CENTRAL";
            } else {
                btType = "PERIPHERAL";
            }
            outcome = Outcomes.SUCCESS;
        }
        BasicResponse response = new BasicResponse(outcome, btType);
        model = this.addBasicResponse(model, response);
        return "result";

    }
}
