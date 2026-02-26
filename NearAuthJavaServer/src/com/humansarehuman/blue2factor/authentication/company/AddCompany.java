package com.humansarehuman.blue2factor.authentication.company;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.AuthorizationMethod;
import com.humansarehuman.blue2factor.entities.enums.NonMemberStrategy;
import com.humansarehuman.blue2factor.entities.enums.UserType;
import com.humansarehuman.blue2factor.entities.jsonConversion.CompanyCreation;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.ADD_COMPANY)
@SuppressWarnings("ucd")
public class AddCompany extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed", "");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken addCompanyProcessPost(@RequestBody CompanyCreation companyCreation,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		try {
			printAllRequestParams(request);
			String companyName = companyCreation.getCompany();
			String fullname = companyCreation.getFullName().trim();
			dataAccess.addLog("fullname: " + fullname, LogConstants.TRACE);

			if (TextUtils.isEmpty(companyName)) {
				companyName = fullname;
			}
			String email = companyCreation.getEmail();
			String emailDomain = GeneralUtilities.getEmailDomain(email);
			CompanyDbObj company = dataAccess.getCompanyByEmailDomain(emailDomain);
			if (company == null || !company.getCompanyName().equals(companyName)) {
				if (email != "" && emailDomain != null) {
					dataAccess.addLog("email: " + email);
					String pw = companyCreation.getPw1();// this.getRequestValue(request, "pw1");
					String companyId = GeneralUtilities.randomString();
					Encryption encryption = new Encryption();
					String encPw = null;
					String salt = GeneralUtilities.randomString();
					if (!TextUtils.isBlank(pw)) {
						encPw = encryption.encryptPw(pw, salt);
					}
					Timestamp date = new Timestamp(
							new GregorianCalendar(2018, Calendar.FEBRUARY, 11).getTimeInMillis());
					GroupDbObj group = dataAccess.getGroupByEmail(email);
					if (group == null) {
						dataAccess.addLog("encPw: " + encPw);
						group = new GroupDbObj(companyId, GeneralUtilities.randomString(), email, 1, true,
								DateTimeUtilities.getCurrentTimestamp(), 5, encPw, salt, 2, 0, 1, fullname, date, "",
								UserType.SUPER_ADMIN, false, false, false);
						dataAccess.addGroup(group);
						String apiKey = buildApiKey();
						company = new CompanyDbObj(companyId, companyName, "", 1, true, new Date(), "", "", apiKey, 10,
								0, "", "", AuthorizationMethod.SAML, AuthorizationMethod.API, "", "",
								NonMemberStrategy.ALLOW_NOT_SIGNED_UP, false, false, 18000, 18000, 18000, true, true,
								emailDomain, 18000, GeneralUtilities.getRandomUuid());
						dataAccess.addCompany(company);
						dataAccess.addLog("companyAdded");
						addCompanyIdpKeys(companyId);
						outcome = Outcomes.SUCCESS;
					} else {
						reason = "this email address has already been registered with NearAuth.ai.";
						dataAccess.addLog(reason);
					}
				} else {
					reason = "bad email";
				}
			} else {
				reason = "this company already exists";
			}
			dataAccess.addLog("fullname: " + fullname, LogConstants.TRACE);
			if (dataAccess.addEmailToMailingList(email, fullname, false, null, GeneralUtilities.randomString())) {
				outcome = Outcomes.SUCCESS;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = "error: " + e.getMessage();
		}
		ApiResponseWithToken resp = new ApiResponseWithToken(outcome, reason, token);
		return resp;
	}

	private String buildApiKey() {
		String api = GeneralUtilities.randomLetters(3).toUpperCase();
		api += Integer.toString(GeneralUtilities.randInt(2222, 9999));
		String letter = GeneralUtilities.randomLetters(1).toUpperCase();
		String number = Integer.toString(GeneralUtilities.randInt(2, 9));
		api += letter + letter + number + number;
		api = api.replace("0", "7").replace("1", "2");
		api = api.replace("O", "R").replace("L", "W").replace("I", "D");
		return api;
	}
}
