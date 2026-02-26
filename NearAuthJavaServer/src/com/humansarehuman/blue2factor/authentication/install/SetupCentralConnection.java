package com.humansarehuman.blue2factor.authentication.install;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.http.util.TextUtils;
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
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.BasicResponsePlusExtraBoolean;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.SETUP_CENTRAL_CONNECTION)
@SuppressWarnings("ucd")
public class SetupCentralConnection extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processPostSetupCentral(HttpServletRequest request, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String accessStr = "";
		boolean perfHasBle = true;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String peripheralId = this.getEncryptedRequestValue(request, "q4fnkk9se", key, iv);
			String centralId = this.getEncryptedRequestValue(request, "8hqivblHlI", key, iv);
			DeviceDbObj peripheralDevice = dataAccess.getDeviceByDeviceId(peripheralId, "processPostSetupCentral");
			if (peripheralDevice != null) {
				perfHasBle = peripheralDevice.getHasBle();
				dataAccess.addLog(peripheralId, "perf found by Id");
				DeviceDbObj centralDevice = dataAccess.getDeviceByDeviceId(centralId);
				if (centralDevice == null) {
					dataAccess.addLog(peripheralId, "device not found - this is not good", LogConstants.WARNING);
					reason = "central device doesn't exist";
				} else {
					if (centralDevice.getGroupId().equals(peripheralDevice.getGroupId())) {
						if (centralDevice.isActive() && peripheralDevice.isActive()) {
							dataAccess.addLog(peripheralId, "active device found - should be a resync",
									LogConstants.WARNING);

						} else {
							if (!centralDevice.isActive()) {
								dataAccess.addLog(peripheralId, "inactive device found - this is good news",
										LogConstants.DEBUG);
								centralDevice = setupCentralDevice(request, centralDevice, peripheralDevice, key, iv);
							}
							if (dataAccess.isLicenseAvailableForGroup(centralDevice.getGroupId())) {
								dataAccess.addLog(peripheralId, "license is available");
								setupPeripheralDevice(peripheralDevice, centralDevice.getGroupId());
								setupConnection(centralDevice, peripheralDevice);
								accessStr = GeneralUtilities.randomString();
								CompanyDbObj company = dataAccess.getCompanyByDevId(centralId);
								if (company != null) {
									AccessCodeDbObj accessCode = new AccessCodeDbObj(
											DateTimeUtilities.getCurrentTimestamp(), accessStr, company.getCompanyId(),
											"", centralId, 0, true, false);
									dataAccess.addAccessCode(accessCode, "processPostSetupCentral");

									reason = company.getApiKey();
									dataAccess.setActiveAndCentralLastReset(centralDevice, peripheralDevice, true,
											DateTimeUtilities.getCurrentTimestamp());
									outcome = Outcomes.SUCCESS;
								}
							} else {
								dataAccess.addLog(peripheralId, "license NOT available", LogConstants.WARNING);
								reason = "The were no more licenses available for your company.";
							}
						}
					} else {
						peripheralDevice.setTemp(Constants.DIFFERENT_EMAILS);
						dataAccess.setTemp(peripheralDevice, "SetupCentralConnection");
						dataAccess.addLog("Different emails were entered on your device", LogConstants.WARNING);
						reason = Constants.DIFFERENT_EMAILS;
					}
				}
			} else {
				dataAccess.addLog(peripheralId, "peripheral device doesn't exist", LogConstants.WARNING);
				reason = "peripheral device doesn't exist";
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getMessage();
		}
		dataAccess.addLog("accessCode: " + accessStr);
		BasicResponsePlusExtraBoolean response = new BasicResponsePlusExtraBoolean(outcome, reason, accessStr,
				perfHasBle);
		model = this.addBasicResponsePlusExtraBoolean(model, response);
		return "resultWithExtraBoolean";
	}

	private DeviceDbObj setupCentralDevice(HttpServletRequest request, DeviceDbObj centralDevice,
			DeviceDbObj peripheralDevice, String key, String iv) {
		String b2fId = this.getEncryptedRequestValue(request, "UUkhj9kat3", key, iv);
		String deviceType = this.getEncryptedRequestValue(request, "deviceType", key, iv);
		OsClass os = OsClass.valueOf(this.getEncryptedRequestValue(request, "os", key, iv).toUpperCase());
		String osv = this.getEncryptedRequestValue(request, "osv", key, iv);
		Integer gmtOffset = this.getEncryptedGmtOffset(request, "jkajiogfh", key, iv);
		String language = this.getEncryptedRequestValue(request, "jioa88IIIKs", key, iv);
		String btAddress = this.getEncryptedRequestValue(request, "bta", key, iv); // null for iOs
		String screenSize = this.getEncryptedRequestValue(request, "SDARTda", key, iv);
		String publicKey = this.getEncryptedRequestValue(request, "eausnht", key, iv);
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.addLog("publicKey: " + publicKey);

		centralDevice.setUserId(peripheralDevice.getUserId());
		int seed = GeneralUtilities.randInt(0, 1000000000);
		centralDevice.setSeed(seed);
		centralDevice.setActive(true);
		centralDevice.setBtAddress(btAddress);
		centralDevice.setDeviceType(deviceType);
		centralDevice.setOperatingSystem(os);
		centralDevice.setOsVersion(osv);
		centralDevice.setLastGmtOffset(gmtOffset);
		centralDevice.setUserLanguage(language);
		centralDevice.setScreenSize(screenSize);
		centralDevice.setCentral(true);
		centralDevice.setDeviceClass(DeviceClass.PHONE);
		String rand = "";// GeneralUtilities.randomString(4095);
		centralDevice.setRand(rand);
		Double priority = this.getInitialDevicePriority(os);
		centralDevice.setDevicePriority(priority);

		dataAccess.updateDevice(centralDevice, "setupCentralDevice");
		dataAccess.addLog("b2fId = " + b2fId);
		addDevicePublicKey(centralDevice, centralDevice.getGroupId(), publicKey, "");
		if (TextUtils.isBlank(peripheralDevice.getGroupId())) {
			dataAccess.setGroupId(peripheralDevice, centralDevice.getGroupId());
		}
		return centralDevice;
	}

}
