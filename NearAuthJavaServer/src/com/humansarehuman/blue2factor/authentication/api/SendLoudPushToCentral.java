package com.humansarehuman.blue2factor.authentication.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(value = Urls.SEND_LOUD_PUSH)
public class SendLoudPushToCentral extends B2fApi {
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken sendLoudPushProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		ApiResponseWithToken response = new ApiResponseWithToken(Outcomes.FAILURE, "", "");
		getVersion(request);
		String key = getKey(request);
		String iv = getInitVector(request);
		String devId = this.getEncryptedRequestValue(request, "rslledde", key, iv);
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		if (!TextUtils.isBlank(devId)) {
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(devId);
			if (device != null) {
				int outcome = handleNotProximate(device);
				response.setOutcome(outcome);
			}
		}
		return response;
	}
}
