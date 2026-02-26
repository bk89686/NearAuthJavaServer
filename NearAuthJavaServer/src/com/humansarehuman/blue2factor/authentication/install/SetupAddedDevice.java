package com.humansarehuman.blue2factor.authentication.install;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.SETUP_ADDED_DEVICE)
@SuppressWarnings("ucd")
public class SetupAddedDevice extends BaseController {

	@RequestMapping(method = RequestMethod.POST)
	public String setupAddedDeviceProcessPost(HttpServletRequest request, ModelMap model) {
		BasicResponse basicResponse = new BasicResponse(Outcomes.FAILURE, "", "");
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String peripheralId = this.getEncryptedRequestValue(request, "etshuasnh", key, iv);
			String centralId = this.getEncryptedRequestValue(request, "ebasnutbe", key, iv);
			DeviceDbObj peripheralDevice = dataAccess.getDeviceByDeviceId(peripheralId);
			if (peripheralDevice != null) {
				dataAccess.addLog(peripheralId, "peripheral found by Id");
				DeviceDbObj centralDevice = dataAccess.getDeviceByDeviceId(centralId);
				if (centralDevice != null) {
					dataAccess.addLog(peripheralId, "central found by Id");
					basicResponse = setupAddedDevice(centralDevice, peripheralDevice);
				}
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		model = this.addBasicResponse(model, basicResponse);
		return "result";
	}

	public BasicResponse setupAddedDevice(DeviceDbObj centralDevice, DeviceDbObj peripheralDevice) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String accessStr = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		if (dataAccess.isLicenseAvailableForGroup(centralDevice.getGroupId())) {
			dataAccess.addLog(peripheralDevice.getDeviceId(), "license is available");
			setupPeripheralDevice(peripheralDevice, centralDevice.getGroupId());
			setupConnection(centralDevice, peripheralDevice);
			accessStr = GeneralUtilities.randomString();
			CompanyDbObj company = dataAccess.getCompanyByDevId(centralDevice.getDeviceId());
			if (company != null) {
				AccessCodeDbObj accessCode = new AccessCodeDbObj(DateTimeUtilities.getCurrentTimestamp(), accessStr,
						company.getCompanyId(), "", centralDevice.getDeviceId(), 0, true, false);
				dataAccess.addAccessCode(accessCode, "setupAddedDevice");
				dataAccess.setLastReset(centralDevice, DateTimeUtilities.getCurrentTimestamp());
				outcome = Outcomes.SUCCESS;
			} else {
				reason = Constants.COMPANY_NOT_FOUND;
				dataAccess.addLog(peripheralDevice.getDeviceId(), "company not found", LogConstants.WARNING);
			}
		} else {
			dataAccess.addLog(peripheralDevice.getDeviceId(), "license NOT available", LogConstants.WARNING);
			reason = "The were no more licenses available for your company.";
		}
		return new BasicResponse(outcome, reason, accessStr);
	}
}
