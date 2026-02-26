package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import jakarta.servlet.http.HttpServletRequest;

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
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.STOP_BACKGROUND)
@SuppressWarnings("ucd")
public class StopBackground extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String stopBackgroundPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, "devId", key, iv);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				device.setTerminate(true);
				dataAccess.updateDevice(device, "stopBackgroundPost");
				outcome = Outcomes.SUCCESS;
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
			}
		} catch (Exception e) {
			reason = e.getLocalizedMessage();
		}

		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";

	}
}
