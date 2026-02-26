package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.REPORT_SLEEPING)
@SuppressWarnings("ucd")
public class ReportSleeping extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String reportSleepingPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, Parameters.REPORT_SLEEPING_DEVICE_ID, key, iv);
			boolean computerAwake = this.getEncryptedRequestBoolean(request, Parameters.REPORT_SLEEPING_COMPUTER_AWAKE,
					key, iv, false);

			dataAccess.addLog(deviceId, "reporting computer awake status as: " + computerAwake,
					LogConstants.TRACE);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				if (dataAccess.updateDeviceAwake(device, computerAwake, false)) {
					if (computerAwake) {
						reason = "we reported that this device is awake";
					} else {
						reason = "we reported that this device is asleep";
					}
					outcome = Outcomes.SUCCESS;
				} else {
					reason = "updateDeviceAwake failed";
				}
			} else {
				dataAccess.addLog(deviceId, Constants.DEV_NOT_FOUND, LogConstants.ERROR);
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
