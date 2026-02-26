package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

//REPORT_GATT_STATUS = "/schhor";
@Controller
@RequestMapping(Urls.REPORT_GATT_STATUS)
@SuppressWarnings("ucd")
public class ReportGattStatus extends BaseController {

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String reportGattStatusPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, "cbnno", key, iv);
			String serviceUuid = this.getEncryptedRequestValue(request, "chmmon", key, iv);
			String statusCode = this.getEncryptedRequestValue(request, "trddnt", key, iv);
			DeviceDbObj caller = dataAccess.getDeviceByDeviceId(deviceId, "reportGattStatusPost");
			if (caller != null) {
				if (statusCode.equals("147") || statusCode.equals("134") || statusCode.equals("1")) {
					if (statusCode.equals("134")) {
						DeviceConnectionDbObj connection = dataAccess.getConnectionByServiceUuid(serviceUuid);
						if (connection != null) {
							dataAccess.updateCentralProximateConnection(connection, true);
						}
					}
					DeviceDbObj otherDevice = dataAccess.getPeripheralByServiceUuid(serviceUuid);
					if (otherDevice != null) {
						if (otherDevice.getDeviceId().equals(deviceId)) {
							otherDevice = dataAccess.getCentralForPeripheral(caller);
						}
						if (otherDevice != null) {
							dataAccess.addLog(deviceId, "updating status code to " + statusCode,
									LogConstants.IMPORTANT);
							if (dataAccess.setCommand(otherDevice, statusCode)) {
								outcome = Outcomes.SUCCESS;
							}
						} else {
							reason = "other device not found";
						}
					} else {
						reason = "connected dev not found for service: " + serviceUuid;
					}
				} else {
					dataAccess.addLog("updating status code to " + statusCode, LogConstants.IMPORTANT);
					if (dataAccess.setCommand(caller, statusCode)) {
						outcome = Outcomes.SUCCESS;
					}
				}
			} else {
				reason = "caller not found with devId: " + deviceId;
			}
		} catch (Exception e) {
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
