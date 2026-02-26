package com.humansarehuman.blue2factor.authentication.api;

import java.sql.Timestamp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.CheckType;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.SSH_PRECHECK_NEW)
@SuppressWarnings("ucd")
public class NewSshPrep extends BaseController {

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public @ResponseBody ApiResponseWithToken sshPrecheckProcessPost(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("start");
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, "nsahusarchanthdut", key, iv);
			dataAccess.addLog("deviceId=" + deviceId);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId, "newSshPrepProcessPost");
			if (device != null && device.isActive()) {
				dataAccess.addLog("device looks good");
				if (device.getSignedIn()) {
					if (dataAccess.isAccessAllowed(device, "sshPrecheckProcessPost")) {
						GroupDbObj group = dataAccess.getGroupByDeviceId(deviceId);
						if (group != null && group.isActive()) {
							dataAccess.addLog("group looks good");
							String clientIpAddress = this.getEncryptedRequestValue(request, "co0bnesuhsahbnG", key, iv);
							CompanyDbObj company = dataAccess.getCompanyById(group.getCompanyId());
							if (company != null && company.isActive()) {
								dataAccess.addLog("company looks good");
								String firstLetter = GeneralUtilities.randomLetters(1);
								String serverDeviceToken = firstLetter + GeneralUtilities.randomString(19);
								Timestamp expiration = DateTimeUtilities.getCurrentTimestampPlusSeconds(60);
								String[] deviceInstanceIdPair = new Encryption().createEncryptedInstanceIdForSsh(device,
										firstLetter);
								// expire this check as soon as it's validated
								CheckDbObj check = new CheckDbObj(GeneralUtilities.randomString(),
										GeneralUtilities.randomString(), null, device.getDeviceId(), null,
										group.getGroupId(), clientIpAddress, null, null, null, false, false,
										Outcomes.UNKNOWN_STATUS, DateTimeUtilities.getCurrentTimestamp(), null, false,
										CheckType.SSH_CONN, serverDeviceToken, deviceInstanceIdPair[0], expiration);
								dataAccess.addLog("adding check");
								dataAccess.addCheck(check);
								token = serverDeviceToken;
								reason = deviceInstanceIdPair[1];
								outcome = Outcomes.SUCCESS;
							} else {
								reason = Constants.CO_NOT_FOUND;
							}
						}
					} else {
						reason = Constants.NOT_PERMITTED;
						CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
						token = company.getCompleteCompanyLoginUrl();
					}

				} else {
					reason = Constants.SIGNED_OUT;
				}
			} else {
				reason = Constants.DEVICE_INACTIVE;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
		}
		ApiResponseWithToken resp = new ApiResponseWithToken(outcome, reason, token);
		return resp;
	}

	/*
	 * @RequestMapping(method = RequestMethod.POST, produces =
	 * MediaType.APPLICATION_JSON_VALUE) public @ResponseBody ApiResponseWithToken
	 * processPost(HttpServletRequest request, HttpServletResponse httpResponse,
	 * ModelMap model) { int outcome = Outcomes.FAILURE; String reason = ""; String
	 * token = ""; ApiResponseWithToken resp = null; CompanyDataAccess dataAccess =
	 * new CompanyDataAccess(); dataAccess.addLog("NewSshPrep", "start"); try {
	 * getVersion(request); String key = getKey(request); String iv =
	 * getInitVector(request); String deviceId =
	 * this.getEncryptedRequestValue(request, "nsahusarchanthdut", key, iv);
	 * dataAccess.addLog("NewSshPrep", "deviceId=" + deviceId); DeviceDbObj device =
	 * dataAccess.getDeviceByDeviceId(deviceId); if (device != null) { if
	 * (device.isActive()) { if (device.getSignedIn()) { CompanyDbObj company =
	 * dataAccess.getCompanyByDevId(device.getDeviceId());
	 * dataAccess.addLog("NewSshPrep", "device found"); String signedToken =
	 * this.getEncryptedRequestValue(request, "usanehuss", key, iv); String
	 * encryptedData = this.getEncryptedRequestValue(request, "ssbcgie", key, iv)
	 * .replace("+", "-").replace("/", "_"); dataAccess.addLog("NewSshPrep",
	 * "signedToken: " + signedToken); dataAccess.addLog("NewSshPrep",
	 * "encryptedData: " + encryptedData); Encryption encryption = new Encryption();
	 * KeyDbObj coPvtKey =
	 * dataAccess.getCompanyServerPrivateSshKey(company.getCompanyId()); String
	 * decryptedData = encryption.decryptUrlSafeWithPrivateKey(coPvtKey,
	 * encryptedData); dataAccess.addLog("NewSshPrep", "decryptedData: " +
	 * decryptedData); if (!TextUtils.isBlank(decryptedData)) { resp =
	 * this.validateDecryptedData(company, device, coPvtKey, decryptedData,
	 * dataAccess, signedToken); } else { reason = Constants.DECRYPTION_ERROR; } }
	 * else { reason = Constants.SIGNED_OUT; } } else { reason =
	 * Constants.DEVICE_INACTIVE + "\n\nPlease visit: " + Urls.SECURE_URL +
	 * Urls.SIGN_IN; } } else { reason = Constants.DEVICE_NOT_FOUND; } } catch
	 * (Exception e) { dataAccess.addLog("NewSshPrep", e); reason =
	 * e.getLocalizedMessage(); } if (resp == null) { resp = new
	 * ApiResponseWithToken(outcome, reason, token); } return resp; }
	 */

	private boolean tokenAlreadyUsed(String tokenId, String groupId, CompanyDataAccess dataAccess) {
		boolean alreadyUsed = false;
		TokenDbObj token = dataAccess.getToken(tokenId);
		if (token != null) {
			dataAccess.addLog("token was already used");
			alreadyUsed = true;
		} else {
			dataAccess.addLog("valid token found");
			Timestamp yesterday = DateTimeUtilities.getCurrentTimestampMinusHours(24);
			dataAccess.addTokenWithId(null, null, null, tokenId, TokenDescription.SSH, 0, null, yesterday);
		}
		return alreadyUsed;
	}

	@SuppressWarnings("unused")
	private ApiResponseWithToken validateDecryptedData(CompanyDbObj company, DeviceDbObj device, KeyDbObj pvtKey,
			String decryptedData, CompanyDataAccess dataAccess, String signedToken) throws Exception {
		int outcome = Outcomes.FAILURE;
		String token = "";
		String reason = "";
		String[] decryptedSplit = decryptedData.split(":");
		if (decryptedSplit.length == 3) {
			String remoteHost = decryptedSplit[0];
			dataAccess.addLog("remoteHost: " + remoteHost);
			String user = decryptedSplit[1];
			String clientToken = decryptedSplit[2];
			if (!tokenAlreadyUsed(clientToken, device.getGroupId(), dataAccess)) {
				Encryption encryption = new Encryption();
				KeyDbObj key = Encryption.getDeviceSshTerminalKey(device);// public key
				if (key != null) {
					if (encryption.verifyPreSshSignature(key, clientToken, signedToken, device.getOperatingSystem(),
							dataAccess)) {
						dataAccess.addLog("signature verified");
						GroupDbObj group = dataAccess.getGroupByUid(user, company.getCompanyId());
						if (group != null) {
							dataAccess.addLog("group found");
							if (group.getGroupId().equals(device.getGroupId())) {

								String newToken = GeneralUtilities.randomLetters(25);
								// we use the linux class here because this will be decrypted with
								// openSSl
								token = encryption.encryptWithKeyDbObj(device, key, newToken, dataAccess);
								if (!TextUtils.isBlank(token)) {
									dataAccess.addLog("encryption worked");

									if (key != null) {
										reason = encryption.signStringForSsh(pvtKey, newToken);
										dataAccess.addLog("reason: '" + reason + "'");
										if (reason != null) {
											Timestamp baseTime = DateTimeUtilities.getBaseTimestamp();
											dataAccess.addTokenWithId(group.getGroupId(), device.getDeviceId(), null,
													newToken, TokenDescription.SSH, 0, null, baseTime);
											outcome = Outcomes.SUCCESS;
										}
									}
								} else {
									reason = Constants.ENCRYPTION_ERROR;
								}
							} else {
								dataAccess.addLog(group.getGroupId() + " != " + device.getGroupId());
								reason = Constants.INVALID_USER;
							}
						}
					}
				} else {
					reason = Constants.KEY_NOT_FOUND;
				}
			} else {
				dataAccess.addLog("token already used");
				reason = Constants.TOKEN_ERROR;
			}
		} else {
			reason = Constants.SIGNATURE_VALIDATION_FAILED;
		}
		return new ApiResponseWithToken(outcome, reason, token);
	}
}
