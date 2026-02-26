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
@RequestMapping(Urls.VALIDATE_DEV_ID)
@SuppressWarnings("ucd")
public class ValidateDevId extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processPostValidateDevId(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		int outcome = Outcomes.FAILURE;
		boolean central = false;
		String reason = "";
		try {

			String key = getKey(request);
			String iv = getInitVector(request);
			String machineId = this.getEncryptedRequestValue(request, Parameters.MACHINE_ID, key, iv);
			String deviceId = this.getEncryptedRequestValue(request, Parameters.DEV_ID_VALIDATE_DEV_ID, key, iv);
			dataAccess.addLog("machineId: " + machineId + "; devId: " + deviceId, LogConstants.TRACE);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			boolean previouslyConnected = false;
			boolean multiuser = false;
			String serviceUuid = "";
			String characteristicUuid = "";
			OsClass osClass = OsClass.UNKNOWN;
			if (device == null) {
				device = dataAccess.getActiveDeviceByMachineId(machineId);
			}
			if (device != null) {
				if (device.isActive()) {
					if (!device.isCentral()) {
						DeviceConnectionDbObj connection = dataAccess.getConnectionForPeripheral(device, true);
						if (connection != null) {
							previouslyConnected = dataAccess.wasPreviouslyConnected(connection);
							serviceUuid = connection.getServiceUuid();
							characteristicUuid = connection.getCharacteristicUuid();
							osClass = dataAccess.getCentralType(connection);
							dataAccess.addLog(device.getDeviceId(), "device found", LogConstants.INFO);
							if (connection.getPeripheralConnected()) {
								dataAccess.updateAsDisconnected(connection, false);
//								connection.setPeripheralConnected(false);
//								connection.setCentralConnected(false, "processPostValidateDevId");
//								connection.setSubscribed(false);
//								dataAccess.updateConnection(connection, "processPostValidateDevId");
							}
						}
						multiuser = device.isMultiUser();
						outcome = Outcomes.SUCCESS;
					}
				} else {
					reason = Constants.DEVICE_INACTIVE;
				}
				central = device.isCentral();
			} else {
				reason = Constants.DEV_NOT_FOUND;
			}
			BasicDeviceParameters peripheralParameters = new BasicDeviceParameters(deviceId, serviceUuid, central,
					osClass, characteristicUuid, previouslyConnected, multiuser);
			BasicResponse response = new BasicResponse(outcome, reason);
			model = this.addDeviceResponse(model, response, peripheralParameters);
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
		}
		return "deviceResult";
	}
}
