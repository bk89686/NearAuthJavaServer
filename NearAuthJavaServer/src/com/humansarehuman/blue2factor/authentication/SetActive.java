package com.humansarehuman.blue2factor.authentication;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.SET_ACTIVE)
@SuppressWarnings("ucd")
public class SetActive extends BaseController {
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
			String deviceId = this.getEncryptedRequestValue(request, "shshsnshoeuj", key, iv);
			boolean active = this.getEncryptedRequestBoolean(request, "rchbauehtd", key, iv, false);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				device.setActive(active);
				if (device.isCentral()) {
					ArrayList<DeviceConnectionDbObj> conns = dataAccess.getConnectionsForCentral(device);
					DeviceDbObj perf;
					for (DeviceConnectionDbObj conn : conns) {
						perf = dataAccess.getDeviceByDeviceId(conn.getPeripheralDeviceId());
						if (perf != null && perf.isActive()) {
							if (!active) {
								dataAccess.updateQuickAccess(device, false);
							}
							dataAccess.updateAsActive(conn);
							reason = Boolean.toString(active);
							outcome = Outcomes.SUCCESS;
						}
					}
				} else {
					DeviceConnectionDbObj con = dataAccess.getConnectionForPeripheral(device, true);
					if (con != null) {
						dataAccess.updateAsActive(con);
						reason = Boolean.toString(active);
						outcome = Outcomes.SUCCESS;
					}
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
