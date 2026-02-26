package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import java.sql.Timestamp;

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
import com.humansarehuman.blue2factor.entities.ConnectedAndConnectionType;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.SUBMIT_DECRYPTED_IDS)
@SuppressWarnings("ucd")
public class SubmitDecryptedIds extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String submitDecryptedProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		BasicResponse response;
		String centralInstanceId = "";
		DeviceDbObj device = null;
		String devId = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();

		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			devId = this.getEncryptedRequestValue(request, "sanhu", key, iv);
			centralInstanceId = this.getEncryptedRequestValue(request, "ntsihcrhieua", key, iv);
			String peripheralInstanceId = this.getEncryptedRequestValue(request, "jastnehpchi", key, iv);
			Boolean accept = this.getEncryptedRequestBoolean(request, "esahensuh", key, iv, true);
			boolean computerAwake = this.getEncryptedRequestBoolean(request, Parameters.COMPUTER_AWAKE, key, iv, true);
			dataAccess.addLog(devId, "computerAwake: " + computerAwake, LogConstants.TRACE);
			String bssId = GeneralUtilities.getClientIp(request);
			device = dataAccess.getDeviceByDeviceId(devId);
			if (device != null) {

				if (accept) {
					if (peripheralInstanceId.endsWith("push")) {
						if (!peripheralInstanceId.equals("push")) {
							peripheralInstanceId = peripheralInstanceId.substring(0, peripheralInstanceId.length() - 4);
						} else {
							peripheralInstanceId = null;
						}
						response = handlePushResponse(centralInstanceId, peripheralInstanceId, bssId, device);
					} else {
						response = handleBluetoothConnectionSubmission(centralInstanceId, peripheralInstanceId, bssId,
								device, now);
					}
				} else { // this was a push request that was rejected
					response = rejectCheck(centralInstanceId);
				}
				dataAccess.updateDeviceAwake(device, computerAwake, true);
			} else {
				response = new BasicResponse(Outcomes.FAILURE, Constants.DEV_NOT_FOUND);
				dataAccess.addLog("device was null for id: " + devId);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			response = new BasicResponse(Outcomes.FAILURE, "error: " + e.getMessage());
		}
		dataAccess.addLog(centralInstanceId, "outcome: " + response.getOutcome() + ", reason: " + response.getReason(),
				LogConstants.DEBUG);
		double timeDiff = DateTimeUtilities.timeDifferenceInSecondsFromNow(now);
		dataAccess.addLog("time to run: " + timeDiff);
		model = addBasicResponse(model, response);
		return "result";
	}

	private BasicResponse rejectCheck(String centralId) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		CheckDbObj check = dataAccess.getPushCheckByCentralInstance(centralId);
		if (check != null) {
			check.setExpired(true, "rejectCheck");
			check.setOutcome(Outcomes.FAILURE);
			dataAccess.updateCheck(check);
			outcome = Outcomes.SUCCESS;
			dataAccess.addLog("check found and rejected", LogConstants.WARNING);
		} else {
			reason = "connection wasn't found";
			dataAccess.addLog("rejectCheck", "check found and rejected", LogConstants.WARNING);
		}
		return new BasicResponse(outcome, reason);
	}

	private BasicResponse handlePushResponse(String centralInstanceId, String peripheralId, String bssId,
			DeviceDbObj device) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		CheckDbObj check;
		dataAccess.addLog("peripheralId: " + peripheralId);
		if (!TextUtils.isBlank(peripheralId)) {
			check = dataAccess.getCheckByCentralInstanceAndPeripheralDeviceIds(centralInstanceId, peripheralId);
		} else {
			check = dataAccess.getCheckByCentralInstance(centralInstanceId);
		}

		if (check != null) {
			if (TextUtils.isBlank(peripheralId)) {
				peripheralId = check.getPeripheralDeviceId();
			}
			String centralId = check.getCentralDeviceId();
			dataAccess.addLog(peripheralId, "check found");
			if (!centralId.equals(peripheralId)) {
				DeviceConnectionDbObj conn = dataAccess.getConnectionByDeviceIds(centralId, peripheralId);
				if (conn != null) {
					dataAccess.addLog(peripheralId, "connection found");
					updateConnectionAndCheck(conn, check, bssId, device.isCentral(), ConnectionType.PROX);
					dataAccess.addConnectionLogIfNeeded(conn, device, true, DateTimeUtilities.getCurrentTimestamp(),
							device.getDeviceId(), "handlePushResponse", false, ConnectionType.PROX);
					outcome = Outcomes.SUCCESS;
				} else {
					reason = "connection wasn't found";
				}
			} else {
				reason = "peripheral and centralIds were the same";
			}
		} else {
			reason = "check not found";
		}
		if (!TextUtils.isEmpty(reason)) {
			dataAccess.addLog(peripheralId, reason, LogConstants.WARNING);
		}

		return new BasicResponse(outcome, reason);
	}

	private BasicResponse handleBluetoothConnectionSubmission(String centralInstanceId, String peripheralInstanceId,
			String bssId, DeviceDbObj device, Timestamp now) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		CheckDbObj check = dataAccess.getCheckByDeviceInstanceIds(centralInstanceId, peripheralInstanceId);

		if (check != null) {

			dataAccess.updateLastSilentPushResponse(check.getCentralDeviceId(), check.getPeripheralDeviceId());
			DeviceConnectionDbObj connection = dataAccess.getConnectionByDeviceIds(check.getCentralDeviceId(),
					check.getPeripheralDeviceId());
			DeviceDbObj peripheral;
			if (!device.isCentral()) {
				peripheral = device;
			} else {
				peripheral = dataAccess.getDeviceByDeviceId(connection.getPeripheralDeviceId());
			}
			if (!check.isExpired()) {
				if (connection != null) {
					if (!connection.getCentralConnected() || !connection.getPeripheralConnected()) {
						dataAccess.addLog(device.getDeviceId(), "we connected", LogConstants.IMPORTANT);
					}
					if (peripheral.isActive() && !peripheral.isScreensaverOn() && !peripheral.isTurnedOff()) {
						updateConnectionAndCheck(connection, check, bssId, device.isCentral(), ConnectionType.PROX);
						reason = connection.getServiceUuid();
						dataAccess.addConnectionLogIfNeeded(connection, device, true,
								DateTimeUtilities.getCurrentTimestamp(), device.getDeviceId(),
								"handleBluetoothConnectionSubmission", false, ConnectionType.PROX);
						outcome = Outcomes.SUCCESS;
					} else {
						reason = "peripheral screensaver was on or something";
					}
				} else {
					dataAccess.addLog(device.getDeviceId(), "conn not found", LogConstants.ERROR);
					reason = "connection wasn't found";
				}
			} else {
				Timestamp expirationDate = check.getExpirationDate();
				long lastUpdate = DateTimeUtilities.timestampSecondAgo(expirationDate);
				if (check.isCompleted() && lastUpdate < 300) {
					reason = "this check was already completed";
					reason = this.updateIfPasskeyConnected(dataAccess, device, connection, check, bssId, reason);
					outcome = Outcomes.DUPLICATE;
				} else {
					reason = Constants.EXPIRED_CHECK;
					reason = this.updateIfPasskeyConnected(dataAccess, device, connection, check, bssId, reason);
				}
				if (!hasPreviouslyConnected(dataAccess, connection) && lastUpdate < 300) {
					if (connection != null) {
						if (peripheral.isActive() && !peripheral.isScreensaverOn() && !peripheral.isTurnedOff()) {
							updateConnectionAndCheck(connection, check, bssId, device.isCentral(), ConnectionType.PROX);
						} else {
							reason = "peripheral screensaver was on or something";
						}
					} else {
						reason = Constants.CHECK_NOT_FOUND;
					}
				} else {
					// this is in here because we occasionally run the same check multiple times
					// and I'm wondering if we should update the last connection date, but not the
					// status
					if (connection != null) {
						if (peripheral.isActive() && !peripheral.isScreensaverOn() && !peripheral.isTurnedOff()) {
							dataAccess.updateAsProximatelyConnected(connection, device.isCentral());
							dataAccess.addLog("setting last success = " + now);
						} else {
							reason = "peripheral screensaver was on or something";
						}
					}
				}
			}
		} else {
			reason = "check was not found";
		}
		if (outcome != Outcomes.SUCCESS) {
			dataAccess.addLog(device.getDeviceId(), reason, LogConstants.WARNING);
		}
		return new BasicResponse(outcome, reason);
	}

	/**
	 * This probably should be removed and we can handle it better on the device
	 * side
	 * 
	 * @param dataAccess
	 * @param device
	 * @param connection
	 * @param check
	 * @param bssId
	 * @param reason
	 * @return
	 */
	private String updateIfPasskeyConnected(DeviceDataAccess dataAccess, DeviceDbObj device,
			DeviceConnectionDbObj connection, CheckDbObj check, String bssId, String reason) {
		ConnectedAndConnectionType connectionAndConnType = dataAccess.didGiveAccess(device);
		if (connectionAndConnType.isConnected()) {
			updateConnectionAndCheck(connection, check, bssId, device.isCentral(), ConnectionType.PROX);
			reason = connection.getServiceUuid();
			dataAccess.addConnectionLogIfNeeded(connection, device, true, DateTimeUtilities.getCurrentTimestamp(),
					device.getDeviceId(), "handleBluetoothConnectionSubmission", false, ConnectionType.PROX);
		}
		return reason;
	}

	private boolean hasPreviouslyConnected(DeviceDataAccess dataAccess, DeviceConnectionDbObj connection) {
		boolean previouslyConnected = false;
		Timestamp lastConnection = connection.getLastSuccess();
		if (lastConnection != null) {
			if (DateTimeUtilities.timeDifferenceInSecondsFromNow(lastConnection) < 365 * 24 * 60 * 60) {
				// One year limit because I foolishly use different dates as the default.
				// this should suffice though - cjm
				previouslyConnected = true;
			}
		}
		return previouslyConnected;
	}
}
