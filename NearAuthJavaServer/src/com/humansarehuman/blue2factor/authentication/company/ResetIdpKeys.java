package com.humansarehuman.blue2factor.authentication.company;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;

@Controller
@RequestMapping(Urls.RESET_IDP_KEYS)
@SuppressWarnings("ucd")
public class ResetIdpKeys extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			@PathVariable("companyID") String companyId) {
		int outcome = Outcomes.FAILURE;
		DataAccess dataAccess = new DataAccess();
		try {
			if (addCompanyIdpKeys(companyId)) {
				outcome = Outcomes.SUCCESS;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		model.addAttribute("outcome", outcome);
		return "basicResult";
	}
}
