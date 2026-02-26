package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
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
@RequestMapping(Urls.RSSI_UPDATE)
@SuppressWarnings("ucd")
public class RssiUpdate extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String rssiUpdatePost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, Parameters.RSSI_DEVICE_ID, key, iv);
			String serviceUuid = this.getEncryptedRequestValue(request, Parameters.RSSI_SERVICE_UUID, key, iv);
			Integer txPower = null;
			Integer rssi = null;
			try {
				txPower = Integer.parseInt(this.getEncryptedRequestValue(request, Parameters.TX_POWER, key, iv));
			} catch (Exception eInt) {
				dataAccess.addLog("txPower not passed", LogConstants.WARNING);
			}
			try {
				rssi = Integer.parseInt(this.getEncryptedRequestValue(request, Parameters.RSSI, key, iv));
			} catch (Exception eInt2) {
				dataAccess.addLog("rssi not passed", LogConstants.WARNING);
			}
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			DeviceConnectionDbObj connection = null;
			if (device != null) {
				if (!device.isCentral()) {
					connection = dataAccess.getConnectionForPeripheral(device, true);
				} else {
					if (!TextUtils.isBlank(serviceUuid)) {
						connection = dataAccess.getConnectionByServiceUuid(serviceUuid);
					}
				}
				if (device != null && connection != null) {
					if (txPower != null && device.getTxPower() != txPower) {
						DeviceDbObj connectedDevice;
						if (device.isCentral()) {
							// this is counterintuitive but the central reads the txPower of the
							// peripheral. Also, I'm not even sure the peripheral ever can read the
							// value of the central, but it's in here too just in case. -- cjm
							connectedDevice = dataAccess.getPeripheralDeviceByConnection(connection);
						} else {
							connectedDevice = dataAccess.getCentralDeviceByConnection(connection);
						}
						dataAccess.setTxPower(connectedDevice, txPower);
					}
					if (dataAccess.updateRssi(connection, rssi, device.isCentral())) {
						outcome = Outcomes.SUCCESS;
					}
				} else {
					dataAccess.addLog("either device or connection was null, serviceUuid: " + serviceUuid
							+ ", deviceId: " + deviceId, LogConstants.WARNING);
				}
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
				dataAccess.addLog("device not found for deviceId: " + deviceId, LogConstants.ERROR);
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
