package com.humansarehuman.blue2factor.authentication.install;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.UNINSTALL_DEVICE)
@SuppressWarnings("ucd")
public class UninstallDevice extends B2fApi {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String uninstallDeviceProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String key = getKey(request);
		String iv = getInitVector(request);
		String devId = this.getEncryptedRequestValue(request, Parameters.DEVICE_ID_UNINSTALL, key, iv);
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(devId, "uninstallDeviceProcessPost");
			if (device != null) {
				dataAccess.addLog("device was found");
				dataAccess.deactivateDevice(device);
				dataAccess.deactivateConnectionForDevice(device);
				outcome = Outcomes.SUCCESS;
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
