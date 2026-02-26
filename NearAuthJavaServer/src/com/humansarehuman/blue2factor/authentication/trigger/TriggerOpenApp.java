package com.humansarehuman.blue2factor.authentication.trigger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.communication.PushNotifications;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.TRIGGER_OPEN_APP)
public class TriggerOpenApp extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	@SuppressWarnings("ucd")
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	@SuppressWarnings("ucd")
	public String triggerOpenAppProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		String deviceId = "";
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "");
		PushNotifications push = new PushNotifications();
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			deviceId = this.getEncryptedRequestValue(request, "thnujensh", key, iv);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId, "triggerOpenAppProcessPost");
			response = push.sendOpenAppPush(device);
		} catch (Exception e) {
			response.setReason(e.getLocalizedMessage());
			dataAccess.addLog(deviceId, e);
		}
		model = this.addBasicResponse(model, response);
		return "result";
	}

}
