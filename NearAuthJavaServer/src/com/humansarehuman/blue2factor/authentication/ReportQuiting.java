package com.humansarehuman.blue2factor.authentication;

import java.util.ArrayList;

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
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.REPORT_QUITING)
@SuppressWarnings("ucd")
public class ReportQuiting extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String reportQuitingPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, Parameters.REPORT_QUITING_DEVICE_ID, key, iv);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId, "reportQuitingPost");
			if (device != null) {
				if (device.isCentral()) {
					disconnectCentral(device, dataAccess);
				} else {
					disconnectPeripheral(device, dataAccess);
				}
				outcome = Outcomes.SUCCESS;
			} else {
				reason = Constants.DEV_NOT_FOUND;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	private void disconnectCentral(DeviceDbObj device, DeviceDataAccess dataAccess) {
		dataAccess.setTurnedOffFromInstaller(device, true);
		new java.util.Timer().schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				ArrayList<DeviceConnectionDbObj> connections = dataAccess.getConnectionsForCentral(device);
				// super fucking inefficient
				for (DeviceConnectionDbObj connection : connections) {
					dataAccess.addLog("disconnected after delay from central", LogConstants.TRACE);
					dataAccess.updateAsDisconnected(connection, true);
				}
			}
		}, 3000);
	}

	private void disconnectPeripheral(DeviceDbObj device, DeviceDataAccess dataAccess) {

		dataAccess.setTurnedOffFromInstaller(device, true);

		new java.util.Timer().schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				DeviceConnectionDbObj connection = dataAccess.getConnectionForPeripheral(device, true);
				dataAccess.addLog("disconnected after delay", LogConstants.TRACE);
				dataAccess.updateAsDisconnected(connection, false);
			}
		}, 3000);
	}

}
