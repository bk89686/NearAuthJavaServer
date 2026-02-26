package com.humansarehuman.blue2factor.authentication.trigger;

import java.sql.Timestamp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

@Controller
@RequestMapping(Urls.TRIGGER_PUSH_RESPONSE)
/**
 * This is form a loud push
 */
public class TriggerPushResponse extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	@SuppressWarnings("ucd")
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	@SuppressWarnings("ucd")
	public String triggerPushResponseProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		String deviceId = "";
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			deviceId = this.getEncryptedRequestValue(request, "thnujensh", key, iv);
			String instance = this.getEncryptedRequestValue(request, "uoiddhfbeyd", key, iv);
			dataAccess.addLog(deviceId, "received response for instance: " + instance, LogConstants.IMPORTANT);
			@SuppressWarnings("unused")
			boolean disconnectSent = this.getEncryptedRequestBoolean(request, "nthauchrbauj", key, iv, false);
			String serviceUuid = this.getEncryptedRequestValue(request, "santehuns", key, iv);
			if (serviceUuid != null) {
				DeviceConnectionDbObj connection = dataAccess.getConnectionByServiceUuid(serviceUuid);
				Timestamp now = DateTimeUtilities.getCurrentTimestamp();
				if (connection != null) {
					CheckDbObj check = dataAccess.getActivePushCheckByCentralInstance(instance);
					if (check != null) {
						check.setCompletionDate(now);
						check.setOutcome(Outcomes.SUCCESS);
						Timestamp oneDay = DateTimeUtilities.getCurrentTimestampPlusDays(1);
						check.setExpirationDate(oneDay);
						dataAccess.updateCheck(check);
						dataAccess.addConnectionLogIfNeeded(check, true, check.getCentralDeviceId(),
								"triggerPushResponseProcessPost", ConnectionType.PROX);
					}
				}
				DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
				if (device != null) {
					dataAccess.setPushResponse(device);
					if (connection != null) {
						outcome = Outcomes.SUCCESS;
					}
				}
			}
		} catch (Exception e) {
			dataAccess.addLog("TriggerPush", e);
			reason = e.getLocalizedMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

//	public void noPushResponse(final DeviceDbObj device, final boolean repeat) {
//		new java.util.Timer().schedule(new java.util.TimerTask() {
//			@Override
//			public void run() {
//				DeviceDataAccess dataAccess = new DeviceDataAccess();
//				if (device != null) {
//					DeviceDbObj updatedDevice = dataAccess.getDeviceByDeviceId(device.getDeviceId());
//					if (updatedDevice != null) {
//						if (updatedDevice.getTriggerUpdate()) {
//							if (dataAccess.isConnecting(updatedDevice)) {
//								dataAccess.addLog(updatedDevice.getDeviceId(),
//										"We are trying, unsuccessfully, to connect. "
//												+ "Will trigger again after delay with new service.",
//										LogConstants.INFO);
//							} else {
//								if (!updatedDevice.getUnresponsive()) {
//									dataAccess.addLog(updatedDevice.getDeviceId(),
//											"We got the trigger but never connected", LogConstants.INFO);
//								} else {
//									dataAccess.addLog(updatedDevice.getDeviceId(),
//											"There seems to be no response to our push : "
//													+ updatedDevice.getUnresponsive(),
//											LogConstants.INFO);
//								}
//							}
//							if (repeat) {
//								triggerDeviceAfterDelay(device);
//							}
//						}
//					} else {
//						dataAccess.addLog("updateDevice was null");
//					}
//				} else {
//					dataAccess.addLog("device was null");
//				}
//			}
//		}, 10000);
//	}
//
//	private void triggerDeviceAfterDelay(DeviceDbObj device) {
//		PushNotifications push = new PushNotifications();
//		push.sendOneSilentPushAfterDelay(device, true, 10, "pushing with new service");
//	}

}
