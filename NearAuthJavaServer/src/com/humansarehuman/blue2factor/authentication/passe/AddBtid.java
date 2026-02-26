package com.humansarehuman.blue2factor.authentication.passe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping("/98hgfqhnak23infai")
@SuppressWarnings("ucd")
public class AddBtid extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String addBtidProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		String deviceId = "";
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			deviceId = this.getEncryptedRequestValue(request, "ljafoijnenfv", key, iv);
			String btid = this.getEncryptedRequestValue(request, "fljdfaienfalkwnff", key, iv);
			String serviceUuid = this.getEncryptedRequestValue(request, "djfaieonvurnbav", key, iv);

			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				DeviceConnectionDbObj connection = dataAccess.getConnectionByServiceUuid(serviceUuid);
				if (connection != null) {
					String connectedId;
					if (connection.getCentralDeviceId().equals(deviceId)) {
						connectedId = connection.getCentralDeviceId();
					} else {
						connectedId = connection.getPeripheralDeviceId();
					}
					DeviceDbObj connectedDevice = dataAccess.getDeviceByDeviceId(connectedId);
					dataAccess.setBtAddress(connectedDevice, btid);
					outcome = Outcomes.SUCCESS;
					reason = "";
				} else {
					reason = "devices not connected.";
				}

			} else {
				reason = "device not found by Id";
			}
		} catch (Exception e) {
			dataAccess.addLog(deviceId, e);
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

}
