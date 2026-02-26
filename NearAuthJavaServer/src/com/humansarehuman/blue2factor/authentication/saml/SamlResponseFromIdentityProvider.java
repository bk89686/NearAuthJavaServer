package com.humansarehuman.blue2factor.authentication.saml;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.SamlDataAccess;
import com.humansarehuman.blue2factor.entities.UrlModelAndHttpResponse;

/**
 * If we come through here we are setting up. If the first factor is validated,
 * then check the second factor and set up the browser keys and the biometrics
 * 
 * @author cjm10
 *
 */
@Controller
@RequestMapping(Urls.SAML_RESPONSE_FROM_IDENTITY_PROVIDER)
@SuppressWarnings("ucd")
public class SamlResponseFromIdentityProvider extends SamlAndLdapResponse {

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	public String samlResponseFromAuthnRequest(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model, @PathVariable("apiKey") String apiKey) {
		String relayState = this.getRequestValue(request, "RelayState");
		String samlResponse = this.getRequestValue(request, "SAMLResponse");
		UrlModelAndHttpResponse urlModelAndHttpResponse = evaluateResponse(request, httpResponse, model, samlResponse,
				relayState, apiKey);
		String nextPage = urlModelAndHttpResponse.getUrl();
		model = urlModelAndHttpResponse.getModelMap();
		httpResponse = urlModelAndHttpResponse.getHttpResponse();
		return nextPage;
	}

	@SuppressWarnings("unused")
	private ArrayList<String> getParamsFromSource(HttpServletRequest request, SamlDataAccess dataAccess) {
		ArrayList<String> params = new ArrayList<>();
		String src = this.getSession(request, "src");
		dataAccess.addLog("getParamsFromSource", "'" + src + "'");
		if (!TextUtils.isEmpty(src)) {
			String[] split1 = src.split("&SamlResponse=");
			if (split1.length == 2) {
				String samlResponse = split1[1];
				String[] split2 = split1[0].split("RelayState=");
				if (split2.length == 2) {
					String relayState = split2[1];
					params.add(0, relayState);
					params.add(1, samlResponse);
				}
			}
		}
		return params;
	}
}
