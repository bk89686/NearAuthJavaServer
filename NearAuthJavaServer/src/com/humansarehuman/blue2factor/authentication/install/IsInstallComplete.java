package com.humansarehuman.blue2factor.authentication.install;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.IS_INSTALL_COMPLETE)
@SuppressWarnings("ucd")
public class IsInstallComplete extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		new DataAccess().addLog("method not allowed");
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processIsInstalledPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.addLog("start");
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "false"; // the token, in this case, shows whether the browser install has
								// been completed
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, "afrrea44", key, iv);
			String fcmId = this.getEncryptedRequestValue(request, "aenuhcas223th", key, iv);
			dataAccess.addLog("checking for device with ID: " + deviceId, LogConstants.IMPORTANT);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				if (device.isActive()) {
					List<DeviceConnectionDbObj> connections = dataAccess.getConnectionsForDevice(device, true);
					if (connections.size() > 0) {
						if (device.isBrowserInstallComplete() != null) {
							token = Boolean.toString(device.isBrowserInstallComplete());
						}
						if (isInstallComplete(connections, dataAccess)) {
							outcome = Outcomes.SUCCESS;
							reason = device.getBluetoothType().toString();
						} else {
							reason = "connection installation not complete";
						}
					} else {
						reason = "no connected devices";
					}
				} else {
					reason = "device isn't active";
				}
				if (fcmId != null || outcome == Outcomes.SUCCESS && device.getUnresponsive()) {
					device.setUnresponsive(false);
					if (fcmId != null && (device.getFcmId() == null || !device.getFcmId().equals(fcmId))) {
						dataAccess.setFcmAndUnresponsive(device, fcmId, false);
					} else {
						dataAccess.setUnresponsive(device, false);
					}
				}
			} else {
				dataAccess.addLog("is installed device not found for " + deviceId);
				reason = "device not found with Id: " + deviceId;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getMessage();
		}
		if (reason != null) {
			dataAccess.addLog("reason: " + reason);
		}
		BasicResponse response = new BasicResponse(outcome, reason, token);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	private boolean isInstallComplete(List<DeviceConnectionDbObj> conns, DeviceDataAccess dataAccess) {
		boolean installComplete = false;
		for (DeviceConnectionDbObj conn : conns) {
			if (conn.isInstallComplete()) {
				installComplete = true;
				break;
			}
		}
		return installComplete;
	}

//	@SuppressWarnings("unused")
//	private void resetBluetoothConnected(DeviceDbObj device) {
//		DeviceConnectionDataAccess dataAccess = new DeviceConnectionDataAccess();
//		try {
//			if (device.isCentral()) {
//				ArrayList<DeviceConnectionDbObj> connections = dataAccess.getConnectionsForCentral(device);
//				for (DeviceConnectionDbObj connection : connections) {
//					if (connection.getPeripheralConnected()) {
//						connection.setPeripheralConnected(false);
//						connection.setCentralConnected(false, "resetBluetoothConnected");
//						connection.setSubscribed(false);
//						dataAccess.updateConnection(connection, "resetBluetoothConnected");
//					}
//				}
//			}
//		} catch (Exception e) {
//			dataAccess.addLog(device.getDeviceId(), e);
//		}
//	}

}
