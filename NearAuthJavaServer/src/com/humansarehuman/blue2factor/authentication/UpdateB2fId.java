package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.UPDATE_B2FID)
@SuppressWarnings("ucd")
public class UpdateB2fId extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String updateB2fIdProcessGet(HttpServletRequest request, ModelMap model) {
		getVersion(request);
		String key = getKey(request);
		String iv = getInitVector(request);
		String deviceId = this.getEncryptedRequestValue(request, "nshcarhbjs", key, iv);
		String b2fId = this.getEncryptedRequestValue(request, "mcdganuek3m", key, iv);
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String accessStr = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog(deviceId, "entry", LogConstants.DEBUG);
		int changes = 0;
		DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
		if (device != null) {
			dataAccess.addLog(deviceId, "deviceFound", LogConstants.DEBUG);
			try {
				if (dataAccess.isAccessAllowed(device, "updateB2fIdProcessGet")) {
					dataAccess.addLog(deviceId, "recentSuccess", LogConstants.DEBUG);
					accessStr = GeneralUtilities.randomString();
					CompanyDbObj company = dataAccess.getCompanyByDevId(deviceId);
					if (company != null) {
						AccessCodeDbObj accessCode = new AccessCodeDbObj(DateTimeUtilities.getCurrentTimestamp(),
								accessStr, company.getCompanyId(), "", deviceId, 0, true, false);
						dataAccess.addAccessCode(accessCode, "UpdateB2fId GET");
						dataAccess.addLog(device.getDeviceId(), "adding token: " + b2fId, LogConstants.DEBUG);
						reason = company.getCompleteCompanyLoginUrl();
						outcome = Outcomes.SUCCESS;
					}

				} else {
					reason = "This device wasn't proximate to another NearAuth.ai device.";
					dataAccess.addLog(device.getDeviceId(), "not proximate", LogConstants.DEBUG);
				}

			} catch (Exception e) {
				reason = e.getLocalizedMessage();
				dataAccess.addLog(deviceId, e);
			}
		} else {
			dataAccess.addLog(deviceId, "device not Found", LogConstants.DEBUG);
			reason = Constants.DEVICE_NOT_FOUND;
		}
		if (changes > 0) {
			reason = Integer.toString(changes) + " btids changed.";
		}
		BasicResponse response = new BasicResponse(outcome, reason, accessStr);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String updateB2fIdProcessPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		String deviceId = "";
		String accessStr = "";
		dataAccess.addLog(deviceId, "entry", LogConstants.DEBUG);
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			deviceId = this.getEncryptedRequestValue(request, "nshcarhbjs", key, iv);
			String b2fId = this.getEncryptedRequestValue(request, "mcdganuek3m", key, iv);
			dataAccess.addLog(deviceId, "b2fid to: " + b2fId);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				if (dataAccess.isAccessAllowed(device, "updateB2fIdProcessPost")) {
					dataAccess.addLog(deviceId, "recentSuccess", LogConstants.DEBUG);
					CompanyDbObj company = dataAccess.getCompanyByDevId(deviceId);
					if (company != null) {
						accessStr = GeneralUtilities.randomString();
						AccessCodeDbObj accessCode = new AccessCodeDbObj(DateTimeUtilities.getCurrentTimestamp(),
								accessStr, company.getCompanyId(), "", deviceId, 0, true, false);
						dataAccess.addAccessCode(accessCode, "UpdateB2fId POST");
						dataAccess.addLog(device.getDeviceId(), "adding token: " + b2fId, LogConstants.DEBUG);
						reason = company.getCompleteCompanyLoginUrl();
						dataAccess.setLastReset(device, DateTimeUtilities.getCurrentTimestamp());
						outcome = Outcomes.SUCCESS;
					}

				} else {
					reason = "This device wasn't proximate to another NearAuth.ai device.";
					dataAccess.addLog(device.getDeviceId(), "not proximate", LogConstants.DEBUG);
				}
			} else {
				dataAccess.addLog(deviceId, "device was null", LogConstants.DEBUG);
				reason = Constants.DEVICE_NOT_FOUND;
			}
		} catch (Exception e) {
			dataAccess.addLog(deviceId, e);
		}
		dataAccess.addLog(deviceId, "resync outcome: " + outcome + ", reason: " + reason, LogConstants.DEBUG);
		BasicResponse response = new BasicResponse(outcome, reason, accessStr);
		model = this.addBasicResponse(model, response);
		return "result";
	}

}
