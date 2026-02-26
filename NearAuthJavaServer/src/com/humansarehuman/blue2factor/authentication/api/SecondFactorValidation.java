package com.humansarehuman.blue2factor.authentication.api;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.http.util.TextUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.jsonConversion.ApiRequest;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(value = Urls.SECOND_FACTOR_VALIDATION)
public class SecondFactorValidation extends B2fApi {

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@SuppressWarnings("ucd")
	public @ResponseBody ApiResponseWithToken getJson(HttpServletRequest request) throws IOException {
		ApiResponseWithToken apiResponse = null;
		String coKey = "En8Vp07IruaGsLIHzd";
		String cmd = "proxcheck";
		String b2fSession = "tPbAYDZLir8JaJf6Rv1J3A9ajjMN0DuVy3GY53Pd";
		String b2fToken = "";
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("ProximityApiEndpoint",
				"cmd: '" + cmd + "'; coKey: " + coKey + ", sess: " + b2fSession + ", b2fToken: " + b2fToken,
				LogConstants.WARNING);
		switch (cmd) {
		case Constants.FACTOR_2_CHECK:
			apiResponse = serverFactor2Check(coKey, b2fToken, b2fSession, "");
			break;
		case Constants.CONFIRM_TOKEN:
			apiResponse = expireOtherTokens(coKey, b2fToken, "");
		default:
			dataAccess.addLog("ProximityApiEndpoint", Constants.BAD_COMMAND + ": '" + cmd + "'", LogConstants.WARNING);
			apiResponse = new ApiResponseWithToken(Outcomes.FAILURE, Constants.BAD_COMMAND + ": '" + cmd + "' - GET",
					"");
			break;
		}
		dataAccess.addLog("ProximityApiEndpoint", "outcome: " + apiResponse.outcome);
		return apiResponse;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@SuppressWarnings("ucd")
	public @ResponseBody ApiResponseWithToken postJson(@RequestBody ApiRequest apiReq, HttpServletRequest request)
			throws IOException {
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("ProximityApiEndpoint", "entry - POST");
		String b2fToken = apiReq.getBrowserToken();
		ApiResponseWithToken apiResponse = null;
		String coKey = apiReq.getCoKey();
		String cmd = apiReq.getCmd();
		String browserSession = apiReq.getBrowserSession();
		String baseUrl = GeneralUtilities.getUrlHost(apiReq.getReqUrl());

		dataAccess.addLog("ProximityApiEndpoint",
				"cmd: '" + cmd + "'; coKey: " + coKey + ", sess: " + browserSession + ", b2fToken: " + b2fToken,
				LogConstants.WARNING);
		switch (cmd) {
		case Constants.FACTOR_2_CHECK:
			apiResponse = serverFactor2Check(coKey, b2fToken, browserSession, baseUrl);
			break;
		case Constants.CONFIRM_TOKEN:
			apiResponse = expireOtherTokens(coKey, b2fToken, baseUrl);
			break;
		default:
			dataAccess.addLog("ProximityApiEndpoint", Constants.BAD_COMMAND + ": '" + cmd + "'", LogConstants.WARNING);
			apiResponse = new ApiResponseWithToken(Outcomes.FAILURE, Constants.BAD_COMMAND + ": '" + cmd + "' - POST",
					"");
			break;
		}
		return apiResponse;
	}

	private ApiResponseWithToken expireOtherTokens(String coKey, String b2fToken, String url) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("called on: '" + b2fToken + "'");
		TokenDbObj token = dataAccess.getToken(b2fToken);
		if (token != null) {
			CompanyDbObj company = dataAccess.getCompanyByPrivateKey(coKey);
			CompanyDbObj companyFromToken = dataAccess.getCompanyByToken(b2fToken);
			if (company != null && companyFromToken != null) {
				if (company.getCompanyId().equals(companyFromToken.getCompanyId())) {
					dataAccess.expireOtherTokens(token, TokenDescription.F2_SERVER, url);
					outcome = Outcomes.SUCCESS;
				} else {
					reason = "company mismatch";
				}
			} else {
				reason = "bad company (til the day I die)";
			}
		} else {
			reason = "token not found";
		}
		dataAccess.addLog("outcome: " + outcome);
		return new ApiResponseWithToken(outcome, reason, "");
	}

	public ApiResponseWithToken serverProxCheckTest() {
		return serverFactor2Check("En8Vp07IruaGsLIHzd", "GjbTOKVcMtNmRJA7YZq2QsF7dFlfY2ZK-MWgMqT2EuOZo2rHR-F9Dw", "",
				"");
	}

	private ApiResponseWithToken serverFactor2Check(String coKey, String b2fToken, String browserSession, String url) {
		ApiResponseWithToken apiResponse;
		if (!TextUtils.isBlank(browserSession) || !TextUtils.isBlank(b2fToken)) {
			if (!TextUtils.isBlank(coKey)) {
				CompanyDataAccess dataAccess = new CompanyDataAccess();
				CompanyDbObj company = dataAccess.getCompanyByPrivateKey(coKey);
				if (company != null) {
					dataAccess.addLog("company found");
//                    if (browserSession.endsWith("sync")) {
//                        browserSession = browserSession.substring(0, browserSession.length() - 4);
//                        dataAccess.addLog("serverFactor2Check", "recheck: " + browserSession);
//                        b2fToken = "";
//                    }
					if (!TextUtils.isBlank(b2fToken)) {
						apiResponse = getAccessAllowedByTokenAndCompany(b2fToken, company, url);
					} else {
						apiResponse = checkForNewSignup(browserSession, url);
					}
				} else {
					dataAccess.addLog("company Not found");
					int response = Outcomes.ERROR;
					String reason = Constants.CO_NOT_FOUND;
					apiResponse = new ApiResponseWithToken(response, reason, "");
				}
			} else {
				int response = Outcomes.ERROR;
				String reason = Constants.CO_NOT_FOUND;
				apiResponse = new ApiResponseWithToken(response, reason, "");
			}
		} else {
			int response = Outcomes.FAILURE;
			String reason = Constants.DEV_NOT_FOUND;
			apiResponse = new ApiResponseWithToken(response, reason, "");
		}
		return apiResponse;
	}

//    @SuppressWarnings("unused")
//    private ApiResponseWithToken sendLoudPush(DeviceDbObj device) {
//        int outcome = Outcomes.FAILURE;
//        String reason = "";
//        PushNotifications push = new PushNotifications();
//        DeviceDbObj central;
//        if (device.isCentral()) {
//            central = device;
//        } else {
//            central = new DeviceDataAccess().getConnectedCentral(device);
//        }
//        return push.sendLoudPushByDevice(central);
//    }
}
