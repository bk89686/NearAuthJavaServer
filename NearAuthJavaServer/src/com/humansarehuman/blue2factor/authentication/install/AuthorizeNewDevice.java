package com.humansarehuman.blue2factor.authentication.install;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.AUTHORIZE_NEW_DEVICE)
@SuppressWarnings("ucd")
public class AuthorizeNewDevice extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String authorizeDeviceProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		BasicResponse response;
		String deviceId = "";
		DeviceDbObj device = null;
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			deviceId = this.getEncryptedRequestValue(request, "famoepnfei00ghae", key, iv);
			boolean fromCentral = this.getEncryptedRequestBoolean(request, "ctl", key, iv, false);
			if (deviceId != "") {
				device = dataAccess.getDeviceByDeviceId(deviceId);
			}
			if (device != null) {
				// adding authorization
				dataAccess.addLog(deviceId, "device was found");
				response = this.addAuthorization(device, fromCentral);
			} else {
				// checking for authorization
				dataAccess.addLog(deviceId, "will check for authorization");
				String authId = this.getEncryptedRequestValue(request, "afjeowinfnase", key, iv);
				response = this.checkForAuthorization(request, authId, fromCentral);
			}
		} catch (Exception e) {
			dataAccess.addLog(deviceId, e);
			response = new BasicResponse(outcome, e.getMessage(), "", "");
		}
		model = this.addBasicResponse(model, response);
		return "resultWithInstanceId";
	}

}
