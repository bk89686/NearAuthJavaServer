package com.humansarehuman.blue2factor.authentication.browserSetup;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.entities.BasicResponse;

@Controller
@RequestMapping(value = Urls.FINGERPRINT_STATUS)
public class JsSetup extends B2fApi {

	@RequestMapping(method = RequestMethod.GET)
	public String jsStatusProcessGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String jsStatusProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {

		return "";
	}
}
