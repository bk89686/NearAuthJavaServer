package com.humansarehuman.blue2factor.communication;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.Timer;

import org.apache.http.util.TextUtils;
import org.json.JSONException;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponse;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.ServerDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;
import com.humansarehuman.blue2factor.utilities.JsonUtilities;

public class PushNotifications {
	@SuppressWarnings("ucd")
	final static int iOsPushLimitationSeconds = 481;
	final static int generalPushNotificationLimits = 20;
	final static int loudPushNotificationLimits = 60;

	public void resendLoudPush(DeviceDbObj central) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		CheckDbObj check = dataAccess.getLastPushCheckForCentral(central.getDeviceId());
		if (check != null) {
			if (!TextUtils.isBlank(check.getPeripheralDeviceId())) {
				DeviceDbObj peripheral = dataAccess.getDeviceByDeviceId(check.getPeripheralDeviceId());
				if (peripheral != null) {
					sendLoudPush(peripheral, central, null, false);
				}
			} else {
				dataAccess.addLog("peripheralId was blank");
			}
		} else {
			dataAccess.addLog("check was null");
		}
	}

	public ApiResponseWithToken sendLoudPushByDevice(DeviceDbObj callingDevice, boolean ssh) {
		return this.sendLoudPushByDevice(callingDevice, null, ssh);
	}

	public ApiResponseWithToken sendLoudPushByDevice(DeviceDbObj callingDevice, ServerDbObj server, boolean ssh) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		DeviceDbObj centralDevice;
		if (!callingDevice.isCentral()) {
			centralDevice = dataAccess.getCentralForPeripheral(callingDevice);
		} else {
			centralDevice = callingDevice;
		}
		if (centralDevice != null) {
			BasicResponse resp = this.sendLoudPush(callingDevice, centralDevice, server, ssh);
			outcome = resp.getOutcome();
			reason = resp.getReason();
		} else {
			reason = Constants.DEV_NOT_FOUND;
		}
		dataAccess.addLog("outcome for loud push " + outcome);
		return new ApiResponseWithToken(outcome, reason, "");
	}

	public ApiResponseWithToken sendLoudPushAboutPairing(DeviceDbObj callingDevice, boolean ssh) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
