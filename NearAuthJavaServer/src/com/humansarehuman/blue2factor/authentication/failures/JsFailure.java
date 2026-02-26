package com.humansarehuman.blue2factor.authentication.failures;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.IdentityObjectFromServer;
import com.humansarehuman.blue2factor.entities.UrlAndModel;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = { Urls.JS_FAILURE, Urls.SERVER_FAILURE, Urls.SIGN_IN_GENERIC })
@SuppressWarnings("ucd")
public class JsFailure extends Failure {
	@RequestMapping(method = RequestMethod.GET)
	public Object jsFailureProcessGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		String nextPage = "needsResync";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		try {
			dataAccess.addLog("entry");
			String url = this.getRequestValue(request, "url");
			String session = this.getRequestValue(request, "tid");
			request = setReferrer(request, url);
			model.addAttribute("previousUrl", url);
			String authToken = getPersistentToken(request);
			dataAccess.addLog("authToken: " + authToken, LogConstants.TRACE);
			IdentityObjectFromServer idObj = this.getIdObj(authToken, dataAccess);
			if (idObj != null) {
				UrlAndModel urlAndModel = handleFailureWithIdObj(request, httpResponse, model, idObj, dataAccess,
						authToken, session);
				nextPage = urlAndModel.getUrl();
				model = urlAndModel.getModelMap();
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			model.addAttribute(e.getMessage());
		}
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return nextPage;
	}

}
