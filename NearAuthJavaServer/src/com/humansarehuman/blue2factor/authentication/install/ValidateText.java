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
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.VALIDATE_TEXT)
@SuppressWarnings("ucd")
public class ValidateText extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String validateTextProcessGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String validateTextProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String devId = this.getEncryptedRequestValue(request, Parameters.VALIDATE_TEXT_DEVICE_ID, key, iv);
			String textCode = this.getEncryptedRequestValue(request, Parameters.VALIDATE_TEXT_CODE, key, iv);
			Boolean addDevice = this.getEncryptedRequestBoolean(request, Parameters.VALIDATE_TEXT_ADD_DEVICE, key, iv,
					false);
			DeviceDbObj peripheralDevice = dataAccess.getDeviceByDeviceId(devId, "validateTextProcessPost");
			if (peripheralDevice != null) {
				CheckDbObj check = dataAccess.getCheckByPeripheralAndCode(devId, textCode);
				if (check != null) {
					dataAccess.setActive(peripheralDevice, true);
					DeviceConnectionDbObj conn = dataAccess.getConnectionForPeripheral(peripheralDevice, false);
					DeviceDbObj centralDevice = null;
					if (conn != null) {
						centralDevice = dataAccess.getCentralDeviceByConnection(conn);
					} else if (conn == null && addDevice) {
						centralDevice = dataAccess.getCentralByGroupId(peripheralDevice.getGroupId());
						Timestamp now = DateTimeUtilities.getCurrentTimestamp();
						String serviceUuid = GeneralUtilities.getRandomUuid();
						String charUuid = GeneralUtilities.getRandomUuid();
						conn = new DeviceConnectionDbObj(now, GeneralUtilities.randomString(),
								peripheralDevice.getDeviceId(), centralDevice.getDeviceId(), serviceUuid, charUuid,
								peripheralDevice.getGroupId(), true, now, now, true, now, false, now, null, null, false,
								null, true, centralDevice.getOperatingSystem().toString(), null, null, null, null,
								false, "");
						dataAccess.addConnection(conn);
					}

					if (conn != null) {
						dataAccess.updateAsInstallCompleteAndActive(conn);
						if (centralDevice != null) {
							CompanyDbObj company = dataAccess.getCompanyByDevId(devId);
							if (company != null && company.isActive()) {
								dataAccess.setActive(centralDevice, true);
								check.setCompleted(true);
								check.setOutcome(Outcomes.SUCCESS);
								Timestamp now = DateTimeUtilities.getCurrentTimestamp();
								check.setCompletionDate(now);
								int seconds = company.getTextTimeoutSeconds();
								check.setExpirationDate(DateTimeUtilities.getCurrentTimestampPlusSeconds(seconds));
								dataAccess.updateCheck(check);
								dataAccess.addConnectionLogIfNeeded(check, true, check.getPeripheralDeviceId(),
										"validateTextProcessPost", ConnectionType.PROX);
								reason = company.getApiKey();
								outcome = Outcomes.SUCCESS;
							}
						} else {
							reason = Constants.CENTRAL_NOT_FOUND;
						}
					} else {
						reason = Constants.CONNECTION_NOT_FOUND;
					}
				} else {
					reason = Constants.CHECK_NOT_FOUND;
				}
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
