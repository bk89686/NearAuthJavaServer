package com.humansarehuman.blue2factor.authentication.install;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.jsonConversion.JsRequest;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.IS_INSTALL_COMPLETE_FROM_JS)
@SuppressWarnings("ucd")
public class IsInstallCompleteFromJs extends BaseController {
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken processGet(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		ApiResponseWithToken response = new ApiResponseWithToken(Outcomes.FAILURE, Constants.METHOD_NOT_ALLOWED, "");
		return response;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken isInstallationCompleteProcessPost(@RequestBody JsRequest jsRequest,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String tokenStr = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String peripheralId = jsRequest.getToken();
			DeviceDbObj peripheral = dataAccess.getDeviceByDeviceId(peripheralId);
			if (peripheral != null) {
				if (peripheral.isActive()) {
					AccessCodeDbObj accessCode = dataAccess.addAccessCode(peripheral.getDeviceId(),
							"IsInstallCompleteFromJs");
					tokenStr = accessCode.getAccessCode();
					outcome = Outcomes.SUCCESS;
				} else {
					reason = "connection not active";
				}
			} else {
				reason = "peripheral is null for id: " + peripheralId;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getMessage();
		}
		ApiResponseWithToken response = new ApiResponseWithToken(outcome, reason, tokenStr);
		return response;
	}
}
