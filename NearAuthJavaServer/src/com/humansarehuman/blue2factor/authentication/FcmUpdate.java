package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.FCM_UPDATE)
@SuppressWarnings("ucd")
public class FcmUpdate extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed", "");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String fcmUpdateProcessPush(HttpServletRequest request, ModelMap model) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.addLog("received post request");
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "", "");

		String deviceId = "";
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			deviceId = getEncryptedRequestValue(request, "deviceId", key, iv);
			String fcmId = getEncryptedRequestValue(request, "fcmId", key, iv);
			if (fcmId != null && !fcmId.trim().equals("")) {
				DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
				if (device != null) {
					DeviceDbObj oldDevice = dataAccess.getDeviceByFcm(fcmId);
					if (oldDevice != null) {
						if (oldDevice.getDeviceId() != null) {
							if (!oldDevice.getDeviceId().equals(device.getDeviceId())) {
								dataAccess.addLog(deviceId,
										"Device with existing fcm found "
												+ "but different devId.  This is fucked up, and probably caused by "
												+ "a device Id change through testing.",
										LogConstants.ERROR);
								dataAccess.setFcmAndPushFailure(device, fcmId, false);
								response.setReason(fcmId);
								response.setOutcome(Outcomes.SUCCESS);
							} else {
								response.setReason(fcmId);
								response.setOutcome(Outcomes.SUCCESS);
							}
						}
					}
					if (!fcmId.equals(device.getFcmId())) {
						dataAccess.setFcmAndPushFailure(device, fcmId, false);
						response.setReason(fcmId);
						response.setOutcome(Outcomes.SUCCESS);
					} else {
						response.setOutcome(Outcomes.SUCCESS);
						dataAccess.addLog(deviceId, "previous fcmId is the same as this one");
					}
				} else {
					response.setReason("device was not found for id: " + deviceId);
				}
			} else {
				response.setReason("fcm in post was blank");
				dataAccess.addLog(response.getReason(), LogConstants.ERROR);
			}
		} catch (Exception e) {
			dataAccess.addLog(deviceId, e);
			response.setReason(e.getMessage());
		}
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
