package com.humansarehuman.blue2factor.authentication.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.AdminCodeResponse;
import com.humansarehuman.blue2factor.entities.AdminSignin;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = Urls.GET_ADMIN_CODES)
@SuppressWarnings("ucd")
public class GetAdminCodes extends B2fApi {

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AdminCodeResponse pamServerCheckPostJson(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model) {
		AdminCodeResponse adminCodeResponse = null;
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		try {
			AdminSignin adminSignin = checkPermission(request, httpResponse);
			if (adminSignin.canAdministerCodes()) {
				dataAccess.addLog("user is allowed");
				httpResponse = updateCompanyPageCookie(httpResponse, adminSignin.getGroup());
				adminCodeResponse = dataAccess.generateAccessCodes();
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return adminCodeResponse;
	}

}
