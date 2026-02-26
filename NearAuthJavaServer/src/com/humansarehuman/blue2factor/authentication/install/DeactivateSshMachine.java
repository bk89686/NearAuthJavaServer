package com.humansarehuman.blue2factor.authentication.install;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(Urls.DEACTIVATE_SSH_MACHINE)
@SuppressWarnings("ucd")
public class DeactivateSshMachine extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGetAuthSshClient(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processPostDeactivateSshMachine(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		String key = getKey(request);
		String iv = getInitVector(request);
		
		String deviceId = this.getEncryptedRequestValue(request, Parameters.DEVICE_ID_FOR_DEACTIVE_SSH_MACHINE, key, iv);
		//Should we encrypt this key? - cjm
		if (!TextUtils.isBlank(deviceId)) {
			dataAccess.deactivateTerminalSshKeys(deviceId);
			outcome = Outcomes.SUCCESS;
		} else {
			reason = Constants.DEV_FOUND_BY_MACHINE_ID;
		}
		BasicResponse response = new BasicResponse(outcome, reason, token);
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
