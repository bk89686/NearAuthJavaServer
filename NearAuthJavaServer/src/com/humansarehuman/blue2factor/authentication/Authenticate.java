package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;

@Controller
@RequestMapping(Urls.AUTHENTICATE)
@SuppressWarnings("ucd")
public class Authenticate extends B2fApi {
	@RequestMapping(method = RequestMethod.GET)
	public String getAuthenticateProcessGet(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		String next = null;
		SamlDataAccess dataAccess = new SamlDataAccess();
		try {
			String authToken = getPersistentToken(request);
			dataAccess.addLog("authToken: " + authToken);
			IdentityObjectFromServer idObj = this.getIdObj(authToken, dataAccess);
			if (idObj != null && idObj.getCompany() != null) {
				String url = idObj.getCompany().getCompleteCompanyLoginUrl();
				httpResponse.sendRedirect(url);
			}
		} catch (Exception e) {
			next = "error";
		}
		return next;
	}
}
