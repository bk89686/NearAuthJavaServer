package com.humansarehuman.blue2factor.authentication.reset;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.EXPIRE_TOKEN_FOR_CENTRAL)
@SuppressWarnings("ucd")
public class ExpireTokensForCentral extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed", "");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String expireTokensProcessPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		String deviceId = "";
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			deviceId = this.getEncryptedRequestValue(request, "ETHLAUCREHU", key, iv);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				DeviceDbObj central = dataAccess.getConnectedCentral(device);
				if (central != null) {
					dataAccess.expireTokensForDevice(central, "ExpireTokens - POST");
					outcome = Outcomes.SUCCESS;
				} else {
					reason = "central not found";
				}
			} else {
				reason = "device was null";
			}
		} catch (Exception e) {
			dataAccess.addLog(deviceId, e);
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason, "");
		model = this.addBasicResponse(model, response);
		return "result";
	}

}
