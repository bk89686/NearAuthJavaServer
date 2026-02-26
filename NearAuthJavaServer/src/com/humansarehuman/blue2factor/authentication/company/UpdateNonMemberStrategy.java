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
import com.humansarehuman.blue2factor.entities.enums.NonMemberStrategy;
import com.humansarehuman.blue2factor.entities.jsonConversion.UpdateNonMemberStrategyRequest;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponse;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;

@Controller
@RequestMapping(Urls.UPDATE_NON_MEMBER_STRATEGY)
@SuppressWarnings("ucd")
public class UpdateNonMemberStrategy extends B2fApi {
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse updateUserFlowSettingsProcessPost(
			@RequestBody UpdateNonMemberStrategyRequest jsRequest, HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		AdminSignin adminSignin = checkPermission(request, httpResponse);
		if (adminSignin.isAllowed()) {
			CompanyDbObj company = adminSignin.getCompany();
			if (company != null) {
				NonMemberStrategy nonMemberStrategy;
				Boolean noConsoleAllowed = jsRequest.isNoConsoleAllowed();
				Boolean noDeviceAllowed = jsRequest.isNoDeviceAllowed();
				dataAccess.addLog("noDeviceAllowed: " + noDeviceAllowed + ", noConsoleAllowed: " + noConsoleAllowed);
				if (noConsoleAllowed) {
					if (noDeviceAllowed) {
						nonMemberStrategy = NonMemberStrategy.ALLOW_NOT_SIGNED_UP_OR_NO_DEVICE;
					} else {
						nonMemberStrategy = NonMemberStrategy.ALLOW_NOT_SIGNED_UP;
					}
				} else {
					if (noDeviceAllowed) {
						nonMemberStrategy = NonMemberStrategy.ALLOW_NO_DEVICE;
					} else {
						nonMemberStrategy = NonMemberStrategy.ALLOW_AUTHENTICATED_ONLY;
					}
				}
				company.setNonMemberStrategy(nonMemberStrategy);
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
