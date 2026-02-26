package com.humansarehuman.blue2factor.authentication;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;

@Controller
@RequestMapping(Urls.FAILSAFE)
@SuppressWarnings("ucd")
public class Failsafe extends BaseController {

	@RequestMapping(method = RequestMethod.GET)
	public String failsafeProcessGet(HttpServletRequest request, ModelMap model) {
		// String serverId = GeneralUtilities.randomString();
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		String coId = this.getRequestValue(request, "co");
		if (TextUtils.isEmpty(coId)) {
			coId = "hEST40wxme7awgIo0cdTCBcksoyZbHsCIfGKPbXh";
		}
		CompanyDbObj company = dataAccess.getCompanyById(coId);
		if (company != null) {
			ArrayList<GroupDbObj> groups = dataAccess.getGroupsFromCompany(company);
			for (GroupDbObj group : groups) {
				List<DeviceDbObj> devices = new DeviceDataAccess().getActiveDevicesFromGroup(group);
				for (DeviceDbObj device : devices) {
					dataAccess.setTemp(device, "theendoftheworldasweknowit");
				}
			}
		}
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String failsafeProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {

		String oauthToken = this.getRequestValue(request, "Authorization");
		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("oauth2: " + oauthToken, LogConstants.DEBUG);

		BasicResponse response = new BasicResponse(Outcomes.SUCCESS, "oauth2: " + oauthToken);
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
