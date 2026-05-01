package com.humansarehuman.blue2factor.utilities;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
//import com.google.cloud.storage.Notification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceConnectionDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.enums.CheckType;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.ServerDbObj;

public class JsonUtilities {
	private String COMPANY_SECRET = "i2sKSHfVb32Y5ejnyN";

	public Message getNewSilentMessageDataForPeripheral(DeviceDbObj peripheralDevice, boolean disconnect,
			String instanceId) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		DeviceConnectionDbObj connection = dataAccess.getConnectionForPeripheral(peripheralDevice, true);
		return this.getNewSilentMessageDataForPeripheral(peripheralDevice, connection, disconnect, instanceId);

	}

	public Message getNewSilentMessageDataForPeripheral(DeviceDbObj peripheralDevice, DeviceConnectionDbObj connection,
			boolean disconnect, String instanceId) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		Message message = null;
		try {
			if (!TextUtils.isBlank(peripheralDevice.getFcmId())) {
				if (connection != null && !TextUtils.isBlank(connection.getServiceUuid())) {
					if (!dataAccess.isAccessAllowed(connection, peripheralDevice)) {

						JSONObject devices = new JSONObject();
						JSONObject device = new JSONObject();
						Notification.Builder notificationBuilder = Notification.builder();
						notificationBuilder.setTitle(Constants.APP_NAME);
						if (connection.isActive()) {
							if (Encryption.getDevicePublicKey(peripheralDevice.getDeviceId()) != null) {
								devices.put("device0", device);
								device.put("disconnect", disconnect);
								device.put("connected", false);
								Timestamp lastConnectionSuccess;
								if (connection.getLastCentralConnectionSuccess()
										.after(connection.getLastPeripheralConnectionSuccess())) {
									lastConnectionSuccess = connection.getLastCentralConnectionSuccess();
								} else {
									lastConnectionSuccess = connection.getLastPeripheralConnectionSuccess();
								}
								device.put("lastSuccess", lastConnectionSuccess);
								message = buildFcm(device, 1, instanceId, peripheralDevice);
							} else {
								dataAccess.addLog("key was null, probably", LogConstants.WARNING);
							}
						}
					}
				} else {
					dataAccess.addLog("connection was null or service uuid was empty", LogConstants.WARNING);
				}
			} else {
				dataAccess.addLog("fcm was blank");
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return message;
	}

	public Message getResetMessage(DeviceDbObj toDevice) {
		Message.Builder messageBuilder = Message.builder();
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		if (!TextUtils.isBlank(toDevice.getFcmId())) {
			String instanceId = GeneralUtilities.randomString(20);
			messageBuilder.putData("devices", "");
			messageBuilder.putData("deviceCount", Integer.toString(0));
			messageBuilder.putData("instanceId", instanceId);
			messageBuilder.putData("showLoud", Boolean.toString(false));
			messageBuilder.setToken(toDevice.getFcmId());
			ApnsConfig.Builder apnsConfigBuilder = ApnsConfig.builder();
			Aps.Builder apsBuilder = Aps.builder();
			apsBuilder.setContentAvailable(true);
			apnsConfigBuilder.setAps(apsBuilder.build());
			if (toDevice.getOperatingSystem().toString().equals(OsClass.ANDROID.toString())) {
				apnsConfigBuilder.putHeader("priority", "high");
			} else {
				apnsConfigBuilder.putHeader("apns-priority", "5");
			}
			messageBuilder.setApnsConfig(apnsConfigBuilder.build());
			dataAccess.addLog(toDevice.getDeviceId(), messageBuilder.toString());
		} else {
			dataAccess.addLog(toDevice.getDeviceId(), "fcmId was blank");
		}
		return messageBuilder.build();
	}

	public JSONObject getExpireJson(String newToken) {
		JSONObject expireJson = new JSONObject();
		expireJson.put("coKey", this.COMPANY_SECRET);
		expireJson.put("b2fToken", newToken);
		expireJson.put("b2fSession", "");
		expireJson.put("cmd", "confirm");
		return expireJson;
	}

	public JSONObject getProxJson(String token, String session) {
		JSONObject proxJson = new JSONObject();
		proxJson.put("coKey", this.COMPANY_SECRET);
		proxJson.put("b2fToken", token);
		proxJson.put("b2fSession", session);
		proxJson.put("cmd", "proxcheck");
		return proxJson;
	}

	public Message getNewSilentMessageDataForCentral(DeviceDbObj centralDevice, boolean disconnect, String instanceId,
			boolean pushIfConnected) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		ArrayList<DeviceConnectionDbObj> connections = dataAccess.getConnectionsForCentral(centralDevice);
		return this.getNewSilentMessageDataForCentral(centralDevice, connections, disconnect, instanceId,
				pushIfConnected);
	}

	public Message getNewSilentMessageDataForCentral(DeviceDbObj centralDevice,
			ArrayList<DeviceConnectionDbObj> connections, boolean disconnect, String instanceId,
			boolean pushIfConnected) {
		Message message = null;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		if (!TextUtils.isBlank(centralDevice.getFcmId())) {
			JSONObject devices = new JSONObject();
			JSONObject device;
			DeviceDbObj connectedDevice;
			int deviceCount = 0;
			boolean dontSend;
			for (DeviceConnectionDbObj connection : connections) {
				try {
					dontSend = false;
					device = new JSONObject();
					// don't find devices we don't need to
					if (!dontSend) {
						boolean proximate = dataAccess.isProximateAccessAllowed(connection, true);
						if (pushIfConnected || !proximate) {
							if (connection.isActive()) {
								if (!TextUtils.isEmpty(connection.getServiceUuid())) {
									connectedDevice = dataAccess
											.getDeviceByDeviceId(connection.getPeripheralDeviceId());
									if (connectedDevice != null
											&& Encryption.getDevicePublicKey(connectedDevice.getDeviceId()) != null) {
										device = this.buildDeviceJson(device, centralDevice, connection,
												connectedDevice, disconnect, proximate, instanceId);
										devices.put("device" + deviceCount, device);
										deviceCount++;
									} else {
										dataAccess.addLog("key was null, probably", LogConstants.WARNING);
									}
								} else {
									dataAccess.addLog("not sending push info about connection without service uuid",
											LogConstants.WARNING);
								}
							}
						}
					}
				} catch (Exception e) {
					dataAccess.addLog(e);
				}
			}

			message = buildFcm(devices, deviceCount, instanceId, centralDevice);
		} else {
			dataAccess.addLog(centralDevice.getDeviceId(), "fcmId was blank");
		}

		dataAccess.addLog(centralDevice.getDeviceId(), message.toString(), LogConstants.TRACE);
		return message;
	}

	public JSONObject buildDeviceJson(JSONObject device, DeviceDbObj centralDevice, DeviceConnectionDbObj connection,
			DeviceDbObj connectedDevice, boolean disconnect, boolean proximate, String instanceId) {
		Encryption encryption = new Encryption();
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		device.put("service", connection.getServiceUuid());
		device.put("chr", connection.getCharacteristicUuid());
		device.put("disconnect", disconnect);
		String firstLetter = GeneralUtilities.randomLetters(1);
		String[] centralInstancePair = encryption.createEncryptedInstanceId(centralDevice, firstLetter);
		if (centralInstancePair != null && centralInstancePair.length == 2) {
			device.put("centralId", centralInstancePair[1]);
			device.put("hasBle", connectedDevice.getHasBle());
			device.put("connected", proximate);
			Timestamp lastConnectionSuccess;
			if (connection.getLastCentralConnectionSuccess().after(connection.getLastPeripheralConnectionSuccess())) {
				lastConnectionSuccess = connection.getLastCentralConnectionSuccess();
			} else {
				lastConnectionSuccess = connection.getLastPeripheralConnectionSuccess();
			}
			device.put("peripheral_identifier", connection.getPeripheralIdentifier());
			device.put("lastSuccess", lastConnectionSuccess);
			dataAccess.addLog(centralDevice.getDeviceId(), "central: " + centralInstancePair[0]);
			String[] peripheralInstancePair = encryption.createEncryptedInstanceId(connectedDevice, firstLetter);
			if (peripheralInstancePair != null && peripheralInstancePair.length > 1) {
				device.put("periphId", peripheralInstancePair[1]);
				Timestamp now = DateTimeUtilities.getCurrentTimestamp();
				device.put("idDate", timestampToPushString(now));
				CheckDbObj check = new CheckDbObj(GeneralUtilities.randomString(), instanceId,
						connection.getCentralDeviceId(), connection.getPeripheralDeviceId(),
						connection.getServiceUuid(), centralDevice.getUserId(), null, null, null, null, false, false,
						Outcomes.INCOMPLETE, now, null, false, CheckType.PROX, centralInstancePair[0],
						peripheralInstancePair[0]);
				dataAccess.addCheck(check);
			}
		}
		return device;
	}

	public Message buildFcm(JSONObject devices, int deviceCount, String instanceId, DeviceDbObj deviceToSend) {

		Message.Builder messageBuilder = Message.builder();
		messageBuilder.putData("devices", devices.toString());
		messageBuilder.putData("deviceCount", Integer.toString(deviceCount));
		messageBuilder.putData("instanceId", instanceId);
		messageBuilder.putData("showLoud", Boolean.toString(false));
		messageBuilder.setToken(deviceToSend.getFcmId());
		ApnsConfig.Builder apnsConfigBuilder = ApnsConfig.builder();
		Aps.Builder apsBuilder = Aps.builder();
		apsBuilder.setContentAvailable(true);
		apnsConfigBuilder.setAps(apsBuilder.build());
		if (deviceToSend.getOperatingSystem().toString().equals(OsClass.ANDROID.toString())) {
			apnsConfigBuilder.putHeader("priority", "high");
		} else {
			apnsConfigBuilder.putHeader("apns-priority", "5");
		}
		messageBuilder.setApnsConfig(apnsConfigBuilder.build());
		Message message = messageBuilder.build();
		printFcm(devices, deviceCount, instanceId, deviceToSend);
		return message;
	}

	public void printFcm(JSONObject devices, int deviceCount, String instanceId, DeviceDbObj deviceToSend) {
		JSONObject jsonMessage = new JSONObject();
		JSONObject jsonData = new JSONObject();
		jsonData.put("devices", devices);
		jsonData.put("deviceCount", Integer.toString(deviceCount));
		jsonData.put("instanceId", instanceId);
		jsonData.put("showLoud", Boolean.toString(false));
		jsonMessage.put("data", jsonData);
		jsonMessage.put("token", deviceToSend.getFcmId());
		JSONObject jsonApns = new JSONObject();
		jsonApns.put("contentAvailable", true);
		JSONObject jsonApnsConfig = new JSONObject();
		jsonApnsConfig.put("aps", jsonApns);
		jsonMessage.put("apnsConfig", jsonApnsConfig);
		new DataAccess().addLog(jsonMessage.toString(4), LogConstants.TRACE);
	}

	private String timestampToPushString(Timestamp timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String tsStr = sdf.format(timestamp);
		return tsStr;
	}

	public String getDeviceType(DeviceDbObj device) {
		OsClass os = device.getOperatingSystem();
		String deviceName = "";
		if (os.equals(OsClass.ANDROID)) {
			deviceName = "an Android device";
		} else if (os.equals(OsClass.IOS)) {
			deviceName = "an Apple device";
		} else if (os.equals(OsClass.WINDOWS)) {
			deviceName = "a Windows computer";
		} else if (os.equals(OsClass.OSX)) {
			deviceName = "a Mac";
		} else if (os.equals(OsClass.UNKNOWN)) {
			deviceName = "unknown";
		}
		return deviceName;
	}

	public Message confirmSigninMessageAndroid(DeviceDbObj callingDevice, DeviceDbObj centralDevice, boolean ssh) {
		return this.confirmSigninMessageAndroid(callingDevice, centralDevice, null, ssh);
	}

	public Message confirmSigninMessageAndroid(DeviceDbObj callingDevice, DeviceDbObj centralDevice, ServerDbObj server,
			boolean ssh) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		DeviceConnectionDbObj connection = dataAccess.getConnectionByDeviceIds(centralDevice.getDeviceId(),
				callingDevice.getDeviceId());
		Notification.Builder notificationBuilder = Notification.builder();
		Message.Builder messageBuilder = Message.builder();
		String collapseKey = "validate";
		String serverName = "a device";
		dataAccess.addLog(callingDevice.getDeviceId(), "start");
		String devType = getDeviceType(callingDevice);
		CheckType checkType = CheckType.PUSH;
		if (ssh) {
			checkType = CheckType.SSHPUSH;
			if (server != null && !TextUtils.isBlank(server.getServerName())) {
				serverName = server.getServerName();
			}
			notificationBuilder.setTitle("\"Did you just attempt to connect to " + serverName + " from "
					+ GeneralUtilities.getAorAn(devType) + " " + devType + "?\"");
		} else {
			if (devType.equals(DeviceClass.UNKNOWN.toString())) {
				notificationBuilder.setTitle("\"Did you recently attempt to temporarily signin?\"");
			} else {
				notificationBuilder.setTitle("\"Did you recently attempt to signin on "
						+ GeneralUtilities.getAorAn(devType) + " " + devType + "?\"");
			}
		}
		messageBuilder.setNotification(notificationBuilder.build());
		messageBuilder.putData("showLoud", Boolean.toString(true));
		messageBuilder.putData("devType", "\"" + devType + "\"");
		String uuid = "";
		if (connection != null) {
			uuid = connection.getServiceUuid();
			messageBuilder.putData("uuid", "\"" + connection.getServiceUuid() + "\"");
			dataAccess.addLog("service uuid was found");
		} else {
			dataAccess.addLog("connection not found. this should be a central device");
		}
		Encryption encryption = new Encryption();
		String firstLetter = Integer.toString(GeneralUtilities.randInt(1, 9));
		String[] centralInstancePair = encryption.createEncryptedInstanceId(centralDevice, firstLetter);
		messageBuilder.putData("centralId", "\"" + centralInstancePair[1] + "\"");
		messageBuilder.putData("sendTime", Long.toString(new Date().getTime()));
		String centralDeviceId = centralDevice.getDeviceId();
		String callingDeviceId;
		// this keeps accepting a push from validating the central device it's sent to
		if (callingDevice.getDeviceId().equals(centralDevice.getDeviceId())) {
			callingDeviceId = null;
		} else {
			callingDeviceId = callingDevice.getDeviceId();
		}
		Timestamp expireTime;
		CompanyDbObj company = dataAccess.getCompanyByDevId(centralDeviceId);
		if (company != null) {
			expireTime = DateTimeUtilities.getCurrentTimestampPlusSeconds(company.getPushTimeoutSeconds());
		} else {
			expireTime = DateTimeUtilities.getCurrentTimestampPlusDays(1);
		}
		CheckDbObj check = new CheckDbObj(GeneralUtilities.randomString(), GeneralUtilities.randomString(20),
				centralDeviceId, callingDeviceId, uuid, callingDevice.getUserId(), null, null, null, null, false, false,
				Outcomes.INCOMPLETE, DateTimeUtilities.getCurrentTimestamp(), null, false, checkType,
				centralInstancePair[0], "", expireTime);
		dataAccess.addLog("creating record with centralInstanceId = " + centralInstancePair[0]);
		dataAccess.addCheck(check);

		JSONObject accButton = new JSONObject();
		accButton.put("title", "Yes");
		accButton.put("action", "accpt");
		JSONObject declButton = new JSONObject();
		declButton.put("title", "No");
		declButton.put("action", "decl");
		JSONArray actions = new JSONArray();
		actions.put(0, accButton);
		actions.put(1, declButton);
		messageBuilder.setToken(centralDevice.getFcmId());
		ApnsConfig.Builder apnsConfigBuilder = ApnsConfig.builder();
		Aps.Builder apsBuilder = Aps.builder();
		apsBuilder.setContentAvailable(true);
		apnsConfigBuilder.setAps(apsBuilder.build());
		apnsConfigBuilder.putHeader("priority", "high");
		messageBuilder.setApnsConfig(apnsConfigBuilder.build());

		messageBuilder.putData("collapseKey", collapseKey);
		messageBuilder.putData("actions", actions.toString());
		AndroidConfig.Builder androidConfigBuilder = AndroidConfig.builder().setTtl(60 * 1000).setNotification(
				AndroidNotification.builder().setIcon("stock_ticker_update").setColor("#3f51b5").build());
		messageBuilder.setAndroidConfig(androidConfigBuilder.build());

		dataAccess.addLog(callingDevice.getDeviceId(), "sending: " + messageBuilder.toString());
		dataAccess.addLog(callingDevice.getDeviceId(), "length: " + messageBuilder.toString().getBytes().length);
		return messageBuilder.build();
	}

	public Message confirmSigningMessageIos(DeviceDbObj callingDevice, DeviceDbObj centralDevice, boolean ssh) {
		return this.confirmSigningMessageIos(callingDevice, centralDevice, null, ssh);
	}

	public Message confirmSigningMessageIos(DeviceDbObj callingDevice, DeviceDbObj centralDevice, ServerDbObj server,
			boolean ssh) {
		Message.Builder messageBuilder = Message.builder();
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		if (!StringUtils.isEmpty(centralDevice.getFcmId())) {
			dataAccess.addLog(callingDevice.getDeviceId(), "sending loud push to iOs device");
			String serverName = "a device";
			String devType = getDeviceType(callingDevice);
			String title = "NearAuth.ai";
			String desc;
			CheckType checkType = CheckType.PUSH;
			if (ssh) {
				if (server != null && !TextUtils.isBlank(server.getServerName())) {
					serverName = server.getServerName();
				}
				checkType = CheckType.SSHPUSH;
				desc = "Did you just attempt to connect to " + serverName + " from " + devType + "?";
			} else {
				if (devType.equals("unknown")) {
					desc = "Did you recently attempt to temporarily signin?";
				} else {
					desc = "Did you recently attempt to signin on " + devType + "?";
				}
			}

			Notification.Builder notificationBuilder = Notification.builder();
			notificationBuilder.setTitle(title).setBody(desc);
			messageBuilder.setNotification(notificationBuilder.build());

			messageBuilder.putData("sendTime", Long.toString(new Date().getTime()));
			messageBuilder.putData("description", desc);
			String connectedDeviceUuid = "";
			Encryption encryption = new Encryption();
			String firstLetter = Integer.toString(GeneralUtilities.randInt(1, 9));
			String[] centralInstancePair = encryption.createEncryptedInstanceId(centralDevice, firstLetter);
			messageBuilder.putData("centralId", "\"" + centralInstancePair[1] + "\"");
			messageBuilder.putData("peripheralId", "\"" + callingDevice.getDeviceId() + "push\"");
			DeviceConnectionDbObj connection = dataAccess.getConnectionByDeviceIds(centralDevice.getDeviceId(),
					callingDevice.getDeviceId());
			if (connection != null) {
				connectedDeviceUuid = connection.getServiceUuid();
				messageBuilder.putData("uuid", "\"" + connectedDeviceUuid + "\"");
			}
			Timestamp expireTime;
			CompanyDbObj company = dataAccess.getCompanyByDevId(centralDevice.getDeviceId());
			if (company != null) {
				expireTime = DateTimeUtilities.getCurrentTimestampPlusSeconds(company.getPushTimeoutSeconds());
			} else {
				expireTime = DateTimeUtilities.getCurrentTimestampPlusDays(1);
			}
			CheckDbObj check = new CheckDbObj(GeneralUtilities.randomString(), GeneralUtilities.randomString(20),
					centralDevice.getDeviceId(), callingDevice.getDeviceId(), connectedDeviceUuid,
					callingDevice.getUserId(), null, null, null, null, false, false, Outcomes.INCOMPLETE,
					DateTimeUtilities.getCurrentTimestamp(), null, false, checkType, centralInstancePair[0], "",
					expireTime);
			dataAccess.addCheck(check);

			messageBuilder.putData("MESSAGE_TYPE", "LOGIN");
			messageBuilder.setToken(centralDevice.getFcmId());
			ApnsConfig.Builder apnsConfigBuilder = ApnsConfig.builder();
			Aps.Builder apsBuilder = Aps.builder();
			apsBuilder.setContentAvailable(true);
			apsBuilder.setMutableContent(true);
			apnsConfigBuilder.setAps(apsBuilder.build());
			apnsConfigBuilder.putHeader("apns-expiration",
					Long.toString(DateTimeUtilities.secondsSinceEpochPlusSeconds(300)));
			apnsConfigBuilder.putHeader("apns-priority", "5");
			messageBuilder.setApnsConfig(apnsConfigBuilder.build());
			dataAccess.addLog(callingDevice.getDeviceId(), "sending: " + messageBuilder.toString());
		}
		return messageBuilder.build();
	}

	public Message openAppDataAndroid(DeviceDbObj callingDevice) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();

		Notification.Builder notificationBuilder = Notification.builder();
		Message.Builder messageBuilder = Message.builder();
		String collapseKey = "validate";
		dataAccess.addLog(callingDevice.getDeviceId(), "start");
		String devType = getDeviceType(callingDevice);
		notificationBuilder.setTitle("\"Please open NearAuth.ai to connect\"");
		messageBuilder.setNotification(notificationBuilder.build());
		messageBuilder.putData("showLoud", Boolean.toString(true));
		messageBuilder.putData("devType", "\"" + devType + "\"");
		messageBuilder.putData("sendTime", Long.toString(new Date().getTime()));

		messageBuilder.setToken(callingDevice.getFcmId());
		ApnsConfig.Builder apnsConfigBuilder = ApnsConfig.builder();
		Aps.Builder apsBuilder = Aps.builder();
		apsBuilder.setContentAvailable(true);
		apnsConfigBuilder.setAps(apsBuilder.build());
		apnsConfigBuilder.putHeader("priority", "high");
		messageBuilder.setApnsConfig(apnsConfigBuilder.build());

		messageBuilder.putData("collapseKey", collapseKey);
		AndroidConfig.Builder androidConfigBuilder = AndroidConfig.builder().setTtl(60 * 1000).setNotification(
				AndroidNotification.builder().setIcon("stock_ticker_update").setColor("#3f51b5").build());
		messageBuilder.setAndroidConfig(androidConfigBuilder.build());

		dataAccess.addLog(callingDevice.getDeviceId(), "sending: " + messageBuilder.toString());
		dataAccess.addLog(callingDevice.getDeviceId(), "length: " + messageBuilder.toString().getBytes().length);
		return messageBuilder.build();
	}

	public Message openAppDataIos(DeviceDbObj callingDevice) {
		Message.Builder messageBuilder = Message.builder();
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		if (!StringUtils.isEmpty(callingDevice.getFcmId())) {
			dataAccess.addLog(callingDevice.getDeviceId(), "sending loud push to iOs device");
			String title = "NearAuth.ai";
			String desc = "Please open NearAuth.ai to connect";

			Notification.Builder notificationBuilder = Notification.builder();
			notificationBuilder.setTitle(title).setBody(desc);
			messageBuilder.setNotification(notificationBuilder.build());

			messageBuilder.putData("sendTime", Long.toString(new Date().getTime()));
			messageBuilder.putData("description", desc);

			messageBuilder.putData("MESSAGE_TYPE", "LOGIN");
			messageBuilder.setToken(callingDevice.getFcmId());
			ApnsConfig.Builder apnsConfigBuilder = ApnsConfig.builder();
			Aps.Builder apsBuilder = Aps.builder();
			apsBuilder.setContentAvailable(true);
			apsBuilder.setMutableContent(true);
			apnsConfigBuilder.setAps(apsBuilder.build());
			apnsConfigBuilder.putHeader("apns-expiration",
					Long.toString(DateTimeUtilities.secondsSinceEpochPlusSeconds(300)));
			apnsConfigBuilder.putHeader("apns-priority", "5");
			messageBuilder.setApnsConfig(apnsConfigBuilder.build());
			dataAccess.addLog(callingDevice.getDeviceId(), "sending: " + messageBuilder.toString());
		}
		return messageBuilder.build();
	}

	public Message connectingAppMessageDataIos(DeviceDbObj callingDevice, DeviceDbObj centralDevice) {
		Message.Builder messageBuilder = Message.builder();
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		if (!StringUtils.isEmpty(centralDevice.getFcmId())) {
			dataAccess.addLog(callingDevice.getDeviceId(), "connection");
			String devType = getDeviceType(callingDevice);
			String title = "NearAuth.ai";
			String desc;
			if (devType.equals(DeviceClass.UNKNOWN.toString())) {
				desc = "Connecting ...";
			} else {
				desc = "We are connecting to " + devType;
			}
			Notification.Builder notificationBuilder = Notification.builder();
			notificationBuilder.setTitle(title);
			notificationBuilder.setBody(desc);
			messageBuilder.putData("sendTime", Long.toString(new Date().getTime()));
			messageBuilder.putData("description", desc);
			String connectedDeviceUuid = "";
			Encryption encryption = new Encryption();
			String firstLetter = Integer.toString(GeneralUtilities.randInt(1, 9));
			String[] centralInstancePair = encryption.createEncryptedInstanceId(centralDevice, firstLetter);
			messageBuilder.putData("centralId", "\"" + centralInstancePair[1] + "\"");
			DeviceConnectionDbObj connection = dataAccess.getConnectionByDeviceIds(centralDevice.getDeviceId(),
					callingDevice.getDeviceId());
			if (connection != null) {
				connectedDeviceUuid = connection.getServiceUuid();
				messageBuilder.putData("uuid", "\"" + connectedDeviceUuid + "\"");
			}
			Timestamp expireTime;
			CompanyDbObj company = dataAccess.getCompanyByDevId(centralDevice.getDeviceId());
			if (company != null) {
				expireTime = DateTimeUtilities.getCurrentTimestampPlusSeconds(company.getPushTimeoutSeconds());
			} else {
				expireTime = DateTimeUtilities.getCurrentTimestampPlusDays(1);
			}
			CheckDbObj check = new CheckDbObj(GeneralUtilities.randomString(), GeneralUtilities.randomString(20),
					centralDevice.getDeviceId(), callingDevice.getDeviceId(), connectedDeviceUuid,
					callingDevice.getUserId(), null, null, null, null, false, false, Outcomes.INCOMPLETE,
					DateTimeUtilities.getCurrentTimestamp(), null, false, CheckType.PUSH, centralInstancePair[0], "",
					expireTime);
			dataAccess.addCheck(check);

			messageBuilder.putData("MESSAGE_TYPE", "CONNECTING");
			messageBuilder.setToken(centralDevice.getFcmId());
			ApnsConfig.Builder apnsConfigBuilder = ApnsConfig.builder();
			Aps.Builder apsBuilder = Aps.builder();
			apsBuilder.setContentAvailable(true);
			apsBuilder.setMutableContent(true);
			apnsConfigBuilder.setAps(apsBuilder.build());
			apnsConfigBuilder.putHeader("apns-priority", "5");
			messageBuilder.setApnsConfig(apnsConfigBuilder.build());
			dataAccess.addLog(callingDevice.getDeviceId(), "sending: " + messageBuilder.toString());
		}
		return messageBuilder.build();
	}

	public JSONObject altJson(DeviceDbObj callingDevice, DeviceDbObj centralDevice) {
		JSONObject pushJson = new JSONObject();
		JSONObject message = new JSONObject();
		DeviceConnectionDataAccess dataAccess = new DeviceConnectionDataAccess();
		if (!StringUtils.isEmpty(centralDevice.getFcmId())) {
			JSONObject notification = new JSONObject();
			notification.put("title", "donkeycorn, donkeycorn");
			notification.put("body", "all about the donkeycorn");
			notification.put("sound", "default");
			notification.put("badge", 1);
			notification.put("MESSAGE_TYPE", "LOGIN");
			notification.put("click_action", "LOGIN");
			message.put("to", centralDevice.getFcmId());
			message.put("notification", notification);
			message.put("mutable_content", true);
			pushJson.put("message", message);
			dataAccess.addLog(callingDevice.getDeviceId(), "sending: " + message.toString(4));

		}
		return message;
	}
}
