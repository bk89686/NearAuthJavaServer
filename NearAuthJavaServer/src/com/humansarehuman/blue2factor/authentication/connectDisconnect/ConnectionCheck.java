package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import java.sql.Timestamp;
import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONObject;
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
import com.humansarehuman.blue2factor.entities.DeviceAndInstanceId;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

/**
 * This is coming from a peripheral server
 * 
 * @author cjm10
 *
 */
@Controller
@RequestMapping(Urls.CONNECTION_CHECK)
@SuppressWarnings("ucd")
public class ConnectionCheck extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processConnectionCheckPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		JSONObject varJson = new JSONObject();
		String reason = "";
		String instanceId = "";
		String deviceId = "";
		int i = 0;
		int outcome = Outcomes.FAILURE;
		boolean connected = false;
		boolean showIcon = false;
		boolean connectedToAll = true;
		boolean subscribed = false;
		boolean connecting = false;
		boolean hasBle = false;
		int pendingChecks = 0;
		ArrayList<ConnectionAttrs> connDevices = new ArrayList<>();
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			deviceId = this.getEncryptedRequestValue(request, "auhsahnn", key, iv);
			boolean proximityOnly = this.getEncryptedRequestBoolean(request, "natheush", key, iv, true);

			boolean computerAwake = this.getEncryptedRequestBoolean(request, Parameters.COMPUTER_AWAKE, key, iv, true);
			DeviceDbObj senderDevice = dataAccess.getDeviceByDeviceId(deviceId, "processConnectionCheckPost");
			if (senderDevice != null) {
				String devServices = "";
				hasBle = senderDevice.getHasBle();
				if (senderDevice.isActive()) {
					if (senderDevice.getTurnOffFromInstaller()) {
						reason = Constants.TURN_OFF_FROM_INSTALLER;
						dataAccess.setTurnOffFromInstallerAndLastSilentPushResponse(senderDevice, false,
								DateTimeUtilities.getCurrentTimestamp());
					} else {
						if (senderDevice.getTerminate()) {
							reason = Constants.TERMINATE;
							senderDevice.setTerminate(false);
							dataAccess.updateDevice(senderDevice, "we just terminated");
						} else {
							showIcon = senderDevice.getShowIcon();
							dataAccess.addLog(deviceId, "showIcon: " + showIcon);
							ArrayList<DeviceConnectionDbObj> connections = dataAccess
									.getConnectionsForDevice(senderDevice, true);
							instanceId = GeneralUtilities.randomString(20);
							if (connections.size() > 0) {
								instanceId = GeneralUtilities.randomString(20);
								for (DeviceConnectionDbObj connection : connections) {
									dataAccess.addLog(deviceId, connections.size()
											+ " connected devices found, checking prox only? " + proximityOnly);
									boolean clientConnected;
									if (proximityOnly) {
										clientConnected = dataAccess.isProximate(connection, true);
										if (!clientConnected) {
											DeviceDbObj peripheralDevice;
											// TODO: I changed this because it didn't seem correct -- cjm
											if (!senderDevice.isCentral()) {
												peripheralDevice = senderDevice;
											} else {
												peripheralDevice = dataAccess.getDeviceByDeviceId(
														connection.getPeripheralDeviceId(),
														"processConnectionCheckPost");
											}
											clientConnected = dataAccess.connectionIsWithinTimePeriod(connection,
													peripheralDevice.isPeripheralMobile());
										}
									} else {
										dataAccess.addLog(deviceId,
												"calling isAccessAllowed from processConnectionCheckPost",
												LogConstants.TRACE);
										clientConnected = dataAccess.isAccessAllowed(connection, senderDevice,
												!computerAwake);
									}
									if (!clientConnected) {
										connectedToAll = false;
									} else {
										connected = true;
									}
									if (connection.isSubscribed()) {
										subscribed = true;
									}

									String[] centralInstanceIdPair;
									String[] peripheralInstanceIdPair;
									Encryption encryption = new Encryption();
									DeviceDbObj receivingDevice;
									String firstLetter = GeneralUtilities.randomLetters(1);
									boolean pushFailure = true;
									if (senderDevice.isCentral()) {
										centralInstanceIdPair = encryption.createEncryptedInstanceId(senderDevice,
												firstLetter);
										receivingDevice = dataAccess
												.getDeviceByDeviceId(connection.getPeripheralDeviceId());
										peripheralInstanceIdPair = encryption.createEncryptedInstanceId(receivingDevice,
												firstLetter);
										pushFailure = senderDevice.isPushFailure();
									} else {
										peripheralInstanceIdPair = encryption.createEncryptedInstanceId(senderDevice,
												firstLetter);
										receivingDevice = senderDevice;
										centralInstanceIdPair = encryption.createEncryptedInstanceId(receivingDevice,
												firstLetter);
										dataAccess.updateLastVariableRetrieval(senderDevice, computerAwake);
										DeviceDbObj centralDevice = dataAccess.getDeviceByDeviceId(
												connection.getCentralDeviceId(), "processConnectionCheckPost");
										if (centralDevice != null) {
											if (!didJustSignUp(centralDevice)) {
												pushFailure = centralDevice.isPushFailure();
											}
										} else {
											dataAccess.addLog("trying to get data from an old device",
													LogConstants.WARNING);
										}
									}
									boolean hasBlePair = senderDevice.getHasBle() && receivingDevice.getHasBle();
									if (peripheralInstanceIdPair != null && centralInstanceIdPair != null) {
										connDevices.add(new ConnectionAttrs(connection.getServiceUuid(),
												connection.getCharacteristicUuid(), "dev" + i, clientConnected,
												centralInstanceIdPair[1], peripheralInstanceIdPair[1], instanceId,
												DateTimeUtilities.getCurrentTimestamp(), hasBlePair,
												connection.getPeripheralConnected(), connection.getCentralConnected(),
												connection.isSubscribed(), connection.getLastSuccess(),
												receivingDevice.getCommand(),
												receivingDevice.getDeviceClass().deviceClassName(),
												connection.getPeripheralIdentifier(), pushFailure));
										i++;
										outcome = Outcomes.SUCCESS;
										if (senderDevice.getTemp().equals("requiresReset")) {
											reason = "requiresReset";
											senderDevice.setTemp("");
											dataAccess.updateDevice(senderDevice, "processConnectionCheckPost");
										}
									} else {
										dataAccess.addLog(deviceId,
												"encryption pair wasn't created. " + "Key not found?",
												LogConstants.WARNING);
										reason = Constants.KEY_NOT_FOUND;
									}
									if (!connecting && dataAccess.getConnecting(connection)) {
										connecting = true;
									}
								}
								if (!senderDevice.isCentral() && !connected) {
									if (connections.get(0) != null) {
										Timestamp connectingCentralDt = connections.get(0).getConnectingCentral();
										Timestamp lastSuccess = connections.get(0).getLastSuccess();
										if (connectingCentralDt != null && lastSuccess != null) {
											if (connectingCentralDt.compareTo(lastSuccess) > 0) {
												reason = Constants.SERVER_CONNECTING;
											}
										}
									}
								}
								ArrayList<DeviceAndInstanceId> pendingRequestDevices = dataAccess
										.getPendingRequests(senderDevice);
								pendingChecks = pendingRequestDevices.size();
							} else {
								dataAccess.addLog(deviceId, "no connected devices");
								reason = "no connected devices";
							}
						}
					}
				} else {
					reason = Constants.RESET;
				}
				varJson.put("devices", devServices);
			} else {
				DeviceDbObj expiredDev = dataAccess.getExpiredDevice(deviceId);
				if (expiredDev == null) {
					reason = Constants.DEVICE_NOT_FOUND;
				} else {
					reason = Constants.RESET;
				}
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getMessage();
		}
		varJson.put("deviceCount", i);
		model = this.addReturnVars(model, outcome, reason, connDevices, instanceId, connected, subscribed,
				connectedToAll, showIcon, false, hasBle, connecting, pendingChecks);
		return "varResult";
	}

	public boolean didJustSignUp(DeviceDbObj central) {
		return DateTimeUtilities.timestampSecondAgo(central.getLastReset()) < 180;
	}

}