//		DeviceDbObj centralDevice;
//		if (!callingDevice.isCentral()) {
//			centralDevice = dataAccess.getCentralForPeripheral(callingDevice);
//		} else {
//			centralDevice = callingDevice;
//		}
//		if (centralDevice != null) {
//			BasicResponse resp = this.sendLoudPush(callingDevice, centralDevice, server, ssh);
//			outcome = resp.getOutcome();
//			reason = resp.getReason();
//		} else {
//			reason = Constants.DEV_NOT_FOUND;
//		}
		dataAccess.addLog("outcome for loud push " + outcome);
		return new ApiResponseWithToken(outcome, reason, "");
	}

	public ApiResponse sendLoudPushByDeviceOnce(DeviceDbObj callingDevice, boolean ssh) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		DeviceDbObj centralDevice;
		if (!callingDevice.isCentral()) {
			centralDevice = dataAccess.getCentralForPeripheral(callingDevice);
			dataAccess.addLog("called from peripheral");
		} else {
			centralDevice = callingDevice;
			dataAccess.addLog("called from central");
		}
		if (centralDevice != null) {
			Timestamp lastPush = centralDevice.getLastPush();
			dataAccess.addLog("lastLoudPush: " + lastPush);
			BasicResponse resp = this.sendLoudPush(callingDevice, centralDevice, ssh);
			outcome = resp.getOutcome();
			reason = resp.getReason();
		} else {
			reason = Constants.DEV_NOT_FOUND;
		}
		dataAccess.addLog("outcome for loud push " + outcome + ", reason: " + reason);
		return new ApiResponse(outcome, reason);
	}

	public BasicResponse sendPushByDeviceId(String deviceId, boolean disconnect, String source, boolean pushIfConnected,
			boolean pushRegardless) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
		return sendPushToAllConnectedDevices(device, source, disconnect, true, pushIfConnected, pushRegardless);
	}

	public BasicResponse sendPushesFromDevice(DeviceDbObj fromDevice) {
		return sendPushToAllConnectedDevices(fromDevice, "sendPushesFromDevice", false, false, false, true);
	}

	public BasicResponse sendPushToAllConnectedDevices(DeviceDbObj fromDevice, String source, boolean disconnect,
			boolean repeat, boolean pushIfConnected, boolean pushRegardless) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		int outcome = Outcomes.FAILURE;
		String reason = "";

		DeviceDbObj currDevice;
		PushNotifications push = new PushNotifications();
		BasicResponse lastResponse;
		ArrayList<DeviceConnectionDbObj> connections = dataAccess.getAllUsersConnections(fromDevice, true);
		for (DeviceConnectionDbObj connection : connections) {
			if (fromDevice.getDeviceId().equals(connection.getPeripheralDeviceId())) {
				currDevice = dataAccess.getDeviceByDeviceId(connection.getCentralDeviceId());
			} else {
				currDevice = dataAccess.getDeviceByDeviceId(connection.getPeripheralDeviceId());
			}
			if (currDevice != null) {
				lastResponse = push.sendPushToDevice(currDevice, connections, connection,
						"sendPushToAllConnectedDevices", disconnect, repeat, pushIfConnected, pushRegardless);
				if (lastResponse.getOutcome() == Outcomes.SUCCESS) {
					outcome = Outcomes.SUCCESS;
				}

				reason += lastResponse.getReason() + " -- ";
			}
		}

		BasicResponse response = new BasicResponse(outcome, reason);
		return response;
	}

	public BasicResponse sendResetSilentPush(DeviceDbObj device) {
		JsonUtilities jsonUtil = new JsonUtilities();
		Message resetMessage = jsonUtil.getResetMessage(device);
		return this.sendFcmMessage(resetMessage, device, false, true, false);
	}

	public BasicResponse sendLoudPush(DeviceDbObj callingDevice, DeviceDbObj centralDevice, boolean ssh) {
		return this.sendLoudPush(callingDevice, centralDevice, null, ssh);
	}

	public BasicResponse sendLoudPush(DeviceDbObj callingDevice, DeviceDbObj centralDevice, ServerDbObj server,
			boolean ssh) {
		JsonUtilities jsonUtil = new JsonUtilities();
		Message message;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.addLog(callingDevice.getDeviceId(), "trying to send");
		if (centralDevice.getOperatingSystem().equals(OsClass.ANDROID)) {
			message = jsonUtil.confirmSigninMessageAndroid(callingDevice, centralDevice, server, ssh);
		} else {
			message = jsonUtil.confirmSigningMessageIos(callingDevice, centralDevice, server, ssh);
		}
		return this.sendFcmMessage(message, centralDevice, false, true, false, callingDevice.getDeviceType());

	}

	public BasicResponse sendOpenAppPush(DeviceDbObj callingDevice) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "");
		JsonUtilities jsonUtil = new JsonUtilities();
		Message message;
		if (callingDevice != null) {
			if (callingDevice.getOperatingSystem().equals(OsClass.ANDROID)) {
				message = jsonUtil.openAppDataAndroid(callingDevice);
			} else {
				message = jsonUtil.openAppDataIos(callingDevice);
			}
			response = this.sendFcmMessage(message, callingDevice, false, true, false);
		} else {
			response.setReason("calling device was null");
		}
		return response;
	}

	public BasicResponse sendConnectingPush(DeviceDbObj callingDevice, DeviceDbObj centralDevice) {
		JsonUtilities jsonUtil = new JsonUtilities();
		Message message;
		BasicResponse resp = new BasicResponse(Outcomes.FAILURE, "iosOnly");
		if (centralDevice.getOperatingSystem().toString().equals(OsClass.IOS.toString())) {
			message = jsonUtil.connectingAppMessageDataIos(callingDevice, centralDevice);
			resp = sendFcmMessage(message, centralDevice, false, true, false, callingDevice.getDeviceType());
		}

		return resp;
	}

	public void sendOneSilentPushAfterDelay(final DeviceDbObj device, final Boolean disconnect, int seconds,
			final String source) {
		Timer timer = new Timer(seconds * 1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DeviceDataAccess dataAccess = new DeviceDataAccess();
				if (!dataAccess.isRecentSuccessOrConnecting(device)) {
					PushNotifications push = new PushNotifications();
					boolean pushRegardless = !dataAccess.isProximate(device, true);
					push.sendPushToAllConnectedDevices(device, "secondPush", disconnect, false, false, pushRegardless);
				}
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

	public BasicResponse sendPushToDevice(DeviceDbObj device) {
		return sendPushToDevice(device, "sendPushToDevice", false, false, false, true);
	}

	public BasicResponse sendPushToDevice(DeviceDbObj toDevice, String source, boolean disconnect, boolean repeat,
			boolean pushIfConnected, boolean pushRegardless) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		if (!toDevice.isTurnedOff()) {
			Message message;
			if (toDevice.getFcmId() != null) {
				JsonUtilities jsonUtil = new JsonUtilities();
				String instanceId = GeneralUtilities.randomString(20);
				if (toDevice.isCentral()) {
					message = jsonUtil.getNewSilentMessageDataForCentral(toDevice, disconnect, instanceId,
							pushIfConnected);
				} else {
					message = jsonUtil.getNewSilentMessageDataForPeripheral(toDevice, disconnect, instanceId);
				}
				if (message != null) {
					BasicResponse resp = sendFcmMessage(message, toDevice, repeat, false, pushRegardless);
					outcome = resp.getOutcome();
					if (outcome == Outcomes.SUCCESS) {
						checkForResponse(toDevice);
					}
					reason = resp.getReason();
				} else {
					dataAccess.addLog(toDevice.getDeviceId(),
							"json was null, probably because we are already connected", LogConstants.WARNING);
				}
			} else {
				reason = "NOT SENT: fcm was null for deviceId: " + toDevice.getDeviceId();
			}
		} else {
			reason = "not sending push because we are turned off";
		}
		if (reason != "") {
			dataAccess.addLog(toDevice.getDeviceId(), reason);
		}
		return new BasicResponse(outcome, reason);
	}

	public BasicResponse sendPushToDevice(DeviceDbObj toDevice, ArrayList<DeviceConnectionDbObj> connections,
			DeviceConnectionDbObj currConnection, String source, boolean disconnect, boolean repeat,
			boolean pushIfConnected, boolean pushRegardless) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		if (!toDevice.isTurnedOff()) {
			Message message;
			if (toDevice.getFcmId() != null) {
				JsonUtilities jsonUtil = new JsonUtilities();
				String instanceId = GeneralUtilities.randomString(20);
				if (toDevice.isCentral()) {
					message = jsonUtil.getNewSilentMessageDataForCentral(toDevice, connections, disconnect, instanceId,
							pushIfConnected);
				} else {
					message = jsonUtil.getNewSilentMessageDataForPeripheral(toDevice, currConnection, disconnect,
							instanceId);
				}
				if (message != null) {
					BasicResponse resp = sendFcmMessage(message, toDevice, repeat, false, pushRegardless);
					outcome = resp.getOutcome();
					if (outcome == Outcomes.SUCCESS) {
						checkForResponse(toDevice);
					}
					reason = resp.getReason();
				} else {
					reason = "json was null, probably because we are already connected";
				}
			}
		} else {
			reason = "not sending push because we are turned off";
		}
		if (reason != "") {
			dataAccess.addLog(toDevice.getDeviceId(), reason);
		}
		return new BasicResponse(outcome, reason);
	}

	private void checkForResponse(DeviceDbObj device) {
		Timer timer = new Timer(10000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean responseReceived = false;
				DeviceDataAccess dataAccess = new DeviceDataAccess();
				if (device != null) {
					Timestamp lastResponse = device.getLastSilentPushResponse();
					if (lastResponse != null) {
						double secondsAgo = DateTimeUtilities.timeDifferenceInSecondsFromNow(lastResponse);
						if (secondsAgo < 15) {
							responseReceived = true;
						}
					}
				}
				if (!responseReceived) {
					dataAccess.addLog(device.getDeviceId(), "We failed to receive a response from a silent push.",
							LogConstants.IMPORTANT);
					dataAccess.setPushFailed(device, true);

				}
			}
		});
		timer.setRepeats(false); // Only execute once
		timer.start();
	}

	public BasicResponse sendOneNewSilentPushFromPeripheral(DeviceDbObj peripheralDevice, String source,
			Boolean disconnect, Boolean repeat, boolean pushIfConnected, boolean pushRegardless) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			DeviceConnectionDbObj connection = dataAccess.getConnectionForPeripheral(peripheralDevice, true);
			DeviceDbObj centralDevice = dataAccess.getDeviceByDeviceId(connection.getCentralDeviceId());
			dataAccess.addLog(peripheralDevice.getDeviceId(), "call to send silent push from: " + source);
			JsonUtilities jsonUtil = new JsonUtilities();
			String instanceId = GeneralUtilities.randomString(20);
			Message message = jsonUtil.getNewSilentMessageDataForCentral(centralDevice, disconnect, instanceId,
					pushIfConnected);
			if (message != null) {
				BasicResponse resp = sendFcmMessage(message, centralDevice, repeat, false, pushRegardless,
						peripheralDevice.getDeviceType());
				outcome = resp.getOutcome();
				reason = resp.getReason();
			} else {
				reason = "json was null, Keys probably aren't set";
			}
		} catch (Exception e) {
			reason = e.getLocalizedMessage();
		}
		if (TextUtils.isEmpty(reason)) {
			dataAccess.addLog(peripheralDevice.getDeviceId(), reason);
		}
		dataAccess.addLog("Outcome from sendOneNewSilentPushFromPeripheral = " + outcome);
		return new BasicResponse(outcome, reason);
	}

	public BasicResponse sendTestFcm(DeviceDbObj device) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		Message message = Message.builder()
				.setNotification(Notification.builder().setTitle("Sample Notification Title")
						.setBody("This is a sample FCM notification body.").build())
				.setToken(device.getFcmId()).build();
		try {
			String messageId = new Internet().sendFcmMessage(message, 1);
			outcome = Outcomes.SUCCESS;
			reason = "push sent to " + device.getDeviceType() + " -- returned " + messageId;
		} catch (JSONException e) {
			reason = e.getLocalizedMessage();
			e.printStackTrace();
		} catch (IOException e) {
			reason = e.getLocalizedMessage();
			e.printStackTrace();
		} catch (InterruptedException e) {
			reason = e.getLocalizedMessage();
			e.printStackTrace();
		}

		return new BasicResponse(outcome, reason);
	}

	public BasicResponse sendFcmMessage(Message message, DeviceDbObj device, boolean repeat, boolean loud,
			boolean pushRegardless) {
		return this.sendFcmMessage(message, device, repeat, loud, pushRegardless, "??");
	}

	public BasicResponse sendFcmMessage(Message message, DeviceDbObj device, boolean repeat, boolean loud,
			boolean pushRegardless, String from) {
		// It'd be nicer to do this further up in the call stack so it doesn't have to
		// do the
		// work to get here only to be rejected, but this was the first common function
		// that where it seemed plausible. It also limits the time between the check,
		// and setting the timestamp. -- cjm
		int outcome = Outcomes.FAILURE;
		String reason = "push sent to ";
		Internet internet = new Internet();
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			if (shouldPush(device, loud)) {
				String messageId = internet.sendFcmMessage(message, 1);
				if (messageId != null) {
					if (loud) {
						dataAccess.recordLoudPush(device);
						dataAccess.addLog("new loud push sent to " + device.getDeviceType() + "- ID: " + messageId,
								LogConstants.IMPORTANT);
					} else {
						dataAccess.recordSilentPush(device);
						dataAccess.addLog("new silent push sent to " + device.getDeviceType() + "- ID: " + messageId,
								LogConstants.IMPORTANT);
					}
					reason += device.getDeviceType() + ": success";
					outcome = Outcomes.SUCCESS;
				} else {
					reason += device.getDeviceType() + ": failed (sendFcmJson)";
					dataAccess.addLog(device.getDeviceId(), reason, LogConstants.ERROR);
					dataAccess.setPushFailure(device, true);
				}
			} else {
				reason = "already pushed within the last " + Constants.SECONDS_TO_NOT_PUSH + " seconds";
				dataAccess.addLog(reason, LogConstants.TRACE);
			}
		} catch (Exception e) {
			reason = "failure: " + e.getMessage();
			dataAccess.addLog(device.getDeviceId(), e);
		}
		return new BasicResponse(outcome, reason);
	}

	private boolean shouldPush(DeviceDbObj device, boolean loud) {
		boolean shouldPush = false;
		long lastPushSecondsAgo;
		DataAccess dataAccess = new DataAccess();
		if (loud) {
			Timestamp lastPush = device.getLastPush();
			lastPushSecondsAgo = DateTimeUtilities.timestampSecondAgo(lastPush);
		} else {
			Timestamp lastSilentPush = device.getLastSilentPush();
			lastPushSecondsAgo = DateTimeUtilities.timestampSecondAgo(lastSilentPush);
			shouldPush = true;
		}
		if (lastPushSecondsAgo > Constants.SECONDS_TO_NOT_PUSH) {
			if (loud || !device.isPushFailure() || lastPushSecondsAgo > Constants.SECONDS_TO_NOT_PUSH_IF_UNRESPONSIVE) {
				new DataAccess().addLog("pushing, last one was " + lastPushSecondsAgo + " seconds ago",
						LogConstants.INFO);
				shouldPush = true;
			} else {
				if (!loud) {
					dataAccess.addLog("not pushing because it's too soon for a push to an unresponsive device: "
							+ lastPushSecondsAgo + " seconds", LogConstants.IMPORTANT);
				}
			}
		} else {
			dataAccess.addLog("not pushing because it's too soon: " + lastPushSecondsAgo + " seconds",
					LogConstants.IMPORTANT);
		}
		return shouldPush;
	}
}
