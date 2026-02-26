package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;

/**
 * Shows a please wait sign and bounces the user to the saml interface
 * 
 * @author cjm10
 *
 */

@Controller
@RequestMapping(Urls.PLEASE_WAIT)
@SuppressWarnings("ucd")
public class PleaseWait extends B2fApi {
	@RequestMapping(method = RequestMethod.GET)
	public String pleaseWaitGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		String email = this.getRequestValue(request, "sahsenthusnhau");
		String deviceId = this.getRequestValue(request, "tsdhush");
		new DataAccess()
				.addLog("This page serves absolutely no purpose other than to show a wait icon more quickly. devId: "
						+ deviceId);
		model.addAttribute("devId", deviceId);
		model.addAttribute("email", email);
		return "pleaseWait";
	}
}
