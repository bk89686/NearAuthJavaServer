package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.ConnectionAttrs;
import com.humansarehuman.blue2factor.entities.DeviceAndInstanceId;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.CONNECTION_CHECK_BY_UUID)
@SuppressWarnings("ucd")
public class ConnectionCheckByUuid extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String checkConnectionByUuidProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "");
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		model = this.addBasicResponse(model, response);
		ArrayList<ConnectionAttrs> connDevices = new ArrayList<>();
		String reason = "";
		int outcome = Outcomes.FAILURE;
		boolean connected = false;
		boolean showIcon = false;
		boolean subscribed = false;
		boolean connectedToAll = true;
		boolean connecting = false;
		boolean hasBle = false;
		int pendingChecks = 0;
		String serviceUuid = "";
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, "iapniaorguj88", key, iv);
			serviceUuid = this.getEncryptedRequestValue(request, "j5skheiedk", key, iv);
			dataAccess.addLog("deviceId: " + deviceId + ", serviceUuid: " + serviceUuid);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				hasBle = device.getHasBle();
				if (device.getTurnOffFromInstaller()) {
					reason = Constants.TURN_OFF_FROM_INSTALLER;
					device.setTurnOffFromInstaller(false);
				} else {
					dataAccess.updateLastSilentPushResponse(device);
					if (serviceUuid.equals("all")) {
						ArrayList<DeviceConnectionDbObj> connections = dataAccess.getConnectionsForDevice(device,
								true);
						if (connections.size() > 0) {
							outcome = Outcomes.SUCCESS;

							for (DeviceConnectionDbObj connection : connections) {
								if (dataAccess.isAccessAllowed(connection, device)) {
									reason = connection.getServiceUuid();
									connected = true;
									break;
								} else {
									connectedToAll = false;
								}

							}
						} else {
							reason = Constants.CONNECTION_NOT_FOUND;
						}
					} else {
						DeviceConnectionDbObj conn = dataAccess.getConnectionByServiceUuid(serviceUuid);
						if (conn != null) {
							if (dataAccess.isAccessAllowed(conn, device)) {
								connected = true;
								reason = serviceUuid;
							} else {
								connectedToAll = false;
								reason = Constants.NO_PERIPHERALS_ONLINE;
							}
							outcome = Outcomes.SUCCESS;
						} else {
							reason = Constants.CONNECTION_NOT_FOUND;
						}
					}
				}
				showIcon = device.getShowIcon();
				ArrayList<DeviceAndInstanceId> pendingRequestDevices = dataAccess.getPendingRequests(device);
				pendingChecks = pendingRequestDevices.size();
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		model = this.addReturnVars(model, outcome, reason, connDevices, "", connected, subscribed, connectedToAll,
				showIcon, false, hasBle, connecting, pendingChecks);
		return "connectionVars";
	}

}
