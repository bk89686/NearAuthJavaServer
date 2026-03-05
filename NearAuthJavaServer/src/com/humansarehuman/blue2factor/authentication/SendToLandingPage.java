package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.BrowserDbObj;
import com.humansarehuman.blue2factor.utilities.jwt.JsonWebToken;

/**
 * this is yet another stop at the server on setup, which blows, but right now
 * its the only way I can think of to pass the jwt without using a post or get
 * variable
 * 
 * @author cjm10
 *
 */
//Unused - instead we use a session variable to set the JWT on the first
//time we validate -- cjm
@Controller
@RequestMapping(Urls.TO_LANDING_PAGE)
@SuppressWarnings("ucd")
public class SendToLandingPage extends B2fApi {
	@RequestMapping(method = RequestMethod.GET)
	public void sendToLandingPageProcessGet(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		String authToken = this.getPersistentToken(request);
		String url = Urls.SECURE_URL + Urls.SETUP_FAILURE;
		// need to get a token that can be expired
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		dataAccess.addLog("sessionToken: " + authToken);
		if (!TextUtils.isBlank(authToken)) {
			BrowserDbObj browser = dataAccess.getBrowserByToken(authToken, TokenDescription.AUTHENTICATION);
			if (browser != null) {
				IdentityObjectFromServer idObj = new IdentityObjectFromServer(browser, false);
				String audience = idObj.getCompany().getCompleteCompanyLoginUrl();
				String jwt = new JsonWebToken().buildJwt(idObj, audience);
				url = idObj.getCompany().getCompleteCompanyLoginUrl();
				response.setHeader("Authorization", "Bearer " + jwt);
				dataAccess.addLog("Bearer " + jwt);
			} else {
				dataAccess.addLog("browser was null");
				model.addAttribute("message2", "user could not be found");
			}
		}
		dataAccess.addLog("forwarding to: " + url);
		response.setHeader("Location", url);
		response.setStatus(302);

	}

}
