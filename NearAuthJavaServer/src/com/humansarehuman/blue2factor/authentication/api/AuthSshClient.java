package com.humansarehuman.blue2factor.authentication.api;

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
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.SshConnectionDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.AUTHORIZE_SSH_CLIENT)
@SuppressWarnings("ucd")
public class AuthSshClient extends BaseController {

	private String delimeter = ":098!91#:";

	@RequestMapping(method = RequestMethod.GET)
	public String processGetAuthSshClient(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processPostAuthSshClient(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		String clientUser = Constants.USER_NOT_FOUND;
		String serverUser = Constants.USER_NOT_FOUND;
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String ipAddress = GeneralUtilities.getClientIp(request);
			String machineId = this.getEncryptedRequestValue(request, Parameters.MACHINE_ID_FOR_CLIENT, key, iv);
			String encryptedVal = this.getEncryptedRequestValue(request, Parameters.ENCRYPTED_FOR_CLIENT, key, iv);
			String signature = this.getEncryptedRequestValue(request, Parameters.SIGNATURE_FOR_CLIENT, key, iv);
			dataAccess.addLog("machineId: " + machineId);
			DeviceDbObj device = dataAccess.getActiveDeviceByMachineId(machineId);
			String deviceId = "";
			String companyId = "";
			if (device != null) {
				deviceId = device.getDeviceId();
				KeyDbObj keyObj = dataAccess.getSshClientPrivateKey(deviceId);
				if (keyObj != null) {
					Encryption encryption = new Encryption();
					String decryptedString = encryption.decryptWithPrivateKey(keyObj, encryptedVal);
					if (decryptedString != null) {
						if (this.verifySignature(device, decryptedString, signature, dataAccess)) {
							String[] decryptedSplit = decryptedString.split(delimeter);
							if (decryptedSplit.length > 2) {
								serverUser = decryptedSplit[1];
								clientUser = decryptedSplit[2];
								dataAccess.addLog("user found: client: " + clientUser + ", server: " + serverUser);
								GroupDbObj group = dataAccess.getGroupByDeviceId(deviceId);
								if (group != null) {
									companyId = group.getCompanyId();
									if (group.getUid().equals(serverUser)) {
										if (dataAccess.isAccessAllowed(device, "processPostAuthSshClient")) {
											outcome = Outcomes.SUCCESS;
										} else {
											reason = Constants.NOT_PERMITTED;
											CompanyDbObj company = dataAccess.getCompanyById(companyId);
											token = company.getCompleteCompanyLoginUrl();
										}
									} else {
										reason = Constants.INVALID_USER;
									}
								} else {
									reason = Constants.GROUP_NOT_FOUND;
								}
							}
						} else {
							reason = Constants.SIGNATURE_VALIDATION_FAILED;
						}
					} else {
						reason = Constants.DECRYPTION_ERROR;
						token = encryption.getPublicKeyStringFromPrivate(keyObj).replaceAll("[\n\r]", "");
						dataAccess.addLog("new public key: " + token);
					}
				} else {
					reason = Constants.KEY_NOT_FOUND;
				}
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
				dataAccess.addLog("device lookup failed with token " + machineId + " (might not be active)",
						LogConstants.INFO);
			}
			SshConnectionDbObj ssh = new SshConnectionDbObj(companyId, deviceId, "", clientUser, serverUser,
					DateTimeUtilities.getCurrentTimestamp(), ipAddress, outcome);
			dataAccess.addSshConnection(ssh);
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getMessage();
		}
		if (!TextUtils.isBlank(reason)) {
			dataAccess.addLog("failure reason: " + reason);
		}
		BasicResponse response = new BasicResponse(outcome, reason, token);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	private boolean verifySignature(DeviceDbObj device, String plainText, String signature, DataAccess dataAccess) {
		boolean success = false;

		KeyDbObj key = Encryption.getDeviceSshTerminalKey(device);
		Encryption encryption = new Encryption();
		try {
			success = encryption.verifyPreSshSignature(key, plainText, signature, device.getOperatingSystem(),
					dataAccess);
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return success;
	}
}
