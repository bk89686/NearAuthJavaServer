package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

@Controller
@RequestMapping(Urls.REPORT_CONNECTING)
@SuppressWarnings("ucd")
public class ReportConnecting extends BaseController {

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String reportConnectingProcessPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		getVersion(request);

		String key = getKey(request);
		String iv = getInitVector(request);
		String deviceId = this.getEncryptedRequestValue(request, "nraeh", key, iv);
		String serviceUuid = this.getEncryptedRequestValue(request, "nthsnb", key, iv);
		dataAccess.addLog(deviceId, "serviceUuid: " + serviceUuid + " is attempting to connect");
		try {
			DeviceConnectionDbObj connection = dataAccess.getConnectionByServiceUuid(serviceUuid);
			if (connection != null) {
				dataAccess.updateAsConnecting(connection);
				outcome = Outcomes.SUCCESS;
			}
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				dataAccess.setLastSilentPushResponse(device, DateTimeUtilities.getCurrentTimestamp());
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

}