package com.humansarehuman.blue2factor.authentication;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.DeviceAndInstanceId;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.JsonUtilities;

/*
 * This is used to get pending requests and device name of the old request that is still valid
 * 
 * */

@Controller
@RequestMapping(Urls.GET_NOTIFICATION)
@SuppressWarnings("ucd")
public class GetNotification extends BaseController {

	@RequestMapping(method = { RequestMethod.POST, RequestMethod.GET })
	public String getNotification(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String instanceId = "";
		int token = 0;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		long expireMillis = 0;
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = getEncryptedRequestValue(request, Parameters.GET_NOTIFICATION_DEV_ID, key, iv);
			if (deviceId != null && deviceId != "null") {
				DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
				if (device != null) {
					ArrayList<DeviceAndInstanceId> pendingRequestDevices = dataAccess.getPendingRequests(device);
					token = pendingRequestDevices.size();
					if (token > 0) {
						DeviceAndInstanceId firstDeviceAndInstanceId = pendingRequestDevices.get(0);
						reason = new JsonUtilities().getDeviceType(firstDeviceAndInstanceId.getPeripheralDevice());
						instanceId = firstDeviceAndInstanceId.getInstanceId();
						try {
							expireMillis = firstDeviceAndInstanceId.getExpireDate().getTime();
							dataAccess.addLog("expireMillis: " + expireMillis);
						} catch (Exception e1) {
							dataAccess.addLog(e1);
						}
					}
					outcome = Outcomes.SUCCESS;
				} else {
					reason = Constants.DEVICE_NOT_FOUND;
					dataAccess.addLog(deviceId, "device not found with ID: " + deviceId, LogConstants.WARNING);
				}
			} else {
				if (deviceId == "null") {
					deviceId = "null text";
				}
				reason = "device id was null";
				dataAccess.addLog(deviceId, "bad device ID: " + deviceId, LogConstants.WARNING);
			}
		} catch (Exception e) {
			dataAccess.addLog("GetNotification", e);
		}
		BasicResponse response = new BasicResponse(outcome, reason, Integer.toString(token), instanceId, expireMillis);
		dataAccess.addLog(response.toString());
		model = this.addBasicResponse(model, response);
		return "resultWithInstanceIdAndTimestamp";
	}

}
