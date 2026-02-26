package com.humansarehuman.blue2factor.authentication.trigger;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.communication.PushNotifications;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.TRIGGER_PUSH)
@SuppressWarnings("ucd")
public class TriggerPush extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGetTriggerPush(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			ArrayList<DeviceDbObj> devices = dataAccess.getAllActiveDevices();
			dataAccess.addLog("push connect all " + devices.size());
			ArrayList<DeviceDbObj> pushDevices = new ArrayList<>();
			for (DeviceDbObj device : devices) {
				if (!StringUtils.isBlank(device.getFcmId())) {
					dataAccess.addLog("fcm found for device: " + device.getDeviceId());
					pushDevices.add(device);
				} else {
					dataAccess.addLog("TriggerPush", "fcm NOT found for device: " + device.getDeviceId());
				}
//				push.sendSilentPushToDevices(pushDevices, "TriggerPush", true);
				outcome = Outcomes.SUCCESS;
			}
		} catch (Exception e) {
			dataAccess.addLog("TriggerPush", e);
			reason = e.getLocalizedMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processPostTriggerPush(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		BasicResponse response = new BasicResponse(outcome, reason);
		String deviceId = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			PushNotifications push = new PushNotifications();
			String key = getKey(request);
			String iv = getInitVector(request);
			getVersion(request);
			deviceId = this.getEncryptedRequestValue(request, "tsauhrc8shbj", key, iv);
			boolean disconnect = this.getEncryptedRequestBoolean(request, "btlekrlchm", key, iv, false);
			boolean pushRegardless = this.getEncryptedRequestBoolean(request, "pushItRealGood", key, iv, false);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId, "processPostTriggerPush");
			if (device != null) {
				response = push.sendPushToAllConnectedDevices(device, "processPostTriggerPush", disconnect, false,
						false, pushRegardless);
			} else {
				dataAccess.addLog(deviceId, "device null in send push", LogConstants.WARNING);
			}
		} catch (Exception e) {
			dataAccess.addLog(deviceId, e);
			response.setReason(e.getLocalizedMessage());
		}
		model = this.addBasicResponse(model, response);
		return "result";
	}

}
