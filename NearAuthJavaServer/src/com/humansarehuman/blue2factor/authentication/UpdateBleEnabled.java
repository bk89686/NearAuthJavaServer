package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.UPDATE_BLE_ENABLED)
@SuppressWarnings("ucd")
public class UpdateBleEnabled extends BaseController {

	@RequestMapping(method = RequestMethod.GET)
	public String updateBleGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String updateBlePost(HttpServletRequest request, ModelMap model) {
		getVersion(request);
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String key = getKey(request);
		String iv = getInitVector(request);
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		String deviceId = this.getEncryptedRequestValue(request, "nuhsbb", key, iv);
		boolean bleEnabled = this.getEncryptedRequestBoolean(request, "scrhhh", key, iv, true);
		dataAccess.addLog("updating bleEnabled to " + bleEnabled + " for " + deviceId);
		try {
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				dataAccess.setHasBle(device, bleEnabled);
				outcome = Outcomes.SUCCESS;
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
