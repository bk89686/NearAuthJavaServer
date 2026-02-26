package com.humansarehuman.blue2factor.authentication.api;

import java.util.ArrayList;

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
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.ConnectionAttrs;
import com.humansarehuman.blue2factor.entities.enums.CheckType;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.GET_UNFOUND_PERIPHERAL_FROM_SERVICE)
@SuppressWarnings("ucd")
public class GetUnfoundPeripheralFromService extends B2fApi {
	@RequestMapping(method = RequestMethod.GET)
	public String processGetUnfoundPeripheralFromService(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String getUnfoundPeripheralFromServiceProcessPost(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String instanceId = GeneralUtilities.randomString(20);
		String deviceId = "";
		boolean connected = false;
		boolean showIcon = false;
		boolean connecting = false;
		boolean subscribed = false;
		boolean hasBle = false;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("start");
		ArrayList<ConnectionAttrs> connDevices = new ArrayList<ConnectionAttrs>();
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			deviceId = this.getEncryptedRequestValue(request, Parameters.DEVICE_ID_GET_UNFOUND, key, iv);
			String serviceUuid = this.getEncryptedRequestValue(request, Parameters.SERVICE_UUID_GET_UNFOUND, key, iv);
			String ipAddress = GeneralUtilities.getClientIp(request);
			dataAccess.addLog("deviceId=" + deviceId);
			DeviceDbObj callingDevice = dataAccess.getDeviceByDeviceId(deviceId,
					"getUnfoundPeripheralFromServiceProcessPost");
			if (callingDevice != null) {
				hasBle = callingDevice.getHasBle();
				DeviceConnectionDbObj connection = dataAccess.getConnectionByServiceUuid(serviceUuid);
				if (connection != null && callingDevice != null) {
					DeviceDbObj peripheral = dataAccess.getDeviceByDeviceId(connection.getPeripheralDeviceId(),
							"getUnfoundPeripheralFromServiceProcessPost");
					if (connection.getCentralDeviceId().equals(deviceId)) {
						ConnectionAttrs connAttrs = addNewCheckFromCentral(connection, callingDevice, peripheral,
								instanceId, ipAddress, "", 0, CheckType.PROX);
						connDevices.add(connAttrs);
						dataAccess.updateLastVariableRetrieval(callingDevice, callingDevice.isScreensaverOn());
						connected = dataAccess.isAccessAllowed(connection, callingDevice);
						subscribed = connection.isSubscribed();
						connecting = !connecting && dataAccess.getConnecting(connection);
						showIcon = callingDevice.getShowIcon();
					}
				}
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
		}
		if (connDevices.size() < 1) {
			outcome = Outcomes.FAILURE;
		}
		dataAccess.addLog(deviceId, "devices: " + connDevices.size());
		// connectedToAll is false because we didn't check
		model = this.addReturnVars(model, outcome, reason, connDevices, instanceId, connected, subscribed, false,
				showIcon, false, hasBle, connecting);
		return "varDevices";
	}

}
