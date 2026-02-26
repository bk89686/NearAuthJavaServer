package com.humansarehuman.blue2factor.authentication.qrCodes;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.communication.PushNotifications;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.QR_VALIDATE)
@SuppressWarnings("ucd")
public class QrValidate extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String qrValidateProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String accessCode = this.getEncryptedRequestValue(request, "bc", key, iv);
			String qrString = this.getEncryptedRequestValue(request, "naiu98", key, iv);
			Boolean addDevice = this.getEncryptedRequestBoolean(request, "Uaeukk", key, iv, false);
			DeviceDbObj peripheralDevice = dataAccess.getDeviceByDeviceId(qrString);
			if (peripheralDevice != null) {
				if (validateQr(qrString, peripheralDevice, addDevice)) {
					dataAccess.addLog("qr validated");
					CompanyDbObj company = dataAccess.getCompanyByDevId(peripheralDevice.getDeviceId());
					if (company != null) {
						reason = company.getApiKey();
					}
					dataAccess.addAccessCode(peripheralDevice.getDeviceId(), accessCode, "QrValidate");
					dataAccess.setActive(peripheralDevice, true);
					outcome = Outcomes.SUCCESS;
				} else {
					if (peripheralDevice.getTemp() != null
							&& peripheralDevice.getTemp().equals(Constants.DIFFERENT_EMAILS)) {
						dataAccess.setTemp(peripheralDevice, "");
						reason = Constants.DIFFERENT_EMAILS;
					}
					dataAccess.addLog("QrValidate", "couldn't validate QrCode");
				}
			} else {
				dataAccess.addLog("QrValidate", "peripheralDevice not found with deviceId: " + qrString);
			}
		} catch (Exception e) {
			dataAccess.addLog("QrValidate", e);
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	private boolean validateQr(String qrString, DeviceDbObj peripheralDevice, Boolean addDevice) {
		boolean success = false;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		if (peripheralDevice != null) {
			List<DeviceConnectionDbObj> connections = dataAccess.getConnectionsForDevice(peripheralDevice, false);
			dataAccess.addLog(connections.size() + " connections found.");
			for (DeviceConnectionDbObj connection : connections) {
				if (connection.isActive()) {
					dataAccess.addLog("active connection found.");
					if (peripheralDevice.isMultiUser()) {
						dataAccess.addLog("multiuser is true");
						PushNotifications push = new PushNotifications();
						push.sendLoudPushByDeviceOnce(peripheralDevice, false);
					} else {
						dataAccess.addLog("multiuser is false");
					}
					success = true;
					break;
				} else {
					dataAccess.addLog(peripheralDevice.getDeviceId(), "inactive device found");
				}
			}
		} else {
			dataAccess.addLog("validateQr", "peripheralDevice was null");
		}
		return success;
	}
}
