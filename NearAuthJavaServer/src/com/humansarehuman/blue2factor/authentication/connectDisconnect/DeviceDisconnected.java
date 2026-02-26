package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import java.sql.Timestamp;
import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceConnectionDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

@Controller
@RequestMapping(Urls.DEVICE_DISCONNECTED)
@SuppressWarnings("ucd")
public class DeviceDisconnected extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	@SuppressWarnings("ucd")
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed", "");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	@SuppressWarnings("ucd")
	public String deviceDisconnectedProcessPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, "iapniaorguj88", key, iv);
			String serviceUuid = this.getEncryptedRequestValue(request, "saeuchosa34", key, iv);
			String src = this.getEncryptedRequestValue(request, "src", key, iv);
			boolean immediate = this.getEncryptedRequestBoolean(request, "ahukaecs", key, iv, false);
			boolean turnedOff = this.getEncryptedRequestBoolean(request, "nhtns", key, iv, false);
			boolean computerAwake = this.getEncryptedRequestBoolean(request, Parameters.COMPUTER_AWAKE, key, iv, true);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			dataAccess.addLog(
					deviceId, "disconnecting: " + serviceUuid + " from: " + src + " turnedOff: " + turnedOff
							+ ", computerAwake: " + computerAwake + ", immediate: " + immediate,
					LogConstants.IMPORTANT);
			if (device != null) {
				if (serviceUuid != null && serviceUuid.equals("all") && device.isCentral()) {
					if (device.isCentral()) {
						ArrayList<DeviceConnectionDbObj> connections = dataAccess.getAllConnectionsForCentral(device,
								true);
						for (DeviceConnectionDbObj conn : connections) {
							outcome = this.markDisconnected(conn, immediate, "DeviceDisconnected", true);
							dataAccess.addLog(deviceId, "outcome: " + outcome);
						}
					} else {
						DeviceConnectionDbObj connection = dataAccess.getConnectionForPeripheral(device, true);
						outcome = this.markDisconnected(connection, immediate, "DeviceDisconnected", false);
					}
					dataAccess.disconnectQuickAccessForDevice(device);
				} else {
					DeviceConnectionDbObj connection;
					if (device.isCentral()) {
						connection = dataAccess.getConnectionByServiceUuid(serviceUuid);
					} else {
						connection = dataAccess.getConnectionForPeripheral(device.getDeviceId(), true);
						dataAccess.updateDeviceAwake(connection, device, computerAwake, false);
					}
					if (connection != null) {
						dataAccess.disconnectQuickAccessForConnection(device, connection);
						outcome = this.markDisconnected(connection, immediate, "DeviceDisconnected",
								device.isCentral());
					} else {
						dataAccess.addLog(deviceId, "connection was null for service uuid: " + serviceUuid + "!?",
								LogConstants.WARNING);
					}
				}
				if (turnedOff && device.isCentral()) {
					dataAccess.setTurnedOff(device, turnedOff);
				}
			} else {
				dataAccess.addLog("device not found with id: " + deviceId, LogConstants.ERROR);
			}

			BasicResponse response = new BasicResponse(outcome, "");
			model = this.addBasicResponse(model, response);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return "result";
	}

	public int markDisconnected(DeviceConnectionDbObj connection, boolean immediate, String src, Boolean fromCentral) {
//		DeviceConnectionDataAccess dataAccess = new DeviceConnectionDataAccess();
		int outcome = Outcomes.FAILURE;
		Timestamp expireTime = DateTimeUtilities.getCurrentTimestampMinusSeconds(2);
		// Right now we are handling this potential delay on the client
//		if (immediate) {
		if (expire(connection, expireTime, src, fromCentral)) {
			outcome = Outcomes.SUCCESS;
		}
//		} else {
//			// this could be ill advised. Its purpose is to give us some time to
//			// reconnect
//			dataAccess.updateLatestCheckAsSuccessful(connection);
//			expireAfterDelay(connection, expireTime, fromCentral);
//			outcome = Outcomes.SUCCESS;
//		}

		return outcome;
	}

//	private void expireAfterDelay(final DeviceConnectionDbObj connection, final Timestamp expireTime,
//			Boolean fromCentral) {
//		final CompanyDataAccess dataAccess = new CompanyDataAccess();
//		GroupDbObj group = dataAccess.getGroupById(connection.getGroupId());
//		dataAccess.addLog("expiring connection: " + expireTime + " in " + group.getTimeoutSecs() + " seconds ");
//		new java.util.Timer().schedule(new java.util.TimerTask() {
//			@Override
//			public void run() {
//				dataAccess.addLog("scheduled at " + expireTime + " run at " + DateTimeUtilities.getCurrentTimestamp()
//						+ " with time " + group.getTimeoutSecs());
//				// to update the connection times
//				DeviceConnectionDbObj newConnection = dataAccess
//						.getConnectionByConnectionId(connection.getConnectionId());
//				if (dataAccess.didReconnectInTime(expireTime, newConnection)) {
//					dataAccess.addLog(newConnection.getPeripheralDeviceId(), "expire cancelled",
//							LogConstants.IMPORTANT);
//				} else {
//					expire(newConnection, expireTime, "expireAfterDelay", fromCentral);
//					dataAccess.addLog(newConnection.getPeripheralDeviceId(), "expiring after delay",
//							LogConstants.IMPORTANT);
//				}
//			}
//		}, group.getTimeoutSecs() * 1000);
//	}

	private boolean expire(DeviceConnectionDbObj connection, Timestamp expireTime, String src, Boolean fromCentral) {
		DeviceConnectionDataAccess dataAccess = new DeviceConnectionDataAccess();
		boolean success = false;
		try {
			dataAccess.addLog(connection.getConnectionId(), "expiring connection: " + expireTime,
					LogConstants.TRACE);
			dataAccess.expireStaleServiceChecks(connection.getServiceUuid(), expireTime);

			if (connection != null) {
				if (connection.getLastSuccess() != null) {
					if (!connection.getLastSuccess().after(expireTime)) {
						dataAccess.updateAsDisconnected(connection, fromCentral);
					} else {
						dataAccess.addLog(connection.getConnectionId(),
								"not expiring because " + connection.getLastSuccess() + " is after " + expireTime,
								LogConstants.IMPORTANT);
					}
				}
			}
			success = true;
		} catch (Exception e) {
			dataAccess.addLog(connection.getConnectionId(), e);
		}
		return success;
	}
}
