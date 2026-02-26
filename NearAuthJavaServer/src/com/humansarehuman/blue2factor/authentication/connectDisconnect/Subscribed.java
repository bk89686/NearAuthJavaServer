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
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

/*
 * I believe this should always come from the peripheral
 */
@Controller
@RequestMapping(Urls.SUBSCRIBED)
@SuppressWarnings("ucd")
public class Subscribed extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String subscribedGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String subscribedPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		getVersion(request);
		String key = getKey(request);
		String iv = getInitVector(request);
		String deviceId = this.getEncryptedRequestValue(request, Parameters.DEV_ID_SUBSCRIBED, key, iv);
		boolean subscribed = this.getEncryptedRequestBoolean(request, Parameters.SUBSCRIBED_SUBSCRIBED, key, iv, false);
		String serviceUuid = this.getEncryptedRequestValue(request, Parameters.SERVICE_UUID_SUBSCRIBED, key, iv);
		boolean computerAwake = this.getEncryptedRequestBoolean(request, Parameters.COMPUTER_AWAKE, key, iv, false);
		dataAccess.addLog("subscribed: " + subscribed + ", computerAwake: " + computerAwake);
		try {
			if (serviceUuid != null) {
				DeviceConnectionDbObj connection = dataAccess.getConnectionByServiceUuid(serviceUuid);
				DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
				if (device != null) {
					if (connection != null) {
						if (device.isCentral()) {
							// this could create a small security problem. It is in here to
							// encourage
							// faster connecting on the initial signup - cjm
							if (subscribed && !dataAccess.hasPreviouslyConnected(connection)) {
								dataAccess.updateAsCentralSubscribed(connection);
							} else {
								dataAccess.updateCentralProximateConnection(connection, subscribed);
								if (dataAccess.isAccessAllowed(connection, device, true)) {
									dataAccess.expirePushAndBioChecksForPeripheralDevice(device);
								} else {
									reason = Constants.NO_RECENT_COMMUNICATION;
								}
							}
							dataAccess.setPushFailed(device, false);
						} else {
							if (subscribed) {
								if (computerAwake) {
									dataAccess.updateAsPeripheralSubscribed(connection, device);
								}
							} else {
								dataAccess.updateAsUnsubscribed(connection, device, false);
							}
							if (!subscribed || dataAccess.hasPreviouslyConnected(connection)) {
								dataAccess.addLog("subscribed: " + subscribed);
								if (dataAccess.isAccessAllowed(connection, device, true)) {
									dataAccess.expirePushAndBioChecksForPeripheralDevice(device);
								} else {
									reason = Constants.NO_RECENT_COMMUNICATION;
								}
							}
							dataAccess.updateDeviceAwake(connection, device, computerAwake, true);
						}
						if (subscribed) {
							dataAccess.updateLastSilentPushResponse(device);
						}
						handleUnsuccessfulConnections(request, connection, subscribed, key, iv);
						outcome = Outcomes.SUCCESS;
					} else {
						reason = Constants.CONNECTION_NOT_FOUND;
					}
				} else {
					reason = Constants.DEVICE_NOT_FOUND;
				}
			} else {
				dataAccess.addLog("service was null", LogConstants.WARNING);
				reason = Constants.SERVICE_UUID_WAS_NULL;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";

	}

//	private void updateAsCentralSubscribed(DeviceDataAccess dataAccess, DeviceConnectionDbObj connection) {
//		// CJM - TODO update CentralSubscribed
//		dataAccess.addLog("BluetoothConnected", "firstConnection if true: " + true);
//		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
//		connection.setLastSuccess(now, "BluetoothConnected, firstTime");
//		connection.setLastCentralConnectionSuccess(now);
//		connection.setCentralConnected(true, "updateAsCentralSubscribed");
//		connection.setSubscribed(true);
//		connection.setLastSubscribed(now);
//		dataAccess.updateConnection(connection, "BluetoothConnected, firstTime");
//	}

//	private void updateAsPeripheralSubscribed(DeviceDataAccess dataAccess, DeviceConnectionDbObj connection) {
//		// CJM - TODO update PeripheralSubscribed
//		dataAccess.updateAsPeripheralSubscribed(connection);
//		dataAccess.addLog("BluetoothConnected", "firstConnection if true: " + true);
//		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
//		connection.setLastSuccess(now, "BluetoothConnected, firstTime");
//		connection.setLastPeripheralConnectionSuccess(now);
//		connection.setPeripheralConnected(true);
//		connection.setSubscribed(true);
//		connection.setLastSubscribed(now);
//		dataAccess.updateConnection(connection, "BluetoothConnected, firstTime");
//	}
}
