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
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.AccessCodeDbObj;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.COMPLETE_RESYNC_ON_CENTRAL)
@SuppressWarnings("ucd")
public class CompleteResyncOnCentral extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String checkForResyncOnCentralProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String key = getKey(request);
		String iv = getInitVector(request);
		String deviceId = this.getEncryptedRequestValue(request, "nshcarhbjs", key, iv);
		String tokenId = this.getEncryptedRequestValue(request, "naheusab", key, iv);

		String accessCode = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("device: " + deviceId + ", token: " + tokenId);
		DeviceDbObj central = dataAccess.getDeviceByDeviceId(deviceId);
		TokenDbObj token = dataAccess.getToken(tokenId);
		Timestamp tenMinutesAgo = DateTimeUtilities.getCurrentTimestampMinusMinutes(10);
		if (token != null && token.getDescription().equals(TokenDescription.RESYNC.toString())
				&& token.getAuthorizationTime().after(tenMinutesAgo)) {
			dataAccess.addLog("token works");
			String peripheralDeviceId = token.getDeviceId();
			DeviceConnectionDbObj connection = dataAccess.getConnectionByDeviceIds(central.getDeviceId(),
					peripheralDeviceId);
			if (connection != null && connection.isActive()) {
				dataAccess.addLog("token and connection found");
				CompanyDbObj company = dataAccess.getCompanyByDevId(deviceId);
				if (company != null && company.isActive()) {
					dataAccess.addLog("EXPIRE_TIME UPDATE: ");
					dataAccess.expireResync(peripheralDeviceId);
					accessCode = GeneralUtilities.randomString();
					AccessCodeDbObj accessCodeObj = new AccessCodeDbObj(accessCode, company.getCompanyId(), "",
							central.getDeviceId(), 0, true, false);
					dataAccess.addAccessCode(accessCodeObj, "CompleteResyncOnCentral");
					reason = company.getApiKey();
					dataAccess.setupAddBrowser(central);
					outcome = Outcomes.SUCCESS;
				} else {
					reason = Constants.COMPANY_NOT_FOUND;
				}
			} else {
				reason = Constants.CONNECTION_NOT_FOUND;
			}
		} else {
			if (token == null) {
				reason = Constants.TOKEN_NOT_FOUND;
				dataAccess.addLog(reason);
			} else {
				reason = Constants.TOKEN_ERROR;
				dataAccess.addLog(reason);
			}
		}
		BasicResponse response = new BasicResponse(outcome, reason, accessCode);
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
