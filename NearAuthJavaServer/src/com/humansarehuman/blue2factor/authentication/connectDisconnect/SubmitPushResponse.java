package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
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
import com.humansarehuman.blue2factor.entities.enums.CheckType;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.ConnectionLogDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.SUBMIT_PUSH_RESPONSE)
@SuppressWarnings("ucd")
public class SubmitPushResponse extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String pushResponseProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "");
		String uuid = "";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.addLog("start");
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String bssId = GeneralUtilities.getClientIp(request);
			uuid = this.getEncryptedRequestValue(request, "Zthsnhths", key, iv);
			String instanceId = this.getEncryptedRequestValue(request, "ntsihcrhieua", key, iv);
			dataAccess.addLog("push response received for uuid: " + uuid, LogConstants.IMPORTANT);
			CheckDbObj check = dataAccess.getCheckByCentralInstance(instanceId);
			if (check != null && (check.getCheckType().toString().equals(CheckType.PUSH.toString()))
					|| check.getCheckType().toString().equals(CheckType.SSHPUSH.toString())) {
				DeviceDbObj device;
				if (!TextUtils.isBlank(check.getCentralDeviceId())) {
					device = dataAccess.getDeviceByDeviceId(check.getCentralDeviceId());
				} else {
					device = dataAccess.getDeviceByDeviceId(check.getPeripheralDeviceId());
				}
				ConnectionLogDbObj connLog = new ConnectionLogDbObj(null, device.getDeviceId(), true,
						DateTimeUtilities.getCurrentTimestamp(), check.getCentralDeviceId(), "pushResponseProcessPost",
						ConnectionType.PUSH);
				dataAccess.setPushFailed(device, false);
				dataAccess.addConnectionLog(connLog);
				if (check.getServiceUuid() != null && check.getServiceUuid().equals(uuid)) {
					updateCheckAsSuccessful(check, bssId, ConnectionType.PUSH);
					response.setOutcome(Outcomes.SUCCESS);
				} else {
					dataAccess.addLog("check uuids were null");
					if (checkIsTemp(check, dataAccess)) {
						updateCheckAsSuccessful(check, bssId, ConnectionType.PUSH);
						response.setOutcome(Outcomes.SUCCESS);
					}
				}
			} else {
				dataAccess.addLog("check was null for instanceId: " + instanceId + " with uuid: " + uuid,
						LogConstants.WARNING);
			}
		} catch (Exception e) {
			response.setReason(e.getMessage());
		}
		model = this.addBasicResponse(model, response);
		return "result";
	}

	private boolean checkIsTemp(CheckDbObj check, DeviceDataAccess dataAccess) {
		boolean isTemp = false;
		DeviceDbObj device = dataAccess.getDeviceByDeviceId(check.getPeripheralDeviceId());
		if (device != null) {
			if (device.getDeviceClass().toString().equals(DeviceClass.TEMP.toString())) {
				isTemp = true;
			}
		}
		dataAccess.addLog("checkIsTemp", "isTemp: " + isTemp);
		return isTemp;
	}
}
