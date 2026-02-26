package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;

@Controller
@RequestMapping(Urls.BLUETOOTH_CONNECTED_BY_DEVICE_REFERENCE)
@SuppressWarnings("ucd")
public class BluetoothConnectedByDeviceReference extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String bluetoothConnectedByReferencePost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String centralOsId = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			boolean connected = this.getEncryptedRequestBoolean(request, Parameters.BLUETOOTH_CONNECTED_CONNECTED, key,
					iv, false);
			centralOsId = this.getEncryptedRequestValue(request, Parameters.BLUETOOTH_CONNECTED_CENTRAL_OS_ID, key, iv);
			if (!TextUtils.isBlank(centralOsId)) {
				DeviceConnectionDbObj connection = dataAccess.getConnectionByCentralReference(centralOsId);
				dataAccess.addLog("centralRef: " + centralOsId);
				if (connection != null) {
					if (connected && !dataAccess.hasPreviouslyConnected(connection)) {
						dataAccess.updateAsCentralConnected(connection);
					} else {
						dataAccess.addLog("connected: " + connected);
						dataAccess.updateCentralProximateConnection(connection, connected);
						dataAccess.addLog("connected: " + connected);
					}
					handleUnsuccessfulConnections(request, connection, connected, key, iv);
					outcome = Outcomes.SUCCESS;
				} else {
					reason = "connection not found";
				}
			} else {
				reason = "serviceUuid was null";
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getMessage();
		}
		dataAccess.addLog("outcome: " + outcome + "; reason: " + reason);
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

}
