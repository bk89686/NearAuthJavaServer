package com.humansarehuman.blue2factor.authentication;

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
import com.humansarehuman.blue2factor.entities.BasicDeviceParameters;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.LOOKUP_DEV_ID_BY_MACHINE_ID)
@SuppressWarnings("ucd")
public class LookupDevIdByMachineId extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String lookupDeviceIdProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			int outcome = Outcomes.FAILURE;
			String reason = "";
			boolean isCentral = false;
			String key = getKey(request);
			String iv = getInitVector(request);
			String machineId = this.getEncryptedRequestValue(request, Parameters.MACHINE_ID, key, iv);
			String deviceId = this.getEncryptedRequestValue(request, Parameters.DEV_ID_LOOKUP, key, iv);
			dataAccess.addLog("machineId: " + machineId, LogConstants.TRACE);
			String serviceUuid = "";
			String characteristicUuid = "";
			boolean multiuser = false;
			OsClass osClass = OsClass.UNKNOWN;

			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId, true, "lookupDeviceIdProcessPost");
			boolean previouslyConnected = false;
			if (device == null) {
				device = dataAccess.getActiveDeviceByMachineId(machineId);
			} else {
				// it's possible we may not want to update the machine id here.
				// I did it so it will match in the ssh install
				// We may also want to check the FCM token here as both an update
				// and a double check
				if (device.getLoginToken().equals(machineId)) {
					dataAccess.setLoginToken(device, machineId);
				}
			}
			if (device != null) {
				outcome = Outcomes.SUCCESS;
				deviceId = device.getDeviceId();
				if (!device.isCentral()) {
					DeviceConnectionDbObj connection = dataAccess.getConnectionForPeripheral(device, true);
					if (connection != null) {
						previouslyConnected = dataAccess.wasPreviouslyConnected(connection);
						serviceUuid = connection.getServiceUuid();
						characteristicUuid = connection.getCharacteristicUuid();
						osClass = dataAccess.getCentralType(connection);
						dataAccess.addLog(device.getDeviceId(), "device found", LogConstants.TRACE);
					}
					multiuser = device.isMultiUser();
				}
				isCentral = device.isCentral();
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
				dataAccess.addLog("device lookup failed with token " + machineId + " (might not be active)",
						LogConstants.TRACE);
			}

			BasicDeviceParameters peripheralParameters = new BasicDeviceParameters(deviceId, serviceUuid, isCentral,
					osClass, characteristicUuid, previouslyConnected, multiuser);
			BasicResponse response = new BasicResponse(outcome, reason, Boolean.toString(isCentral));
			model = this.addDeviceResponse(model, response, peripheralParameters);
			dataAccess.addLog(deviceId, "outcome: " + outcome + "; reason: " + reason,
					LogConstants.TRACE);

		} catch (Exception e) {
			dataAccess.addLog("LookupDevIdByMachineId", e);
		}
		return "deviceResult";
	}
}
