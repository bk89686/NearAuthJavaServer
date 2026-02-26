package com.humansarehuman.blue2factor.authentication.failures;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Urls;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(Urls.NOT_SIGNED_UP)
@SuppressWarnings("ucd")
public class NotSignedUp extends Failure {
	@RequestMapping(method = RequestMethod.GET)
	public Object processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		String companyId = this.getRequestValue(request, "company");
		model.addAttribute("company", companyId);
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return "notSignedUp";
	}
}
