package com.humansarehuman.blue2factor.authentication.install;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.GroupDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;

@Controller
@RequestMapping(Urls.IS_TEXT_ALLOWED)
@SuppressWarnings("ucd")
public class IsTextAllowed extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String isTextAllowedProcessGet(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String isTextAllowedProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		GroupDataAccess dataAccess = new GroupDataAccess();
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String groupId = this.getEncryptedRequestValue(request, Parameters.IS_TEXT_ALLOWED_GROUP_ID, key, iv);
			GroupDbObj group = dataAccess.getGroupById(groupId);
			if (group != null) {
				if (group.isTextAllowed()) {
					outcome = Outcomes.SUCCESS;
				} else {
					reason = Constants.TEXTS_NOT_ALLOWED;
				}
			} else {
				reason = Constants.GROUP_NOT_FOUND;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
			reason = e.getLocalizedMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
