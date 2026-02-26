package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceConnectionDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;

/**
 * This clears log entries over 10 days old and resets the recent pushes.
 * 
 * @author cjm10
 *
 */

@Controller
@RequestMapping(Urls.DELETE_OLD_RECORDS)
@SuppressWarnings("ucd")
public class ResetRecentPushesCron extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String resetRecentPushesCron(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		try {
			DeviceConnectionDataAccess dataAccess = new DeviceConnectionDataAccess();
			dataAccess.resetRecentPushes();
			dataAccess.deleteOldRecords();
			dataAccess.addLog("deleting old records", LogConstants.WARNING);
			outcome = Outcomes.SUCCESS;
		} catch (Exception e) {
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
