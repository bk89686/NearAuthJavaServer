package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.SET_NEEDS_PAIRING)
@SuppressWarnings("ucd")
public class SetNeedsPairing extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String setActiveProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, Parameters.NEEDS_PAIRING_DEVICE, key, iv);
			String serviceUuid = this.getEncryptedRequestValue(request, Parameters.NEEDS_PAIRING_SERVICE, key, iv);
			boolean needsPairing = this.getEncryptedRequestBoolean(request, Parameters.DOES_NEEDS_PAIRING, key, iv,
					true);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				DeviceConnectionDbObj conn = dataAccess.getConnectionByServiceUuid(serviceUuid);
				if (conn != null) {// it should always be central turning it on
									// and peripheral turning it off (I think)-- cjm
					dataAccess.updateNeedPairing(conn, needsPairing);
					outcome = Outcomes.SUCCESS;
				} else {
					reason = Constants.CONNECTION_NOT_FOUND;
				}
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
