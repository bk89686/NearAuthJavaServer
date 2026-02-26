package com.humansarehuman.blue2factor.authentication.api;

import java.sql.Timestamp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponse;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;

@Controller
@RequestMapping(Urls.SSH_PRECHECK_CONFIRM)
@SuppressWarnings("ucd")
public class SshPrepConfirm extends BaseController {

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse sshPrecheckProcessPost(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String encrypted = this.getRequestValue(request, "tkn1").replace("+", "-").replace("/", "_");
		;
		String deviceId = this.getRequestValue(request, "tkn2");
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("encrypted:" + encrypted);
		dataAccess.addLog("devId:" + deviceId);
		if (!TextUtils.isBlank(encrypted) && !TextUtils.isBlank(deviceId)) {
			CompanyDbObj company = dataAccess.getCompanyByDevId(deviceId);
			KeyDbObj coPvtKey = dataAccess.getCompanyServerPrivateSshKey(company.getCompanyId());
			String token = new Encryption().decryptUrlSafeWithPrivateKey(coPvtKey, encrypted);
			TokenDbObj tokenObj = dataAccess.getTokenByDescriptionAndTokenId(TokenDescription.SSH, token);
			if (tokenObj != null) {
				if (tokenObj.getExpireTime().compareTo(DateTimeUtilities.getBaseTimestamp()) == 0) {
					Timestamp oneMinute = DateTimeUtilities.getCurrentTimestampPlusSeconds(65);
					tokenObj.setExpireTime(oneMinute);
					dataAccess.updateToken(tokenObj);
					outcome = Outcomes.SUCCESS;
				} else {
					reason = Constants.TOKEN_EXPIRED;
				}
			} else {
				reason = Constants.TOKEN_NOT_FOUND;
			}
		} else {
			dataAccess.addLog("device or enc was blank");
			reason = Constants.TOKEN_ERROR;
		}
		return new ApiResponse(outcome, reason);
	}
}
