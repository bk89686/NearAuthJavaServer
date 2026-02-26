package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import java.sql.Timestamp;

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
import com.humansarehuman.blue2factor.entities.enums.CheckType;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.SUBMIT_ENCRYPTED_PERIPHERAL_CONNECTION)
@SuppressWarnings("ucd")
public class SubmitEncryptedPeripheralConnection extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String encryptedPeripheralConnectionPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request,
					Parameters.SUBMIT_ENCRYPTED_PERIPHERAL_CONNECTION_DEVICE_ID, key, iv);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				String signed = this.getEncryptedRequestValue(request,
						Parameters.SUBMIT_ENCRYPTED_PERIPHERAL_CONNECTION_SIGNED, key, iv);
				String encrypted = this.getEncryptedRequestValue(request,
						Parameters.SUBMIT_ENCRYPTED_PERIPHERAL_CONNECTION_ENCRYPTED, key, iv);
				Encryption encryption = new Encryption();
				KeyDbObj privateKeyObj = dataAccess.getKeyByTypeAndDeviceId(KeyType.SERVER_PRIVATE_KEY_FOR_DEVICE,
						deviceId);
				if (privateKeyObj != null) {
					String decrypted = encryption.decryptWithPrivateKey(privateKeyObj, encrypted);
					if (decrypted != null) {
						dataAccess.addLog("decrypted: " + decrypted);
						if (encryption.verifySignatureWithBackgroundKey(device, decrypted, signed)) {
							dataAccess.addLog("verification succeeded");
							DeviceConnectionDbObj connection = dataAccess.getConnectionForPeripheral(device, true);
							if (connection != null) {
								if (dataAccess.hasPreviouslyConnectedWithin24Hours(deviceId,
										connection.getCentralDeviceId())) {
									String ipAddress = GeneralUtilities.getClientIp(request);
									Timestamp now = DateTimeUtilities.getCurrentTimestamp();
									CheckDbObj check = new CheckDbObj(GeneralUtilities.randomString(),
											GeneralUtilities.randomString(20), connection.getCentralDeviceId(),
											connection.getPeripheralDeviceId(), connection.getServiceUuid(),
											device.getUserId(), ipAddress, "", null, null, false, true,
											Outcomes.SUCCESS, now, now, true, CheckType.CONNECTION_FROM_PERIPHERAL,
											null, null);
									dataAccess.addCheck(check);
									dataAccess.addLog(connection.getPeripheralDeviceId(),
											connection.getServiceUuid() + " updated as successful",
											LogConstants.IMPORTANT);
									dataAccess.updateAsPeripheralConnected(connection);
									outcome = Outcomes.SUCCESS;
								} else {
									reason = Constants.STALE_PUSH;
								}
							} else {
								reason = Constants.CONNECTION_NOT_FOUND;
							}
						} else {
							dataAccess.addLog("verification failed");
							reason = Constants.SIGNATURE_VALIDATION_FAILED;
						}
					} else {
						dataAccess.addLog("decryption failed");
						reason = Constants.DECRYPTION_ERROR;
					}
				} else {
					reason = Constants.KEY_NOT_FOUND;
				}
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
			}
			if (reason != "") {
				dataAccess.addLog(deviceId, reason, LogConstants.WARNING);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

}
