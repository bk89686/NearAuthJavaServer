package com.humansarehuman.blue2factor.authentication.qrCodes;

import java.sql.Timestamp;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

//I don't think we use this -- cjm
@Controller
@RequestMapping(Urls.QR_REVALIDATE)
@SuppressWarnings("ucd")
public class QrRevalidate extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String qrRevalidateProcessGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = Constants.DEVICE_NOT_FOUND;
		String deviceId = this.getRequestValue(request, "id");
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
		if (device != null && device.isActive()) {
			DeviceDbObj central = dataAccess.getConnectedCentral(device);
			if (central != null) {
				List<TokenDbObj> tokens = dataAccess.getActiveTokensByDevice(central);
				if (tokens.size() > 0) {
					outcome = Outcomes.SUCCESS;
					reason = "revalidated";
				} else {
					reason = "resynch not needed";
				}
			} else {
				reason = "central not found";
			}
		} else {
			reason = "device not found";
		}
		dataAccess.addLog(deviceId,
				"get - outcome: " + outcome + ": reason: " + reason + ", for central of device: " + deviceId);
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String qrRevalidateProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = Constants.DEVICE_NOT_FOUND;
		String key = getKey(request);
		String iv = getInitVector(request);
		String deviceId = this.getEncryptedRequestValue(request, "id", key, iv);
		String tokenId = this.getEncryptedRequestValue(request, "token", key, iv);

		String accessCode = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("device: " + deviceId + ", token: " + tokenId);
		DeviceDbObj central = dataAccess.getDeviceByDeviceId(deviceId);
		TokenDbObj token = dataAccess.getToken(tokenId);
		Timestamp tenMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(10);
		if (token != null && token.getDescription().equals(TokenDescription.RESYNC.toString())
				&& token.getAuthorizationTime().after(tenMinutesAgo)) {
			String peripheralDeviceId = token.getDeviceId();
			CompanyDbObj company = dataAccess.getCompanyByDevId(peripheralDeviceId);
			DeviceConnectionDbObj connection = dataAccess.getConnectionByDeviceIds(central.getDeviceId(),
					peripheralDeviceId);
			if (connection != null && connection.isActive() && company != null) {
				accessCode = GeneralUtilities.randomString();
				AccessCodeDbObj accessCodeObj = new AccessCodeDbObj(accessCode, company.getCompanyId(), "",
						central.getDeviceId(), 0, true, false);
				dataAccess.addAccessCode(accessCodeObj, "QrRevalidate");
				outcome = Outcomes.SUCCESS;
			}
		} else {
			if (token == null) {
				reason = "token was null for val: " + tokenId;
				dataAccess.addLog(reason);
			} else {
				reason = "either the token was the wrong type or too old";
				dataAccess.addLog(reason);
			}
		}
		BasicResponse response = new BasicResponse(outcome, reason, accessCode);
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
