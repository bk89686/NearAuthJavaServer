package com.humansarehuman.blue2factor.authentication.company;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.AdminSignin;
import com.humansarehuman.blue2factor.entities.jsonConversion.UpdateUserFlowRequest;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponse;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;

@Controller
@RequestMapping(Urls.UPDATE_USER_FLOW_SETTINGS)
@SuppressWarnings("ucd")
public class UpdateUserFlowSettings extends B2fApi {
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse updateUserFlowSettingsProcessPost(@RequestBody UpdateUserFlowRequest jsRequest,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		AdminSignin adminSignin = checkPermission(request, httpResponse);
		if (adminSignin.isAllowed()) {

			CompanyDbObj company = adminSignin.getCompany();
			if (company != null) {
				Boolean moveB2fUsersToIdp = jsRequest.isAddToAd();
				company.setMoveB2fUsersToIdp(moveB2fUsersToIdp);
				Boolean allowAllFromIdp = jsRequest.isAllowAllIdpUsers();
				dataAccess.addLog("addToAd: " + moveB2fUsersToIdp + ", allowAllFromIdp: " + allowAllFromIdp);
				company.setAllowAllFromIdp(allowAllFromIdp);
				dataAccess.updateCompany(company);
				outcome = Outcomes.SUCCESS;
			} else {
				reason = Constants.COMPANY_NOT_FOUND;
			}

		} else {
			reason = Constants.NOT_PERMITTED;
		}
		ApiResponse response = new ApiResponse(outcome, reason);
		return response;
	}
}
