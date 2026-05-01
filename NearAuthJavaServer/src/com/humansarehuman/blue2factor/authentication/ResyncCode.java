package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.Timestamp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.RESYNC_CODE)
@SuppressWarnings("ucd")
public class ResyncCode extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String resyncCoderocessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		int outcome = Outcomes.FAILURE;
		String tokenId = "";
		String reason = "";
		try {
			String key = getKey(request);
			String iv = getInitVector(request);
			String deviceId = this.getEncryptedRequestValue(request, "nshcarhbjs", key, iv);
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
			if (device != null) {
				DeviceDbObj central = dataAccess.getConnectedCentral(device);
				dataAccess.addLog("connected central: " + central.getDeviceType());
				if (central != null) {
					CompanyDbObj company = dataAccess.getCompanyByDevId(central.getDeviceId());
					if (company != null && company.isActive()) {
						tokenId = GeneralUtilities.randomString();
						Timestamp expire = DateTimeUtilities.getCurrentTimestampMinusMinutes(5);
						dataAccess.addTokenWithId(device, "", tokenId, TokenDescription.RESYNC, 0, "", expire);
						Encryption encryption = new Encryption();
						tokenId = encryption.encryptStringWithPublicKey(central, tokenId);
						outcome = Outcomes.SUCCESS;
					}
				}
			}
		} catch (Exception e) {
			dataAccess.addLog("ResyncCode", e);
		}
		BasicResponse response = new BasicResponse(outcome, reason, tokenId);
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
