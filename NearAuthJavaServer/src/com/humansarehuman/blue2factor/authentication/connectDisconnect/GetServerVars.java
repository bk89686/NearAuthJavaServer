package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
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
import com.humansarehuman.blue2factor.entities.ConnectionAttrs;
import com.humansarehuman.blue2factor.entities.enums.CheckType;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

/**
 * From a central / client or a central / server
 * 
 * should probably only occur if a window non peripheral is being used
 * 
 * @author cjm
 *
 */

@Controller
@RequestMapping(Urls.GET_SERVER_VARS)
@SuppressWarnings("ucd")
public class GetServerVars extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String getServerVarsPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {

		ArrayList<ConnectionAttrs> connDevices = new ArrayList<ConnectionAttrs>();
		String reason = "";
		String instanceId = GeneralUtilities.randomString(20);
		String deviceId = "";
		boolean fromFcm = false;
		int outcome = Outcomes.FAILURE;
		boolean connected = false;
		boolean showIcon = false;
		boolean connectedToAll = true;
		boolean connecting = false;
		boolean subscribed = false;
		boolean hasBle = false;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		int logLevel = LogConstants.TRACE;
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			deviceId = this.getEncryptedRequestValue(request, "csgs5r45", key, iv);
			@SuppressWarnings("unused")
			String devType = this.getEncryptedRequestValue(request, "ukqkikq", key, iv);
			boolean setup = this.getEncryptedRequestBoolean(request, "aheucsdsro", key, iv, false);
			String serviceUuid = this.getEncryptedRequestValue(request, "usaehull", key, iv);
			fromFcm = this.getEncryptedRequestBoolean(request, "pcgisahnns", key, iv, false);
			boolean computerAwake = this.getEncryptedRequestBoolean(request, Parameters.COMPUTER_AWAKE, key, iv, true);
			DeviceDbObj callingDevice = dataAccess.getDeviceByDeviceId(deviceId, "getServerVarsPost");
			if (callingDevice != null) {
				hasBle = callingDevice.getHasBle();
				String clientIpAddress = GeneralUtilities.getClientIp(request);
				String ssid = this.getEncryptedRequestValue(request, "PiiE4nesuhaesnh5nn", key, iv);
				showIcon = callingDevice.getShowIcon();
				ArrayList<DeviceConnectionDbObj> connections = new ArrayList<>();
				dataAccess.addLog(deviceId, "uuid: " + serviceUuid, logLevel);
				if (!shouldGetAll(serviceUuid)) {
					connections.add(dataAccess.getConnectionByServiceUuid(serviceUuid));
				} else {
					connections = dataAccess.getConnectionsForDevice(callingDevice, true);
				}
				dataAccess.addLog(deviceId, "connected devices found: " + connections.size(), logLevel);
				int i = 0;
				if (connections.size() > 0) {
					outcome = Outcomes.SUCCESS;
					int iteration = 0;
					for (DeviceConnectionDbObj connection : connections) {
						i++;
						if (connection != null) {
							dataAccess.addLog(connection.getPeripheralDeviceId(), "connected device #" + i, logLevel);
							if (connection.isActive() && !TextUtils.isEmpty(connection.getCharacteristicUuid())) {
								this.setInstallComplete(callingDevice, connection, dataAccess);
								if (setup) {
									dataAccess.expireStaleChecks(connection,
											DateTimeUtilities.getCurrentTimestampMinusMinutes(20));
								}
								if (!dataAccess.isAccessAllowed(connection, callingDevice)) {
									connectedToAll = false;
								} else {
									connected = true;
								}
								if (connection.isSubscribed()) {
									subscribed = true;
								}
								if (!connecting && dataAccess.getConnecting(connection)) {
									connecting = true;
								}
								DeviceDbObj connectedDevice = dataAccess
										.getDeviceByDeviceId(connection.getPeripheralDeviceId(), "getServerVarsPost");
								if (connectedDevice != null) {
									dataAccess.addLog(connection.getPeripheralDeviceId(),
											"linked device #" + i + " type = " + connectedDevice.getDeviceType(),
											logLevel);
									ConnectionAttrs connAttrs = addNewCheckFromCentral(connection, callingDevice,
											connectedDevice, instanceId, clientIpAddress, ssid, iteration,
											CheckType.PROX);
									if (connAttrs != null) {
										connDevices.add(connAttrs);
										dataAccess.addLog(connection.getPeripheralDeviceId(),
												"adding connected device, connections: " + connDevices.size(),
												logLevel);
										iteration++;
									} else {
										dataAccess.addLog(connection.getPeripheralDeviceId(),
												"connection attributes were ducking null", LogConstants.WARNING);
										reason = Constants.KEY_NOT_FOUND; // could be other reasons too
									}
								} else {
									reason = Constants.PERIPHERAL_NOT_FOUND;
								}
							}
						}
					}
				} else {
					reason = Constants.CONNECTION_NOT_FOUND;
				}
				dataAccess.updateLastVariableRetrieval(callingDevice, computerAwake);

			} else {
				DeviceDbObj oldDevice = dataAccess.getDeviceByDeviceId("X-" + deviceId, "getServerVarsPost");
				if (oldDevice != null) {
					reason = Constants.RESET;
				} else {
					dataAccess.addLog(deviceId, "Device not found.", logLevel);
					reason = "Device not found.";
				}
			}
		} catch (Exception e) {
			dataAccess.addLog(deviceId, e);
			reason = e.getMessage();
		}
		if (connDevices.size() < 1) {
			outcome = Outcomes.FAILURE;
		}
		dataAccess.addLog(deviceId, "devices: " + connDevices.size(), LogConstants.TRACE);
		model = this.addReturnVars(model, outcome, reason, connDevices, instanceId, connected, subscribed,
				connectedToAll, showIcon, fromFcm, hasBle, connecting);
		return "varDevices";
	}

	private boolean shouldGetAll(String uuidStr) {
		boolean getAll = false;
		if (uuidStr == null || uuidStr.isBlank() || uuidStr.equals("all") || uuidStr.equals("null")) {
			getAll = true;
		}
		return getAll;
	}

	private void setInstallComplete(DeviceDbObj device, DeviceConnectionDbObj connection, DeviceDataAccess dataAccess) {
		if (device.isCentral() && !connection.isInstallComplete()) {
			dataAccess.updateAsInstallComplete(connection);
		}
	}

}
