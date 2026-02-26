package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.failures.Failure;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.VALIDATE_TEXT_URL)
@SuppressWarnings("ucd")
public class SubmitTextCode extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String submitTextCodeProcessGet(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		getVersion(request);
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			IdentityObjectFromServer idObj = this.getIdObjWithoutCompany(request);
			if (idObj != null && idObj.getDevice() != null && idObj.getDevice().isActive()) {
				model.addAttribute("fake", false);
			} else {
				// this is not a real situation but needed here for Twilio confirmation
				dataAccess.addLog("setting up fake text code form");
				model.addAttribute("fake", true);
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		model.addAttribute("showForm", true);
		return "enterTextCode";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String submitTextCodePost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		getVersion(request);
		String next;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		boolean errorFound = false;
		model.addAttribute("showForm", false);
		try {
			String textCode = getRequestValue(request, "textcode");
			dataAccess.addLog("testing: " + getRequestValue(request, "test"));
			boolean testing = Boolean.parseBoolean(getRequestValue(request, "test"));
			IdentityObjectFromServer idObj = this.getIdObjWithoutCompany(request);
			if (validateIsNumber(textCode)) {
				if ((idObj != null && idObj.getDevice() != null && idObj.getDevice().isActive())) {
					CheckDbObj check = dataAccess.getCheckByPeripheralAndCode(idObj.getDevice().getDeviceId(),
							textCode);
					if (check != null) {
						this.updateCheckAsSuccessful(check, GeneralUtilities.getClientIp(request), ConnectionType.TXT);
						dataAccess.addLog("the check was found");
						String redirectUrl = new Failure().getReferrer(request, idObj.getCompany(), dataAccess);
						DeviceConnectionDbObj conn = dataAccess.getConnectionForPeripheral(idObj.getDevice(), true);
						dataAccess.updateAsInstallComplete(conn);
						dataAccess.addLog("redirecting to: " + redirectUrl);
						httpResponse.setHeader("Location", redirectUrl);
						httpResponse.setStatus(302);
						DeviceDbObj central = dataAccess.getCentralDeviceByConnection(conn);
						if (central != null) {
							dataAccess.setActive(central, true);
							next = null;
						} else {
							next = "enterTextCode";
							errorFound = true;
							model.addAttribute("errorMessage", Constants.CENTRAL_NOT_FOUND);
						}

					} else {
						next = "enterTextCode";
						errorFound = true;
						model.addAttribute("errorMessage", Constants.CHECK_NOT_FOUND);
					}
				} else if (testing) {
					dataAccess.addLog("test is " + testing);
					model.addAttribute("errorMessage", "this is in test mode");
					next = "enterTextCode";
				} else {
					errorFound = true;
					model.addAttribute("errorMessage", Constants.USER_NOT_FOUND);
					next = "enterTextCode";
				}
			} else {
				errorFound = true;
				model.addAttribute("errorMessage", "Please make sure you enter a six digit number");
				model.addAttribute("showForm", true);
				next = "enterTextCode";
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			errorFound = true;
			next = "enterTextCode";
		}
		model.addAttribute("errorFound", errorFound);

		return next;
	}

	/**
	 * Just check if this is a 6 digit number
	 * 
	 * @param code
	 * @return
	 */
	public boolean validateIsNumber(String code) {
		boolean success = false;
		code = code.trim();
		if (code.length() == 6) {
			if (code.matches("\\d+")) {
				success = true;
			}
		}
		return success;
	}
}
