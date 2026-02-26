package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

/*
 * Signs the user out of the first factor ID only
 */
@Controller
@RequestMapping(Urls.DEVICE_SIGNOUT)
@SuppressWarnings("ucd")
public class Signout extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String signoutProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		SamlDataAccess dataAccess = new SamlDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, "nshashshsoo", key, iv);
			Boolean firstFactor = this.getEncryptedRequestBoolean(request, Parameters.SIGNOUT_FIRST_FACTOR, key, iv,
					false);
			Boolean secondFactor = this.getEncryptedRequestBoolean(request, Parameters.SIGNOUT_SECOND_FACTOR, key, iv,
					false);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				if (firstFactor) {
					dataAccess.addLog(deviceId, "user is signing out - post", LogConstants.IMPORTANT);
					dataAccess.setSignedIn(device, false);
				}
				if (secondFactor) {
					dataAccess.expirePushAndBioChecksForPeripheralDevice(device);
				}
				outcome = Outcomes.SUCCESS;
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

}
