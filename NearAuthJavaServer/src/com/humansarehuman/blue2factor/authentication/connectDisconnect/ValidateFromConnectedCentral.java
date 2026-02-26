package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceConnectionDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.SecondContact;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.VALIDATE_FROM_CONNECTED_CENTRAL)
@SuppressWarnings("ucd")
public class ValidateFromConnectedCentral extends BaseController {

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String validateConnectionProcessPost(HttpServletRequest request, ModelMap model) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String centralId = "";
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String instanceId = this.getEncryptedRequestValue(request, "jaopopaienk", key, iv);
			centralId = this.getEncryptedRequestValue(request, "iapniaorguj88", key, iv);
			dataAccess.addLog(centralId, "for centralId: " + centralId, LogConstants.INFO);
			String peripheralId = this.getEncryptedRequestValue(request, "poIIai7dn9", key, iv);
			// String bssId = this.getEncryptedRequestValue(request, "co0bnG", key, iv);
			String ssid = this.getEncryptedRequestValue(request, "PiiE45nn", key, iv);
			int gmtOffset = this.getEncryptedGmtOffset(request, "kldja8hg", key, iv);
			String os = this.getEncryptedRequestValue(request, "fdajoi8b", key, iv);
			String osVersion = this.getEncryptedRequestValue(request, "aoif9a0j1", key, iv);
			String screenSize = this.getEncryptedRequestValue(request, "ajf8ha", key, iv);
			String userLanguage = this.getEncryptedRequestValue(request, "pfaod8908", key, iv);
			int gmtOffsetReceiver = this.getEncryptedGmtOffset(request, "pafd92i23", key, iv);
			String receiverLanguage = this.getEncryptedRequestValue(request, "q0988uriop4q", key, iv);
			String receiverOs = this.getEncryptedRequestValue(request, "afi289", key, iv);
			String receiverOsVersion = this.getEncryptedRequestValue(request, "jialiei9", key, iv);
			String receiverScreenSize = this.getEncryptedRequestValue(request, "k9aww", key, iv);
			String ipAddress = GeneralUtilities.getClientIp(request);

			DeviceDbObj central = dataAccess.getDeviceByDeviceId(centralId);
			dataAccess.addLog(centralId, "centralFound - centralIp: " + ipAddress + ", centralSsid: " + ssid,
					LogConstants.INFO);
			DeviceDbObj peripheral = dataAccess.getDeviceByDeviceId(peripheralId);
			if (peripheral != null && central != null) {
				if (receiverOs != null) {
					central = updateCentralDevice(central, gmtOffsetReceiver, receiverOs, receiverScreenSize,
							receiverOsVersion, receiverLanguage);
					peripheral = updatePeripheralDevice(peripheral, gmtOffset, os, screenSize, osVersion, userLanguage);
					SecondContact secondContact = new SecondContact(central, peripheral, instanceId, ssid, ipAddress,
							false, gmtOffset);
					outcome = validateSecondStepWithDevices(secondContact);
					if (outcome == Outcomes.FAILURE) {
						reason = "no open inquires";
					}
					DeviceConnectionDbObj connection = updateConnectionAsSuccessfulForDevice(peripheral);
					dataAccess.addLog(centralId, "outcome: " + outcome + ", reason: " + reason);
					dataAccess.cancelAllRecentDisconnects(connection);
				}
			} else {
				if (peripheral == null) {
					dataAccess.addLog(peripheralId, "sender was null", LogConstants.ERROR);
				}
				if (central == null) {
					dataAccess.addLog(centralId, "receiver was null", LogConstants.ERROR);
				}
				reason = "sender and/or receiver was empty";
			}
		} catch (Exception e) {
			reason = e.getLocalizedMessage();
			dataAccess.addLog(centralId, e);
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		;
		this.addBasicResponse(model, response);
		return "result";
	}

	private DeviceConnectionDbObj updateConnectionAsSuccessfulForDevice(DeviceDbObj peripheral) {
		DeviceConnectionDataAccess dataAccess = new DeviceConnectionDataAccess();
		DeviceConnectionDbObj connection = dataAccess.getConnectionForPeripheral(peripheral, false);
		dataAccess.updateAsPeripheralConnected(connection);
//		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
//		connection.setPeripheralConnected(true);
//		connection.setLastSuccess(now, "updateConnection -> ValidateFromConnectedCentral");
//		connection.setLastPeripheralConnectionSuccess(now);
//		// CJM - TODO update peripheralSuccess
//		dataAccess.updateConnection(connection, "ValidateFromConnectedCentral");
		return connection;
	}

	private DeviceDbObj updateCentralDevice(DeviceDbObj central, int gmtOffsetReceiver, String receiverOs,
			String receiverScreenSize, String receiverOsVersion, String receiverLanguage) {
		if (gmtOffsetReceiver != -1) {
			central.setLastGmtOffset(gmtOffsetReceiver);
		}
		central.setOperatingSystem(OsClass.valueOf(receiverOs.toUpperCase()));
		central.setScreenSize(receiverScreenSize);
		central.setOsVersion(receiverOsVersion);
		central.setUserLanguage(receiverLanguage);
		central.setTriggerUpdate(false);
		central.setUnresponsive(false);
		central.setPushLoud(false);
		central.setPushFailure(false);
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.updateDevice(central, "validate from connected central");
		return central;
	}

	private DeviceDbObj updatePeripheralDevice(DeviceDbObj peripheral, int gmtOffset, String os, String screenSize,
			String osVersion, String userLanguage) {
		if (os != null) {
			if (screenSize != null) {
				peripheral.setScreenSize(screenSize);
			}
			if (gmtOffset != -1) {
				peripheral.setLastGmtOffset(gmtOffset);
			}
			peripheral.setUserLanguage(userLanguage);
			if (os.toUpperCase().equals("MACOS")) {
				os = "OSX";
			}
			peripheral.setOperatingSystem(OsClass.valueOf(os.toUpperCase()));
			peripheral.setOsVersion(osVersion);
		}
		peripheral.setTriggerUpdate(false);
		peripheral.setPushLoud(false);
		peripheral.setPushFailure(false);
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.updateDevice(peripheral, "validate from connected central");
		return peripheral;
	}

	public int validateSecondStepWithDevices(SecondContact secondContact) {
		int outcome = Outcomes.FAILURE;
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("validateSecondStep in Central: senderID: " + secondContact.getCentralDevice().getDeviceId());
		dataAccess.addLog(
				"validateSecondStep in Central: receiverID: " + secondContact.getPeripheralDevice().getDeviceId());
		if (dataAccess.isInCurrentCheckWithoutIpAddress(secondContact, true)) {
			// dataAccess.expireStaleChecks(secondContact.getReceiverDevice(),
			// secondContact.getSenderDevice(), secondContact.getInstanceId());
			dataAccess.addLog(secondContact.getCentralDevice().getDeviceId(), "in current response");
			outcome = Outcomes.SUCCESS;
		} else {
			dataAccess.addLog(secondContact.getCentralDevice().getDeviceId(), "no open inquires");
		}
		return outcome;
	}
}
