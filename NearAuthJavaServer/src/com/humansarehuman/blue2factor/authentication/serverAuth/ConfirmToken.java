package com.humansarehuman.blue2factor.authentication.serverAuth;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.entities.jsonConversion.ApiRequest;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponse;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This expires all of the old tokens
 * 
 * 
 */
@Controller
@RequestMapping(Urls.CONFIRM_TOKEN)
public class ConfirmToken extends B2fApi {
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponse confirmTokenGetPost(@RequestBody ApiRequest apiRequest,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		ApiResponse response = null;
		Encryption encryption = new Encryption();
		String reqUrl = GeneralUtilities.getUrlHost(apiRequest.getReqUrl());
		String session = encryption.decryptBasedOnBrowserOrServerId(apiRequest.getToken(), 
				apiRequest.getEncryptedSession(), reqUrl);
		response = expireInstance(session, reqUrl);
		return response;
	}
}
