package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Urls;

@Controller
@RequestMapping(Urls.APPLE_ASSOCIATION)
@SuppressWarnings("ucd")
public class AppleSiteAssociation {
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String processGetAppleAssociations(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		return "appleSiteAssociation";
	}
}