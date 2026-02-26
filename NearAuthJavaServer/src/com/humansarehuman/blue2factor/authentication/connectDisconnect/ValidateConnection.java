package com.humansarehuman.blue2factor.authentication.connectDisconnect;

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
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.VALIDATE_CONNECTION)
@SuppressWarnings("ucd")
public class ValidateConnection extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String validateConnectionProcessPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "not validated";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String peripheralDevId = this.getEncryptedRequestValue(request, "asjoill3l", key, iv);
			String centralDevId = this.getEncryptedRequestValue(request, "oae99aj0p", key, iv);
			DeviceDbObj peripheral = dataAccess.getDeviceByDeviceId(peripheralDevId);
			if (peripheral != null) {
				DeviceDbObj central = dataAccess.getDeviceByDeviceIdAandUser(centralDevId, peripheral.getUserId());
				if (central != null) {
					DeviceConnectionDbObj conn = dataAccess.getConnectionForPeripheral(peripheralDevId, false);
					if (conn != null) {
						if (dataAccess.setActive(central, peripheral, true)) {
							outcome = Outcomes.SUCCESS;
						}
						reason = "";
					}
				} else {
					reason = "central was null";
				}
			} else {
				reason = "peripheral was null";
			}
			dataAccess.addLog(centralDevId, "outcome: " + outcome + "; reason: " + reason, LogConstants.DEBUG);
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);

		return "result";
	}

}
