package com.humansarehuman.blue2factor.authentication;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.communication.Emailer;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.DeviceClass;
import com.humansarehuman.blue2factor.entities.enums.OsClass;
import com.humansarehuman.blue2factor.entities.enums.UserType;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.VALIDATE_USER)
@SuppressWarnings("ucd")
public class ValidateUser extends B2fApi {

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String validateUserProcessPost(HttpServletRequest request, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "");
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		try {
			dataAccess.addLog("entry");
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String username = this.getEncryptedRequestValue(request, "ljao98sajnmf", key, iv).toLowerCase();
			String deviceId = this.getEncryptedRequestValue(request, "hcsaebstj", key, iv);
			String loginToken = this.getEncryptedRequestValue(request, "tnahuckxhbuc", key, iv);
			Boolean hasBle = this.getEncryptedRequestBoolean(request, "nthnsss", key, iv, true);
			String tempId = this.getEncryptedRequestValue(request, "solcqn", key, iv);
			dataAccess.addLog("username: '" + username + "'", LogConstants.DEBUG);
			response = validateUser(username, deviceId, loginToken, hasBle, tempId);
		} catch (Exception e) {
			dataAccess.addLog(e);
			response.setReason(e.getLocalizedMessage());
		}
		printProblem(response);
		model = this.addBasicResponse(model, response);
		return "result";
	}

	private BasicResponse validateUser(String username, String deviceId, String loginToken, Boolean hasBle,
			String tempId) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "");
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		if (!TextUtils.isBlank(username)) {
			GroupDbObj group = dataAccess.getGroupByEmail(username);
			if (group == null) {
				group = addUserToGroupIfNeeded(username);
			}
			if (group == null) {
				dataAccess.addLog("group not in database yet. We'll see what we can do.");
				String emailDomain = GeneralUtilities.getEmailDomain(username);
				if (emailDomain != null) {
					CompanyDbObj company = dataAccess.getCompanyByEmailDomain(emailDomain);
					if (company != null && company.isAllowAllFromIdp()) {
						if (dataAccess.isLicenseAvailableForCompany(company.getCompanyId())) {
							String uid = GeneralUtilities.getUidFromEmail(username);
							GroupDbObj newGroup = new GroupDbObj(company.getCompanyId(),
									GeneralUtilities.randomString(), username, company.getAcceptedTypes(), true,
									DateTimeUtilities.getCurrentTimestamp(), 5, "", GeneralUtilities.randomString(), 2,
									0, 0, username, null, uid, UserType.USER, false, company.isPushAllowed(),
									company.isTextAllowed());
							dataAccess.addGroup(newGroup);
						}
					} else {
						response.setReason(Constants.COMPANY_NOT_FOUND);
					}
				}
			}
			if (group != null) {
				CompanyDbObj company = dataAccess.getCompanyById(group.getCompanyId());
				if (company != null) {
					if (!TextUtils.isEmpty(company.getCompleteCompanyLoginUrl())) {
						if (group.getDevicesInUse() == 0) {
							response = addOrUpdateDevice(group, deviceId, loginToken, hasBle, tempId);
						} else {
							response.setReason(Constants.ALREADY_ACTIVATED);
						}
					} else {
						response.setReason(Constants.BASE_URL_NOT_SET);
					}
				} else {
					response.setReason(Constants.COMPANY_NOT_FOUND);
				}
			} else {
				response.setReason(Constants.GROUP_NOT_FOUND);
			}
		} else {
			response.setReason(Constants.USERNAME_WAS_EMPTY);
		}
		return response;
	}

	private BasicResponse addOrUpdateDevice(GroupDbObj group, String deviceId, String loginToken, Boolean hasBle,
			String tempId) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		dataAccess.addLog("group found");
		CompanyDbObj company = dataAccess.getCompanyById(group.getCompanyId());
		if (company != null) {
			int licensesInUse = dataAccess.getLicensesInUse(company.getCompanyId());
			if (company.getLicenseCount() > licensesInUse) {
				reason = group.getGroupId();
				DeviceDbObj previousDevice = dataAccess.getDeviceByDeviceId(deviceId);
				DeviceDbObj device;
				if (previousDevice == null) {
					device = addNewDevice(group, deviceId, loginToken, hasBle);
				} else {
					device = updateDevice(group, previousDevice, deviceId, loginToken, hasBle);
				}
				if (device.isActive()) {
					reason = "device already active";
				} else {
					outcome = Outcomes.SUCCESS;
					dataAccess.addLog(deviceId, "User was validated");
				}
				if (dataAccess.isTestUser(group.getGroupName(), false)) {
					token = Constants.TEST_USER;
				} else {
					if (outcome == Outcomes.SUCCESS) {
						new Emailer().sendAppConfirmEmail(group.getGroupName(), tempId);
					}
				}
			} else {
				reason = "company full";
			}
		} else {
			reason = "company not found for group - this is unusual";
		}
		return new BasicResponse(outcome, reason, token);
	}

	private void printProblem(BasicResponse response) {
		if (response.getOutcome() != Outcomes.SUCCESS) {
			new DataAccess().addLog("printProblem", "reason: " + response.getReason(), LogConstants.WARNING);
		}
	}

