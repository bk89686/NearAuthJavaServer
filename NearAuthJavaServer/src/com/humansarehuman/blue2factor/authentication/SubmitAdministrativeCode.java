package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.authentication.failures.Failure;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.VALIDATE_ADMIN_CODE)
@SuppressWarnings("ucd")
public class SubmitAdministrativeCode extends B2fApi {
	@RequestMapping(method = RequestMethod.GET)
	public String submitAdminCodeProcessGet(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		getVersion(request);
		String next = "notSignedUp";
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			IdentityObjectFromServer idObj = this.getIdObjWithoutCompany(request);
			if (idObj != null && idObj.getDevice() != null && idObj.getDevice().isActive()) {
				next = "enterAdminCode";
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		model.addAttribute("showForm", true);
		return next;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String submitAdminCodeProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		getVersion(request);
		String next;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		boolean errorFound = false;
		model.addAttribute("showForm", false);
		try {
			String textCode = getRequestValue(request, "adminCode");
			IdentityObjectFromServer idObj = this.getIdObjWithoutCompany(request);
			if (validateAdminCode(textCode)) {
				if ((idObj != null && idObj.getDevice() != null && idObj.getDevice().isActive())) {
					CheckDbObj check = dataAccess.getCheckByDeviceIdAndAdminCode(idObj.getDevice(), textCode);
					if (check != null) {
						this.updateCheckAsSuccessful(check, GeneralUtilities.getClientIp(request),
								ConnectionType.ADMIN_CODE);
						dataAccess.addLog("the check was found");
						String redirectUrl = new Failure().getReferrer(request, idObj.getCompany(), dataAccess);
						DeviceConnectionDbObj conn = dataAccess.getConnectionForPeripheral(idObj.getDevice(), true);
						// CJM - TODO update install complete
						dataAccess.updateAsInstallComplete(conn);
						dataAccess.addLog("redirecting to: " + redirectUrl);
						httpResponse.setHeader("Location", redirectUrl);
						httpResponse.setStatus(302);
						next = null;
					} else {
						next = "enterAdminCode";
						errorFound = true;
						model.addAttribute("errorMessage", "The code you did not work.");
					}
				} else {
					errorFound = true;
					model.addAttribute("errorMessage", Constants.USER_NOT_FOUND);
					next = "enterAdminCode";
				}
			} else {
				errorFound = true;
				model.addAttribute("errorMessage", "The code you entered ... that is not my kind of code.");
				model.addAttribute("showForm", true);
				next = "enterAdminCode";
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			errorFound = true;
			next = "enterAdminCode";
		}
		model.addAttribute("errorFound", errorFound);
		return next;
	}

	private boolean validateAdminCode(String code) {
		return GeneralUtilities.isUuid(code);
	}
}
