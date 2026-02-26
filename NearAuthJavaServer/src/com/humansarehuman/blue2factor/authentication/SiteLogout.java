package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.authentication.saml.SamlSignout;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;

@Controller
@RequestMapping(Urls.LOGOUT)
@SuppressWarnings("ucd")
public class SiteLogout extends B2fApi {
	@RequestMapping(method = RequestMethod.GET)
	public Object processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		SamlDataAccess dataAccess = new SamlDataAccess();
		String nextPage = "notSignedUp";
		try {
			String authToken = getPersistentToken(request);
			IdentityObjectFromServer idObj = this.getIdObjWithoutCompany(authToken, dataAccess);
			if (idObj != null) {
				nextPage = new SamlSignout().signout(httpResponse, request, model, idObj, idObj.getCompany(),
						dataAccess);
			}
		} catch (Exception e) {
			dataAccess.addLog("ClientFailure", e);
			model.addAttribute("message", e.getMessage());
		}
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return nextPage;
	}
}
