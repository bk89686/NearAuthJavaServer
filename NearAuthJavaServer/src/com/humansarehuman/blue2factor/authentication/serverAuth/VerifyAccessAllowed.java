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
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.AccessAllowedWithAccessType;
import com.humansarehuman.blue2factor.entities.jsonConversion.ApiRequest;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(Urls.VERIFY_ACCESS)
public class VerifyAccessAllowed extends B2fApi {
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AccessAllowedWithAccessType validateTokenGetPost(@RequestBody ApiRequest apiRequest,
			HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		AccessAllowedWithAccessType response;
		SamlDataAccess dataAccess = new SamlDataAccess();
		
		String reqUrl = GeneralUtilities.getUrlHost(apiRequest.getReqUrl());
		Encryption encryption = new Encryption();
		String session = new Encryption().decryptBasedOnBrowserOrServerId(apiRequest.getToken(), 
				apiRequest.getEncryptedSession(), reqUrl);
		response = this.checkAccessAllowedFromApi(dataAccess, encryption, apiRequest.getToken(), session, reqUrl);
		
		return response;
	}
}
