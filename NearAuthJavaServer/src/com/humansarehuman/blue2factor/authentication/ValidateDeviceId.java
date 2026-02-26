package com.humansarehuman.blue2factor.authentication;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

@Controller
@RequestMapping(Urls.VALIDATE_DEVICE_ID)
@SuppressWarnings("ucd")
public class ValidateDeviceId extends BaseController {
	/**
	 * also makes sure there is at least one connected device
	 * 
	 * @param request
	 * @param model
	 * @return
	 */

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processValidateDeviceIdPost(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		String deviceId = "";
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			deviceId = this.getEncryptedRequestValue(request, "naiu98", key, iv);
			// deviceId = this.getEncryptedRequestValue(request, "shsssoq", key, iv);
			dataAccess.addLog("deviceId: " + deviceId);
			boolean resync = getActive(request, key, iv);
			dataAccess.addLog("resync found: " + resync);
			if (resync) {
				outcome = checkForResync(deviceId);
				dataAccess.addLog(deviceId, "outcome for resync: " + outcome, LogConstants.INFO);
			} else {
				dataAccess.addLog(deviceId, "not yet active");
				DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId, "processValidateDeviceIdPost");
				if (device != null) {
					dataAccess.addLog(deviceId, "device found");
					if (isAddingDevice(device)) {
						dataAccess.addLog(deviceId, "adding device");
						CompanyDbObj company = dataAccess.getCompanyByDevId(deviceId);
						if (company != null) {
							outcome = Outcomes.SUCCESS;
							reason = company.getCompanyBaseUrl() + "::1";
						} else {
							reason = Constants.COMPANY_NOT_FOUND;
						}
					} else {
						dataAccess.addLog(deviceId, "new device");
						CompanyDbObj company = dataAccess.getCompanyByDevId(deviceId);
						if (company != null) {
							outcome = Outcomes.SUCCESS;
							reason = company.getCompanyBaseUrl() + "::0";
						} else {
							reason = Constants.COMPANY_NOT_FOUND;
						}
					}
					dataAccess.addLog(deviceId, reason, LogConstants.TRACE);
				} else {
					reason = "As a security feature, your phone must be connected to one device in order to add another.";
					dataAccess.addLog(deviceId, reason, LogConstants.WARNING);
				}
				dataAccess.addLog(deviceId, "outcome for setup: " + outcome, LogConstants.INFO);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	private boolean getActive(HttpServletRequest request, String key, String iv) {
		boolean active = false;
		String sActive = this.getEncryptedRequestValue(request, "shauntea", key, iv);
		DataAccess dataAccess = new DataAccess();
		if (sActive != null) {
			dataAccess.addLog("active found");
			active = sActive.equalsIgnoreCase("true");
		} else {
			dataAccess.addLog("active was null");
		}
		return active;
	}

	private boolean isAddingDevice(DeviceDbObj device) {
		boolean success = false;
		if (!device.isActive()) {
			DeviceDataAccess dataAccess = new DeviceDataAccess();
			ArrayList<DeviceConnectionDbObj> conns = dataAccess.getConnectionsForDevice(device, true);
			success = conns.size() > 0;
			dataAccess.addLog(device.getDeviceId(), "activeCount: " + conns.size());
		}
		return success;

	}

	private int checkForResync(String deviceId) {
		DeviceDataAccess dataAccess = new DeviceDataAccess();

		int outcome = Outcomes.FAILURE;

		DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
		if (device != null) {
			DeviceDbObj central = dataAccess.getConnectedCentral(device);
			if (central != null) {
				if (dataAccess.getActiveTokensByDevice(central).size() > 0) {
					outcome = Outcomes.SUCCESS;
				}
			}
		}
		dataAccess.addLog(deviceId, "checkForResync - " + outcome);
		return outcome;
	}
}
