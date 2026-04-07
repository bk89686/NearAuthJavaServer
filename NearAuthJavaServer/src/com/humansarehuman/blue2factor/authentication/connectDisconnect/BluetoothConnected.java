package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;

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
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.BLUETOOTH_CONNECTED)
@SuppressWarnings("ucd")
public class BluetoothConnected extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String bluetoothConnectedPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String deviceId = "";
		String serviceUuid = "";
		String peripheralReference = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			deviceId = this.getEncryptedRequestValue(request, Parameters.BLUETOOTH_CONNECTED_DEVICE_ID, key, iv);
			serviceUuid = this.getEncryptedRequestValue(request, Parameters.BLUETOOTH_CONNECTED_UUID, key, iv);
			boolean connected = this.getEncryptedRequestBoolean(request, Parameters.BLUETOOTH_CONNECTED_CONNECTED, key,
					iv, false);
			peripheralReference = this.getEncryptedRequestValue(request, Parameters.BLUETOOTH_CONNECTED_CENTRAL_OS_ID,
					key, iv);
			boolean computerAwake = this.getEncryptedRequestBoolean(request, Parameters.COMPUTER_AWAKE, key, iv, true);
			String src = this.getEncryptedRequestValue(request, Parameters.BLUETOOTH_CONNECTED_SOURCE, key, iv);
			if (src == null) {
				src = "an unknown source";
			}
			if (!TextUtils.isBlank(serviceUuid)) {
				DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
				DeviceConnectionDbObj connection = dataAccess.getConnectionByServiceUuid(serviceUuid);
				if (TextUtils.isBlank(peripheralReference) && connection == null) {
					handleConnectionUpdate(request, key, iv, dataAccess, connection, device,
							peripheralReference, connected, src);
					outcome = Outcomes.SUCCESS;
					reason = "peripheralReference was blank";
				} else {
					if (connection != null) {
						if (!peripheralReference.equals(connection.getPeripheralIdentifier())) {
							if (TextUtils.isBlank(connection.getPeripheralIdentifier())) {
								dataAccess.addLog(
										"the peripheral_id was blank in the connection table. We are going to update it, "
												+ "but this could represent a small security hole.",
										LogConstants.WARNING);
								dataAccess.updateDeviceAwake(connection, device, computerAwake, true);
								handleConnectionUpdate(request, key, iv, dataAccess, connection, device,
										peripheralReference, connected, src);
								outcome = Outcomes.SUCCESS;
							} else {
								if (peripheralReference != "null") {
									reason = "serviceUUID: " + serviceUuid + " and peripheral reference: "
											+ peripheralReference + " do not match. It should have been "
											+ connection.getPeripheralIdentifier() + " This sucks -- from " + src;
									dataAccess.addLog(deviceId, "bluetoothConnectedPost", reason, LogConstants.ERROR);
									HashMap<String, Object> hm = new HashMap<String, Object>();
									if (peripheralReference != null && !peripheralReference.equals("null")) {								hm.put("PERIPHERAL_IDENTIFIER", "");
										dataAccess.updateConnectionMap(connection, hm, "bluetoothConnectedPost");
									}
								} else {
								reason = Constants.NEEDS_RESCAN;
								}
							}
						} else {

							if (device != null) {
								dataAccess.updateDeviceAwake(connection, device, computerAwake, true);
								handleConnectionUpdate(request, key, iv, dataAccess, connection, device,
										peripheralReference, connected, src);
								outcome = Outcomes.SUCCESS;
							} else {
								dataAccess.addLog(deviceId, "device not found for deviceId " + deviceId,
										LogConstants.ERROR);
								reason = Constants.DEV_NOT_FOUND;
							}
						}
					} else {
						dataAccess.addLog(deviceId, "connection not found for service " + serviceUuid,
								LogConstants.ERROR);
						reason = Constants.CONNECTION_NOT_FOUND;
					}
				}
			} else {

				reason = "serviceUuid was null";
			}
		} catch (Exception e) {
			dataAccess.addLog(deviceId, e);
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	private void handleConnectionUpdate(HttpServletRequest request, String key, String iv, DeviceDataAccess dataAccess,
			DeviceConnectionDbObj connection, DeviceDbObj device, String peripheralReference, boolean connected,
			String src) {
		String connectionStr = "disconnected";
		if (connected) {
			connectionStr = "connected";
		}
		dataAccess.addLog(device.getDeviceId(),
				"updating service " + connection.getServiceUuid() + " as " + connectionStr + " from " + src,
				LogConstants.IMPORTANT);
		if (device.isCentral()) {
			handleCentralConnection(dataAccess, connection, device, peripheralReference, connected);
		} else {
			handlePeripheralConnection(dataAccess, connection, device, peripheralReference, connected);
		}
		if (connected) {
			dataAccess.updateLastSilentPushResponse(device);
		}
		handleUnsuccessfulConnections(request, connection, connected, key, iv);
	}

	private void handleCentralConnection(DeviceDataAccess dataAccess, DeviceConnectionDbObj connection,
			DeviceDbObj device, String peripheralReference, boolean connected) {
		// this could create a small security problem. It is in here to
		// encourage
		// faster connecting on the initial signup - cjm
		if (connected && !dataAccess.hasPreviouslyConnected(connection)) {
			dataAccess.updateAsProximatelyConnected(connection, peripheralReference, true);
		} else {
			dataAccess.addLog("connected: " + connected);
			dataAccess.updateCentralProximateConnection(connection, connected);
			if (connected) {
				dataAccess.expirePushAndBioChecksForCentralDevice(device);
			}
		}
	}

	private void handlePeripheralConnection(DeviceDataAccess dataAccess, DeviceConnectionDbObj connection,
			DeviceDbObj device, String peripheralReference, boolean connected) {
		if (connected && !dataAccess.hasPreviouslyConnected(connection)) {
			dataAccess.updateAsProximatelyConnected(connection, null, false);
		} else {
			dataAccess.addLog("subscribed: " + connected, LogConstants.TRACE);
			dataAccess.updatePeripheralConnected(connection, connected);
			if (connected) {
				dataAccess.expirePushAndBioChecksForPeripheralDevice(device);
			}
		}
	}
}
