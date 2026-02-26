package com.humansarehuman.blue2factor.authentication.company;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.RESET_PASSWORD)
@SuppressWarnings("ucd")
public class ResetAdminPassword extends BaseController {

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			@PathVariable("companyId") String companyId) {
		model.addAttribute("action", "/pw/" + companyId + "/reset");
		model.addAttribute("groupId", this.getRequestValue(request, "token"));
		return "resetPassword";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String resetAdminPasswordProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model, @PathVariable("companyId") String companyId) {
		String pass1 = this.getRequestValue(request, "pw");
		String pass2 = this.getRequestValue(request, "pwConfirm");
		String groupId = this.getRequestValue(request, "gid");
		if (pass1.equals(pass2)) {
			if (pass1.length() < 8) {
				model.addAttribute("errorMessage", "Password must be at least 8 characters.");
				model.addAttribute("action", "/pw/" + companyId + "/reset");
				model.addAttribute("groupId", groupId);
				return "resetPassword";
			}
			CompanyDataAccess dataAccess = new CompanyDataAccess();
			GroupDbObj group = dataAccess.getGroupById(groupId);
			if (!group.getCompanyId().equals(companyId)) {
				System.out.println("uh oh");
				return "resetPassword";
			}
			Encryption encryption = new Encryption();
			String salt = GeneralUtilities.randomString();
			String encPw = encryption.encryptPw(pass1, salt);
			group.setSalt(salt);
			group.setGroupPw(encPw);
			dataAccess.updateGroup(group);
			model.addAttribute("action", "/company");
			return "signinPage";
		} else {
			model.addAttribute("errorMessage", "Passwords did not match.");
			model.addAttribute("action", "/pw/" + companyId + "/reset");
			model.addAttribute("groupId", groupId);
			return "resetPassword";
		}
	}
}
