package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import java.sql.Timestamp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.jsonConversion.TwoTokens;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.SSH_PRECHECK)
@SuppressWarnings("ucd")
public class SshPrecheck extends BaseController {

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody TwoTokens sshPrecheckProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		String encryptedToken1 = null;
		String tokenStr2 = null;
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, "nsahusarchanthdut", key, iv);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId, "SshPrecheck");
			if (device != null) {
				if (device.getSignedIn() && device.isActive()) {
					dataAccess.addLog("device found");
					String randomLetter = GeneralUtilities.randomLetters(1);
					String tokenStr1 = GeneralUtilities.randomString() + randomLetter;
					tokenStr2 = GeneralUtilities.randomString() + randomLetter;
					Encryption encryption = new Encryption();
					KeyDbObj signingKey = dataAccess.getClientSshSignatureKeyFromDeviceId(device.getDeviceId());
					if (signingKey != null) {
						reason = encryption.signStringForSsh(signingKey, tokenStr2);
						Timestamp oneMinute = DateTimeUtilities.getCurrentTimestampPlusSeconds(65);
						dataAccess.addTokenWithId(device.getGroupId(), deviceId, null, tokenStr1,
								TokenDescription.SERVER_CONNECTION, 0, null, oneMinute);

						encryptedToken1 = encryption.encryptStringWithPublicKey(device, tokenStr1);
						// the deviceId we are using here isn't what we want. We actually want the
						// serverID
						// which we don't know at this point, but we can later use this deviceId to
						// identify the company and then identify the correct private key
						dataAccess.addTokenWithId(device.getGroupId(), deviceId, null, tokenStr2,
								TokenDescription.SERVER_CONNECTION, 0, null, oneMinute);
						outcome = Outcomes.SUCCESS;
					} else {
						reason = Constants.KEY_NOT_FOUND;
					}
				} else {
					reason = Constants.SIGNED_OUT;
				}
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
		}
		return new TwoTokens(outcome, encryptedToken1, tokenStr2, reason);
	}
}
