package com.humansarehuman.blue2factor.authentication.saml;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;

@Controller
@RequestMapping(Urls.SIGN_OUT)
@SuppressWarnings("ucd")
public class BasicSignout extends B2fApi {
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken samlSignoutProcessGet(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model) {
		SamlDataAccess dataAccess = new SamlDataAccess();
		int outcome = Outcomes.FAILURE;
		dataAccess.addLog("start");
		String authToken = getPersistentToken(request);
		dataAccess.addLog("authToken: " + authToken);
		IdentityObjectFromServer idObj = this.getIdObj(authToken, dataAccess);
		dataAccess.addLog("set up IdObj");
		if (idObj != null) {
			outcome = new SamlSignout().signoutWithOutcome(idObj, dataAccess);
		}
		return new ApiResponseWithToken(outcome, "");
	}

}
