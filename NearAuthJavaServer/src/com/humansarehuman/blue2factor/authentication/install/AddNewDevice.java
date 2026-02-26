package com.humansarehuman.blue2factor.authentication.install;

import java.sql.Timestamp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

@Controller
@RequestMapping(Urls.ADD_NEW_DEVICE)
@SuppressWarnings("ucd")
public class AddNewDevice extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String addNewDeviceProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;

		String reason = "";
		getVersion(request);
		String key = getKey(request);
		String iv = getInitVector(request);
		String centralId = this.getEncryptedRequestValue(request, "kbntunu7e", key, iv);
		String peripheralId = this.getEncryptedRequestValue(request, "cgc9dxjxoef", key, iv);
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.addLog(peripheralId, "entry");
		DeviceDbObj peripheralDevice = dataAccess.getDeviceByDeviceId(peripheralId);
		if (peripheralDevice != null) {
			DeviceDbObj centralDevice = dataAccess.getDeviceByDeviceId(centralId);
			if (centralDevice != null) {
				if (dataAccess.isAccessAllowed(centralDevice, "addNewDeviceProcessPost")) {
					dataAccess.setGroupIdUserIdAndActive(peripheralDevice, centralDevice.getGroupId(),
							centralDevice.getUserId(), true);
					DeviceConnectionDbObj conn = dataAccess.getConnectionForPeripheral(peripheralDevice, false);
					if (conn != null) {
						dataAccess.setCentralIdGroupIdActiveAndInstallComplete(centralDevice.getDeviceId(),
								centralDevice.getGroupId(), conn);
						dataAccess.addLog(peripheralId, "deviceAdded");
						outcome = Outcomes.SUCCESS;
					} else {
						dataAccess.addLog("connection not found");
						reason = Constants.CONNECTION_NOT_FOUND;
					}
				} else {
					if (giveItAFewSeconds(centralDevice, dataAccess)) {
						outcome = Outcomes.SUCCESS;
					} else {
						reason = Constants.DEVICE_NOT_CONNECTED;
					}
				}
			} else {
				reason = "central not found id: " + centralId;
			}
		} else {
			reason = "peripheral not found";
		}
		if (!reason.equals("")) {
			dataAccess.addLog(peripheralId, "failed with reason: " + reason, LogConstants.ERROR);
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	private boolean giveItAFewSeconds(DeviceDbObj centralDevice, DeviceDataAccess dataAccess) {
		Timestamp startTime = DateTimeUtilities.getCurrentTimestamp();
		boolean success = false;
		while (DateTimeUtilities.getCurrentTimestampMinusSeconds(3).before(startTime) && !success) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			success = dataAccess.isAccessAllowed(centralDevice, "addNewDeviceProcessPost");
		}
		return success;
	}
}
