package com.humansarehuman.blue2factor.authentication.reset;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.RESET_ONE_DEVICE)
@SuppressWarnings("ucd")
public class ResetOneDevice extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String resetOneDeviceProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, "Ssbntbne", key, iv);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				device.setActive(false);
				device.setBrowserInstallComplete(false);
				dataAccess.setActiveAndInstallComplete(device, false, false);
				dataAccess.updateQuickAccess(device, false);
				DeviceConnectionDbObj conn = dataAccess.getConnectionForPeripheral(device, true);
				if (conn != null) {
					dataAccess.updateAsInactive(conn);
				}
				outcome = Outcomes.SUCCESS;
			} else {
				reason = "device was not found";
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
