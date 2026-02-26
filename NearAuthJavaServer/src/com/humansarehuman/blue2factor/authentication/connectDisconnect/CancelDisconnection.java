package com.humansarehuman.blue2factor.authentication.connectDisconnect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;

@Controller
@RequestMapping(Urls.CANCEL_DISCONNECTION)
@SuppressWarnings("ucd")
public class CancelDisconnection extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String cancelDisconnectionProcessPost(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String disconnectString = this.getEncryptedRequestValue(request, "chkqmkaeudgdkm33", key, iv);
			DataAccess dataAccess = new DataAccess();
			dataAccess.addLog("canceling disconnect for: " + disconnectString, LogConstants.DEBUG);
			if (dataAccess.cancelDisconnect(disconnectString)) {
				outcome = Outcomes.SUCCESS;
			} else {
				reason = "disconnectString not found";
			}
		} catch (Exception e) {
			reason = e.getMessage();
		}

		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
