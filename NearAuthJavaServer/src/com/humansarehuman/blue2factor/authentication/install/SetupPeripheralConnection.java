package com.humansarehuman.blue2factor.authentication.install;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.communication.PushNotifications;
import com.humansarehuman.blue2factor.communication.twilio.TextMessage;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.SETUP_PERIPHERAL_CONNECTION)
@SuppressWarnings("ucd")
public class SetupPeripheralConnection extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String setupPeripheralProcessSetupPeripheralPost(@RequestBody String postPayload, HttpServletRequest request,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		BasicResponse response = new BasicResponse(outcome, reason, token);
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			dataAccess.addLog("postReq: " + postPayload, LogConstants.IMPORTANT);
			printAllRequestParams(request);
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String peripheralDeviceId = this.getEncryptedRequestValue(request, "JifnSf4", key, iv);
			boolean addDevice = this.getEncryptedRequestBoolean(request, "shnsh", key, iv, false);
			if (this.validateDeviceId(peripheralDeviceId)) {
				dataAccess.addLog("validated, addDevice: " + addDevice, LogConstants.IMPORTANT);
				if (addDevice) {
					response = addNewDevice(request, peripheralDeviceId, key, iv);
				} else {
					response = signupNewUser(request, peripheralDeviceId, key, iv);
				}
				AccessCodeDbObj accessCodeObj = dataAccess.addAccessCode(peripheralDeviceId, "SetupPeripheral");
				response.setToken(accessCodeObj.getAccessCode());
			} else {
				response.setReason("invalid deviceId");
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		model = this.addBasicResponse(model, response);
		return "result";
	}

	public BasicResponse addNewDevice(HttpServletRequest request, String peripheralDeviceId, String key, String iv) {
		int outcome = Outcomes.FAILURE;
		String accessCodeStr = "";
		String reason = "";
		SamlDataAccess dataAccess = new SamlDataAccess();
		try {
			String loginToken = this.getEncryptedRequestValue(request, "ajfli488s", key, iv);

			DeviceDbObj previousDevice = dataAccess.getDeviceByMachineId(loginToken);
			if (previousDevice != null) {
				if (dataAccess.deleteDevice(previousDevice)) {
					dataAccess.addLog(peripheralDeviceId, "deleted old device and connection for same machineId",
							LogConstants.WARNING);
				} else {
					dataAccess.addLog(peripheralDeviceId,
							"FAILED to delete old connection for same machineId.  This blows.", LogConstants.WARNING);
				}
			} else {
				dataAccess.addLog(peripheralDeviceId,
						"There was no previous device with that machine ID. This is normal", LogConstants.TRACE);
			}
			String service = this.getEncryptedRequestValue(request, "9UmIoh", key, iv).toUpperCase();
			String characteristic = this.getEncryptedRequestValue(request, "auneohunhsb", key, iv).toUpperCase();
			dataAccess.addLog("characteristic: " + characteristic, LogConstants.TRACE);
			String deviceType = this.getEncryptedRequestValue(request, "deviceType", key, iv);
			// String deviceType = "peripheral";
			OsClass os = OsClass.valueOf(this.getEncryptedRequestValue(request, "os", key, iv).toUpperCase());

			String osVersion = this.getEncryptedRequestValue(request, "osv", key, iv).replace(".", "_");
			Integer gmtOffset = this.getEncryptedGmtOffset(request, "ADFainss", key, iv);
			String language = this.getEncryptedRequestValue(request, "afjlibui", key, iv);
			String btAddress = this.getEncryptedRequestValue(request, "bta", key, iv);// null for
																						// OSX
			String screenSize = this.getEncryptedRequestValue(request, "po2jn3", key, iv);
			String groupId = this.getEncryptedRequestValue(request, "groupToken", key, iv);
			Boolean hasBle = this.getEncryptedRequestBoolean(request, "nthnsss", key, iv, true);
			Boolean fromTextCode = this.getEncryptedRequestBoolean(request, "sshCode", key, iv, false);
			boolean multiUser = this.getEncryptedRequestBoolean(request, Parameters.ADD_PERIPHERAL_MULTIUSER, key, iv,
					false);
			dataAccess.addLog("multiUser: " + multiUser);
			DeviceClass deviceClass;
			try {
				deviceClass = DeviceClass
						.valueOf(this.getEncryptedRequestValue(request, "class", key, iv).toUpperCase());
			} catch (Exception e) {
				deviceClass = DeviceClass.UNKNOWN;
			}
			int seed = GeneralUtilities.randInt(0, 1000000000);
			String userId = GeneralUtilities.randomString();
			dataAccess.addLog(peripheralDeviceId, "creating device with Id: " + peripheralDeviceId);
			String rand = "";// GeneralUtilities.randomString(4095);
			Double priority = this.getInitialDevicePriority(os);

			Timestamp date = DateTimeUtilities.getBaseTimestamp();
			DeviceDbObj central = dataAccess.getCentralByGroupId(groupId);
			if (central != null) {
				DeviceDbObj device = new DeviceDbObj(groupId, userId, peripheralDeviceId, seed, false, null, btAddress,
						new Date(), DateTimeUtilities.getCurrentTimestamp(), deviceType, os, loginToken, null,
						gmtOffset, osVersion, language, screenSize, rand, true, priority, false, 0, false, date, false,
						false, "", "", false, DateTimeUtilities.getCurrentTimestamp(), false, date, false, deviceClass,
						false, true, date, null, hasBle, false, null, multiUser, true, null, false);
				DeviceConnectionDbObj connection = new DeviceConnectionDbObj(DateTimeUtilities.getCurrentTimestamp(),
						GeneralUtilities.randomString(25), peripheralDeviceId, central.getDeviceId(), service,
						characteristic, groupId, false, date, date, false, date, false, date, date, date, false, date,
						false, null, null, null, null, null, false, "");
				if (dataAccess.addConnection(connection)) {
					dataAccess.addLog(peripheralDeviceId, "Connection Added", LogConstants.TRACE);
					accessCodeStr = GeneralUtilities.randomString(40);
					String coId = "";
					CompanyDbObj company = dataAccess.getCompanyByGroupId(groupId);
					if (company != null) {
						coId = company.getCompanyId();
					}
					AccessCodeDbObj accessCode = new AccessCodeDbObj(accessCodeStr, coId, "", peripheralDeviceId, 0,
							true, false);
					dataAccess.addAccessCode(accessCode, "addNewDevice");
					if (dataAccess.addDevice(device)) {
						dataAccess.addLog(peripheralDeviceId, "device Added", LogConstants.TRACE);
						if (fromTextCode) {
							dataAccess.addLog("will text code");
							new TextMessage().textCode(company, central, connection);
						}
						outcome = Outcomes.SUCCESS;
						if (central.getOperatingSystem() == OsClass.DUMBPHONE) {
							PushNotifications push = new PushNotifications();
							push.sendLoudPush(device, central, false);
						}
					}
				}
			} else {
				reason = "error finding central for group: " + groupId;
				dataAccess.addLog(reason, LogConstants.WARNING);
			}
		} catch (Exception e) {
			dataAccess.addLog("addNewDevice", e);
			reason = e.getLocalizedMessage();
		}
		return new BasicResponse(outcome, reason, accessCodeStr);
	}

	public BasicResponse signupNewUser(HttpServletRequest request, String peripheralDeviceId, String key, String iv) {
		int outcome = Outcomes.FAILURE;
		String accessCodeStr = "";
		String loginToken = this.getEncryptedRequestValue(request, "ajfli488s", key, iv);
		SamlDataAccess dataAccess = new SamlDataAccess();
		dataAccess.addLog(peripheralDeviceId, "Signup new user", LogConstants.IMPORTANT);
		DeviceDbObj previousDevice = dataAccess.getDeviceByMachineId(loginToken);
		if (previousDevice != null) {
			if (dataAccess.deleteDevice(previousDevice)) {
				if (dataAccess.deleteConnectionByPeripheralDevice(previousDevice)) {
					dataAccess.addLog(peripheralDeviceId, "deleted old device and connection for same machineId");
				} else {
					dataAccess.addLog(peripheralDeviceId,
							"FAILED to delete old connection for same machineId.  This blows.", LogConstants.WARNING);
				}
			} else {
				dataAccess.addLog(peripheralDeviceId, "FAILED to delete old device for same machineId.  This blows.",
						LogConstants.WARNING);
			}
		}
		String service = this.getEncryptedRequestValue(request, "9UmIoh", key, iv).toUpperCase();
		String characteristic = this.getEncryptedRequestValue(request, "auneohunhsb", key, iv).toUpperCase();
		String deviceType = this.getEncryptedRequestValue(request, "deviceType", key, iv);
		// String deviceType = "peripheral";
		OsClass os = OsClass.valueOf(this.getEncryptedRequestValue(request, "os", key, iv).toUpperCase());

		String osVersion = this.getEncryptedRequestValue(request, "osv", key, iv).replace(".", "_");
		Integer gmtOffset = this.getEncryptedGmtOffset(request, "ADFainss", key, iv);
		String language = this.getEncryptedRequestValue(request, "afjlibui", key, iv);
		String btAddress = this.getEncryptedRequestValue(request, "bta", key, iv);// null for OSX
		String screenSize = this.getEncryptedRequestValue(request, "po2jn3", key, iv);
		String groupId = this.getEncryptedRequestValue(request, "groupToken", key, iv);
		Boolean hasBle = this.getEncryptedRequestBoolean(request, "nthnsss", key, iv, true);
		Boolean multiUser = this.getEncryptedRequestBoolean(request, Parameters.ADD_PERIPHERAL_MULTIUSER, key, iv,
				false);
		DeviceClass deviceClass;
		try {
			deviceClass = DeviceClass.valueOf(this.getEncryptedRequestValue(request, "class", key, iv).toUpperCase());
		} catch (Exception e) {
			deviceClass = DeviceClass.UNKNOWN;
		}
		int seed = GeneralUtilities.randInt(0, 1000000000);
		String userId = GeneralUtilities.randomString();
		dataAccess.addLog(peripheralDeviceId, "creating device with Id: " + peripheralDeviceId,
				LogConstants.TRACE);
		String rand = "";// GeneralUtilities.randomString(4095);
		Double priority = this.getInitialDevicePriority(os);

		Timestamp baseDate = DateTimeUtilities.getBaseTimestamp();
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		DeviceDbObj device = new DeviceDbObj(groupId, userId, peripheralDeviceId, seed, false, null, btAddress,
				new Date(), now, deviceType, os, loginToken, null, gmtOffset, osVersion, language, screenSize, rand,
				true, priority, false, 0, false, baseDate, false, false, "", "", false, now, false, baseDate, false,
				deviceClass, false, true, baseDate, null, hasBle, false, null, multiUser, !multiUser, null, false);
		DeviceConnectionDbObj connection = new DeviceConnectionDbObj(now, GeneralUtilities.randomString(25),
				peripheralDeviceId, "", service, characteristic, "", false, baseDate, baseDate, false, baseDate, false,
				baseDate, baseDate, baseDate, false, baseDate, false, null, null, null, null, null, false, "");
		if (dataAccess.addConnection(connection)) {
			accessCodeStr = GeneralUtilities.randomString(40);
			String companyId = "";
			CompanyDbObj company = dataAccess.getCompanyByDevId(peripheralDeviceId);
			if (company != null) {
				companyId = company.getCompanyId();
			}
			AccessCodeDbObj accessCode = new AccessCodeDbObj(accessCodeStr, companyId, "", peripheralDeviceId, 0, true,
					false);
			dataAccess.addAccessCode(accessCode, "signupNewUser");
			if (dataAccess.addDevice(device)) {
				outcome = Outcomes.SUCCESS;
			}
		}
		return new BasicResponse(outcome, "", accessCodeStr);
	}

	/**
	 * verify that the 7th char is f, b, or 2 11th is w, l, or b 33rd is O, I, or n
	 * and 37th is p, E, 7
	 */
	public boolean validateDeviceId(String deviceId) {
		boolean success = false;
		DataAccess dataAccess = new DataAccess();
		try {
			dataAccess.addLog("vaildateDeviceId", "deviceId len: " + deviceId.length());
			if (deviceId.length() == 40) {
				String[] deviceIdArray = deviceId.split("");
				String[] array1 = { "f", "b", "2" };
				if (Arrays.asList(array1).contains(deviceIdArray[6])) {
					String[] array2 = { "w", "l", "b" };
					dataAccess.addLog("vaildateDeviceId", "10th char: " + deviceIdArray[10]);
					if (Arrays.asList(array2).contains(deviceIdArray[10])) {
						String[] array3 = { "0", "I", "n" };
						dataAccess.addLog("vaildateDeviceId", "33rd char: " + deviceIdArray[32]);
						if (Arrays.asList(array3).contains(deviceIdArray[32])) {
							String[] array4 = { "p", "E", "7" };
							dataAccess.addLog("vaildateDeviceId", "37th char: " + deviceIdArray[36]);
							if (Arrays.asList(array4).contains(deviceIdArray[36])) {
								success = true;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			dataAccess.addLog("vaildateDeviceId", e);
		}
		return success;
	}
}
