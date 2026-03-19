package com.humansarehuman.blue2factor.authentication.install;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.communication.PushNotifications;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.utilities.Encryption;

@Controller
@RequestMapping(Urls.SUBMIT_KEY)
@SuppressWarnings("ucd")
public class SubmitKey extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String submitKeyProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		String devId = this.getRequestValue(request, "nsauhnsuha");
		boolean foreground = this.getRequestValueBoolean(request, "fg", false);
		boolean sshKey = this.getRequestValueBoolean(request, "cli", false);
		boolean resend = this.getRequestValueBoolean(request, "resend", false);
		boolean postNearAuth = this.getRequestValueBoolean(request, "postNearAuth", false);
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog(devId, "deviceId: " + devId + ", foreground: " + foreground + ", sshKey: " + 
				sshKey + ", postNearAuth: " +postNearAuth, LogConstants.IMPORTANT);
		DeviceDbObj device = dataAccess.getDeviceByDeviceId(devId);
		try {
			if (device != null && postNearAuth) {
				String publicKey = this.getRequestValue(request, "eausnht");
				dataAccess.addLog(devId, "device found for updating key to: " + publicKey, LogConstants.TRACE);
				if (!TextUtils.isBlank(publicKey)) {
					if (sshKey) {
						String outgoingKey = addSshPublicKeyAndGetPublicKey(device, publicKey, dataAccess);
						if (outgoingKey != null) {
							outcome = Outcomes.SUCCESS;
							token = outgoingKey;
						} else {
							dataAccess.addLog(devId, "sshPublicKey not added", LogConstants.WARNING);
						}
					} else {
						if (foreground) {
							this.addDeviceForegroundPublicKey(device, null, publicKey, "");
						} else {
							// TODO: consider whether we want to do this for peripheral devices - cjm
							// dataAccess.expireChecksForDevice(device);
							CompanyDbObj company = dataAccess.getCompanyByDevId(devId);
							if (company != null) {
								token = addDevicePublicKeyAndReturnPublicKey(company.getCompanyId(), device,
										device.getGroupId(), publicKey, "", dataAccess);
							}
						}
						PushNotifications push = new PushNotifications();
						if (!device.isCentral()) {
							push.sendOneNewSilentPushFromPeripheral(device, "SubmitKey", false, false, true, false);
						} else if (resend) {
							push.resendLoudPush(device);
						}
						dataAccess.addLog(devId, "key set to: " + publicKey);
						outcome = Outcomes.SUCCESS;
					}
				} else {
					if (!postNearAuth) {
						reason = "public key was blank";
					} else {
						reason = Constants.DEV_NOT_FOUND;
					}
				}
			} else {
				reason = "outdated version";
			}
		} catch (Exception e) {
			reason = e.getLocalizedMessage();
			dataAccess.addLog(e);
		}
		if (!TextUtils.isBlank(reason)) {
			dataAccess.addLog("failed with reason: " + reason, LogConstants.WARNING);
		}
		BasicResponse response = new BasicResponse(outcome, reason, token);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	private String addSshPublicKeyAndGetPublicKey(DeviceDbObj device, String publicKey, CompanyDataAccess dataAccess) {
		boolean success = false;
		String outgoingKey = null;
		try {
			dataAccess.addLog("adding terminal key");
			dataAccess.expireKeysByTypeAndDeviceId(KeyType.TERMINAL_SSH_PUBLIC_KEY, device.getDeviceId());
			CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
			GroupDbObj group = dataAccess.getGroupByDeviceId(device.getDeviceId());
			if (company != null) {
				KeyDbObj key = new KeyDbObj(device.getDeviceId(), null, device.getGroupId(), company.getCompanyId(),
						KeyType.TERMINAL_SSH_PUBLIC_KEY, publicKey, true, "RSA", null);
				dataAccess.addKey(key);
				Encryption encryption = new Encryption();
				outgoingKey = encryption.createAndSaveKeySshTerminal(company, group, device.getDeviceId(), dataAccess);
				success = true;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		dataAccess.addLog("success = " + success);
		return outgoingKey;
	}

}