package com.humansarehuman.blue2factor.authentication.api;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = Urls.PAM_SERVER_CLIENT_KEY_LOOKUP)
@SuppressWarnings("ucd")
public class ServerClientKeyLookup extends B2fApi {
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken getJson(HttpServletRequest request, HttpServletResponse httpResponse)
			throws IOException {
		ApiResponseWithToken response = new ApiResponseWithToken(Outcomes.API_F1_FAILURE, "Method not allowed", "");
		return response;
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken postJson(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		try {
			String serverId = this.getRequestValue(request, "serverId");
			String clientId = this.getRequestValue(request, "clientId");
			if (dataAccess.isServerConnectionSetupByIds(clientId, serverId)) {
				KeyDbObj pubKey = dataAccess.getSshClientPublicKey(clientId);
				if (pubKey != null) {
					outcome = Outcomes.SUCCESS;
					token = clientId;
					reason = pubKey.getKeyText();
				}
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
		}

		return new ApiResponseWithToken(outcome, reason, token);
	}
}
