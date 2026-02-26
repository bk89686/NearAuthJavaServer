package com.humansarehuman.blue2factor.authentication.reset;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.communication.PushNotifications;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.RESET_DEVICES)
@SuppressWarnings("ucd")
public class ResetDevices extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String resetDevicesProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "");
		SamlDataAccess dataAccess = new SamlDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, "comsgmca", key, iv);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				boolean resetTokensOnly = this.getEncryptedRequestBoolean(request, "tanuheht", key, iv, false);
				if (!resetTokensOnly) {
					dataAccess.addLog("ResetDevice", "Will reset devices, browsers, and keys.", LogConstants.IMPORTANT);
					resetDeviceAndConnectedDevices(device);
					dataAccess.deactivateConnections(device);
					dataAccess.expireAllRequestForGroup(device.getGroupId());
					response.setReason(expireKeys(device));
					dataAccess.expireChecksForDevice(device);
					dataAccess.resetBrowsersForGroupId(device.getDeviceId());
					DeviceDbObj central = null;
					ArrayList<DeviceDbObj> connDevices = dataAccess.getActiveDevicesByGroupId(device.getGroupId());
					for (DeviceDbObj connDevice : connDevices) {
						expireKeys(connDevice);
						dataAccess.expireChecksForDevice(device);
						if (connDevice.isCentral()) {
							central = connDevice;
						}
					}
					if (!device.isCentral() && central != null) {
						PushNotifications push = new PushNotifications();
						push.sendResetSilentPush(central);
					}
				}
			} else {
				response.setReason("device was not found");
			}
			response.setOutcome(Outcomes.SUCCESS);
		} catch (Exception e) {
			dataAccess.addLog("ResetDevice", e);
			response.setReason(e.getLocalizedMessage());
		}
		model = this.addBasicResponse(model, response);
		return "result";
	}

	private String expireKeys(DeviceDbObj device) {
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		if (!dataAccess.expireKeysForUser(device.getGroupId())) {
			reason = "error expiring keys";
		}
		return reason;
	}

	private BasicResponse expireTokens(DeviceDbObj device) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		BasicResponse response = new BasicResponse(outcome, reason);
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		if (device != null) {
			dataAccess.addLog("device was not null");
			if (device.isCentral()) {
				if (dataAccess.expireTokensForDevice(device, "resync device called from central")) {
					if (dataAccess.expireChecksForCentral(device, "ResetDevice")) {
						response.setOutcome(Outcomes.SUCCESS);
					} else {
						response.setReason("check expiration failed");
					}
				} else {
					response.setReason("token expiration failed");
				}
				dataAccess.addLog(device.getDeviceId(), "outcome: " + outcome + ", reason: " + reason);
			} else {
				device = dataAccess.getConnectedCentral(device);
				response = expireTokens(device);
			}
		} else {
			dataAccess.addLog("device was null", LogConstants.WARNING);
		}
		return response;
	}

	private void resetDeviceAndConnectedDevices(DeviceDbObj device) {
		PushNotifications push = new PushNotifications();
		push.sendPushToAllConnectedDevices(device, "resetDeviceAndConnectedDevices", true, false, true, false);
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		expireTokens(device);
		dataAccess.deactivateAllDevicesForGroup(device.getGroupId());
		dataAccess.deactivateAllConnectionsForGroup(device.getGroupId());
		ArrayList<DeviceConnectionDbObj> conns = dataAccess.getConnectionsForGroupId(device.getGroupId());
		// ConnectionLogDbObj
		for (DeviceConnectionDbObj conn : conns) {
			dataAccess.addConnectionLogIfNeeded(conn, false, device.getDeviceId(), "resetDeviceAndConnectedDevices",
					null);
		}
		dataAccess.addLog(device.getDeviceId(), "all  connections were reset", LogConstants.IMPORTANT);

	}
}
