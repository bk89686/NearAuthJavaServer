package com.humansarehuman.blue2factor.authentication.saml;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

/**
 * Comes from the device app to see if the user has completed signing in yet
 * 
 * @author cjm10
 *
 */
@Controller
@RequestMapping(Urls.CHECK_FOR_SAML_SUCCESS)
@SuppressWarnings("ucd")
public class CheckForSamlSuccess extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String checkForSamlSuccessProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		boolean prevInstall = false;
		SamlDataAccess dataAccess = new SamlDataAccess();
		String accessStr = "";
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String groupId = this.getEncryptedRequestValue(request, "hachu8aebuh", key, iv);
			String deviceId = this.getEncryptedRequestValue(request, "sssohetc", key, iv);
			if (!TextUtils.isBlank(groupId)) {
				GroupDbObj group = dataAccess.getGroupById(groupId);
//				DeviceDbObj currentDevice = dataAccess.getDeviceByDeviceId(deviceId, "checkForSamlSuccessProcessPost");
				if (group != null) {
					if (dataAccess.isSuccessfulAuthnByGroup(group)) {
//					if (dataAccess.getSuccessfulAuthnByDevice(currentDevice)) {
						outcome = Outcomes.SUCCESS;
						ArrayList<DeviceDbObj> devices = dataAccess.getActiveDevicesFromGroup(group);
						dataAccess.addLog("active: " + devices.size());
						if (devices.size() > 1) {
							prevInstall = true;
						}
						for (DeviceDbObj device : devices) {
							if (device.isCentral()) {
								dataAccess.addLog("central found");
								reason = device.getDeviceType();
							}
						}
						accessStr = GeneralUtilities.randomString();
						AccessCodeDbObj accessCode = new AccessCodeDbObj(DateTimeUtilities.getCurrentTimestamp(),
								accessStr, group.getCompanyId(), "", deviceId, 0, true, false);
						dataAccess.addAccessCode(accessCode, "setupAddedDevice");
					} else {
						reason = Constants.SAML_SUCCESS_NOT_FOUND;
					}
				} else {
					reason = Constants.DEVICE_NOT_FOUND;
				}
			} else {
				reason = Constants.DEVICE_ID_WAS_BLANK;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		BasicResponse response = new BasicResponse(outcome, reason, Boolean.toString(prevInstall), accessStr);
		model = this.addBasicResponse(model, response);
		return "resultWithInstanceId";
	}
}
