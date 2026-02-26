package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.communication.Emailer;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;

@Controller
@RequestMapping(Urls.SEND_EMAIL)
@SuppressWarnings("ucd")
public class SendEmail extends BaseController {
	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String sendEmailProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		Emailer emailer = new Emailer();
		try {
			getVersion(request);
			String key = getKey(request);
			String iv = getInitVector(request);
			String toEmail = this.getEncryptedRequestValue(request, "apeofa9", key, iv);
			String messageCode;
			try {
				messageCode = this.getEncryptedRequestValue(request, "oe09i9", key, iv);
			} catch (Exception e) {
				messageCode = "";
			}
			String emailText = "";
			String subject = "";
			if (messageCode.equals(Constants.OSX)) {
				emailText = "Please use the link below to download NearAuth.ai's "
						+ "app on your computer: www.NearAuth.ai/macInstall.";
				subject = "Download the NearAuth.ai app on your Mac";
			} else if (messageCode.equals(Constants.WINDOWS)) {
				emailText = "Please use the link below to download NearAuth.ai's "
						+ "app on your computer: www.NearAuth.ai/windowsInstall.";
				subject = "Download the NearAuth.ai app on your Windows Computer";
			}
			if (emailer.email(toEmail, subject, emailText)) {
				outcome = Outcomes.SUCCESS;
			}
		} catch (Exception e) {
			new DataAccess().addLog(e);
			reason = e.getMessage();
		}
		BasicResponse response = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, response);
		return "result";
	}

}
