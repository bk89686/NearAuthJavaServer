package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;

@Controller
@RequestMapping(Urls.OPTOUT_EMAIL)
@SuppressWarnings("ucd")
public class DeactivateEmail extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, ModelMap model) {
		String emailId = this.getRequestValue(request, "eid");
		String nextPage = "error";
		if (emailId != "") {
			DataAccess dataAccess = new DataAccess();
			String email = dataAccess.getEmailById(emailId);
			dataAccess.addLog("removing the email " + email + " from the mailing list");
			dataAccess.deactiveEmail(emailId);
			nextPage = "deactivate";
		} else {
			model.addAttribute("errorMessage", "oh crap. we couldn't find you.");
		}
		return nextPage;
	}
}
