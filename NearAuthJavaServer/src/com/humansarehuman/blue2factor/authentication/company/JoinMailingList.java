package com.humansarehuman.blue2factor.authentication.company;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;

@Controller
@RequestMapping(value = Urls.JOIN_MAILING_LIST)
@SuppressWarnings("ucd")
public class JoinMailingList extends B2fApi {
	@RequestMapping(method = RequestMethod.GET)
	public String processGetJoinMailingList(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		DataAccess dataAccess = new DataAccess();
		try {
			String email = this.getRequestValue(request, "em").toLowerCase();
			String name = this.getRequestValue(request, "na");
			boolean beta = this.getRequestValue(request, "be").equalsIgnoreCase("true");
			String backend = this.getRequestValue(request, "ba");
			String emailId = this.getRequestValue(request, "eid");

			dataAccess.addLog("email: " + email + ", backend: " + backend);
			if (dataAccess.addEmailToMailingList(email, name, beta, backend, emailId)) {
				outcome = Outcomes.SUCCESS;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		model.addAttribute("outcome", outcome);
		return "basicResult";
	}
}
