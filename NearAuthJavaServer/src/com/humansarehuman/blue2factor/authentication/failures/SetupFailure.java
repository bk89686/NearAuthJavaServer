package com.humansarehuman.blue2factor.authentication.failures;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Urls;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(Urls.SETUP_FAILURE)
@SuppressWarnings("ucd")
public class SetupFailure extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		model.addAttribute("message2", "Unknown error");
		model.addAttribute("environment", Constants.ENVIRONMENT.toString());
		return "setupFailure";
	}

}
