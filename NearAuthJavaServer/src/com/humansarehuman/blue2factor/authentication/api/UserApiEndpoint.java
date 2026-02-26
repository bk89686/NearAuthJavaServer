package com.humansarehuman.blue2factor.authentication.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
import com.humansarehuman.blue2factor.entities.enums.UserType;
import com.humansarehuman.blue2factor.entities.jsonConversion.UserApiRequest;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponse;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(value = Urls.USER_API_ENDPOINT)
@SuppressWarnings("ucd")
public class UserApiEndpoint extends B2fApi {

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse userApiEndpointostJson(@RequestBody UserApiRequest apiReq,
			HttpServletRequest request) throws IOException {
		ApiResponse response = null;
		String coKey = apiReq.getCoKey();
		String email = apiReq.getEmail();
		String userName = apiReq.getUsername();
		String uid = apiReq.getUid();
		String cmd = apiReq.getCmd();
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("coKey: " + coKey + "; email: " + email + "; userName: " + userName + "; uid: " + uid
				+ "; cmd: " + cmd);
		switch (cmd) {
		case "addUser":
			response = addUser(coKey, email, userName, uid);
			break;
		case "editUser":
			response = updateUser(coKey, email, userName, uid);
			break;
		case "deleteUser":
			response = deleteUser(coKey, email);
			break;
		default:
			dataAccess.addLog("badCommand: " + cmd);
			response = new ApiResponse(Outcomes.ERROR, Constants.BAD_COMMAND + ": " + cmd);
		}
		return response;
	}

	private ApiResponse updateUser(String coKey, String email, String username, String uid) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		int outcome = Outcomes.FAILURE;
		String reason = "";
		if (!TextUtils.isBlank(coKey)) {
			CompanyDbObj company = dataAccess.getCompanyByPrivateKey(coKey);
			if (company != null) {
				GroupDbObj group = dataAccess.getGroupByEmail(email);
				boolean updated = false;
				if (group != null) {
					if (!TextUtils.isBlank(uid)) {
						if (uid.equalsIgnoreCase("blank")) {
							uid = "";
						}
						dataAccess.addLog("updating Uid");
						group.setUid(uid);
						updated = true;
					}
					if (!TextUtils.isBlank(username)) {
						group.setUsername(username);
						dataAccess.addLog("updating username");
						updated = true;
					}
					if (updated) {
						dataAccess.updateGroup(group);
					}
					outcome = Outcomes.SUCCESS;
				}
				if (updated) {
					dataAccess.updateGroup(group);
				}
			} else {
				reason = Constants.CO_NOT_FOUND;
			}
		} else {
			reason = Constants.CO_NOT_FOUND;
		}
		return new ApiResponse(outcome, reason);
	}

	private ApiResponse addUser(String coKey, String email, String username, String uid) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		int outcome = Outcomes.FAILURE;
		String reason = "";
		if (!TextUtils.isBlank(coKey)) {
			CompanyDbObj company = dataAccess.getCompanyByPrivateKey(coKey);
			if (company != null) {
				GroupDbObj group = dataAccess.getGroupByEmail(email);
				if (group == null) {
					if (dataAccess.isLicenseAvailableForCompany(company.getCompanyId())) {
						GroupDbObj newGroup = new GroupDbObj(company.getCompanyId(), GeneralUtilities.randomString(),
								email, company.getAcceptedTypes(), true, DateTimeUtilities.getCurrentTimestamp(), 5, "",
								GeneralUtilities.randomString(), 2, 0, 0, username, null, uid, UserType.USER, false,
								company.isPushAllowed(), company.isTextAllowed());
						dataAccess.addGroup(newGroup);
						try {
							String resp = new GeneralUtilities()
									.readUrl("https://www.NearAuth.ai/nahscxbklacreldhuacdueabonu324peu?user="
											+ URLEncoder.encode(username, StandardCharsets.UTF_8.name()) + "&email="
											+ URLEncoder.encode(email, StandardCharsets.UTF_8.name()));
							dataAccess.addLog("response from request email: " + resp, LogConstants.DEBUG);
						} catch (UnsupportedEncodingException e) {
							dataAccess.addLog(e);
						}
						outcome = Outcomes.SUCCESS;
					} else {
						reason = Constants.NO_LICENSES_ARE_AVAILABLE;
					}
				} else {
					reason = Constants.EMAIL_SIGNED_UP;
				}
			} else {
				reason = Constants.CO_NOT_FOUND;
			}
		} else {
			reason = Constants.CO_NOT_FOUND;
		}
		return new ApiResponse(outcome, reason);
	}

	private ApiResponse deleteUser(String coKey, String email) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		int outcome = Outcomes.FAILURE;
		String reason = "";
		if (!TextUtils.isBlank(coKey)) {
			CompanyDbObj company = dataAccess.getCompanyByPrivateKey(coKey);
			if (company != null) {
				GroupDbObj group = dataAccess.getGroupByEmail(email);
				if (group != null) {
					if (group.getCompanyId().equals(company.getCompanyId())) {
						if (dataAccess.deleteGroupByUser(email)) {
							outcome = Outcomes.SUCCESS;
						} else {
							reason = Constants.GENERIC_FAILURE;
						}
					} else {
						reason = Constants.COMPANY_USER_MISMATCH;
					}
				} else {
					reason = Constants.UNKNOWN_USER;
				}
			} else {
				reason = Constants.CO_NOT_FOUND;
			}
		} else {
			reason = Constants.CO_NOT_FOUND;
		}
		return new ApiResponse(outcome, reason);
	}

}
