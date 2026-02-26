package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.GroupDataAccess;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

@Controller
@RequestMapping(Urls.DELETE_USER_DATA)
@SuppressWarnings("ucd")
public class DeleteUser extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String deleteUserProcessGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		TokenDbObj token = this.getPersistentTokenObj(request);
		boolean showForm = false;
		if (token != null) {
			GroupDataAccess dataAccess = new GroupDataAccess();
			GroupDbObj group = dataAccess.getActiveGroupFromToken(token);
			if (group != null) {
				showForm = true;
			} else {
				model.addAttribute("errorMessage", "oh crap. we couldn't find you.");
			}
		} else {
			model.addAttribute("errorMessage", "oh crap. we couldn't find you.");
		}
		model.addAttribute("showForm", showForm);
		return "deleteUserData";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String deleteUserProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		TokenDbObj token = this.getPersistentTokenObj(request);
		String nextPage = "error";
		boolean errorFound = true;
		if (token != null) {
			GroupDataAccess dataAccess = new GroupDataAccess();
			GroupDbObj group = dataAccess.getActiveGroupFromToken(token);
			if (group != null) {
				dataAccess.deactivateAllDevicesForGroup(group.getGroupId());
				group.setActive(false);
				dataAccess.updateGroup(group);
				token.setExpireTime(DateTimeUtilities.getCurrentTimestamp());
				errorFound = false;
			} else {
				model.addAttribute("errorMessage", "oh crap. we couldn't find you.");
			}
			nextPage = "deactivate";
		} else {
			model.addAttribute("errorMessage", "oh crap. we couldn't find you.");
		}
		model.addAttribute("errorFound", errorFound);
		model.addAttribute("showForm", false);
		return nextPage;
	}
}