//    @SuppressWarnings("unused")
//    private GroupDbObj addGroup(GroupDbObj group, String username) {
//        if (group == null) {
//            // add group for test
//            group = new GroupDbObj("hxMic22Owkbxh2ax9owSFak0tsL7BzHh063kpq86",
//                    GeneralUtilities.randomString(), username, 1, false,
//                    DateTimeUtilities.getCurrentTimestamp(), 240, "",
//                    GeneralUtilities.randomString(), 2, 0, 0, "Test User", null, "");
//            new CompanyDataAccess().addGroup(group);
//        }
//        return group;
//    }

	private DeviceDbObj addNewDevice(GroupDbObj group, String deviceId, String loginToken, Boolean hasBle) {
		Timestamp date = new Timestamp(new GregorianCalendar(2018, Calendar.FEBRUARY, 11).getTimeInMillis());
		Timestamp baseTimestamp = DateTimeUtilities.getBaseTimestamp();
		new DataAccess().addLog("adding");
		DeviceDbObj device = new DeviceDbObj(group.getGroupId(), null, deviceId, 0, false, null, null, new Date(),
				DateTimeUtilities.getCurrentTimestamp(), null, OsClass.UNKNOWN, loginToken, null, -1, null, null, null,
				null, true, -1.0, false, 0, false, date, false, false, "", "", false, baseTimestamp, false,
				baseTimestamp, false, DeviceClass.UNKNOWN, false, true, baseTimestamp, null, hasBle, false, null, false,
				true, null, false);
		new CompanyDataAccess().addDevice(device);
		return device;
	}

	private DeviceDbObj updateDevice(GroupDbObj group, DeviceDbObj previousDevice, String deviceId, String loginToken,
			Boolean hasBle) {
		Timestamp date = new Timestamp(new GregorianCalendar(2018, Calendar.FEBRUARY, 11).getTimeInMillis());
		Timestamp baseTimestamp = DateTimeUtilities.getBaseTimestamp();
		DeviceDbObj device = new DeviceDbObj(group.getGroupId(), previousDevice.getUserId(), deviceId, 0, false, null,
				null, new Date(), DateTimeUtilities.getCurrentTimestamp(), null, OsClass.UNKNOWN, loginToken, null, -1,
				null, null, null, null, true, -1.0, false, 0, false, date, false, false, "", "", false, baseTimestamp,
				false, baseTimestamp, false, DeviceClass.UNKNOWN, false, true, baseTimestamp, null, hasBle, false, null,
				false, true, null, false);
		new CompanyDataAccess().updateDevice(device, "ValidateUser");
		return device;
	}

	@SuppressWarnings("unused")
	private void deleteTestUserIfNeeded(String username) {
		DataAccess dataAccess = new DataAccess();
		try {
			dataAccess.deleteGroupByUser(username);
		} catch (Exception e) {
			dataAccess.addLog("deleteTestUserIfNeeded", e);
		}
	}

}
