package com.humansarehuman.blue2factor.authentication.lambda;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;

/**
 * the token was successfully uploaded to Lambda, so expire the old one
 * 
 * @author blue2factor
 *
 */
@Controller
@RequestMapping(Urls.LAMBDA_UPDATE_TOKEN)
public class UpdateTokenLambda extends B2fApi {
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.APPLICATION_JSON_VALUE)
	public String updateTokenLamdaPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			@PathVariable("apiKey") String apiKey) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = this.getRequestValue(request, Parameters.TOKEN);
		DataAccess dataAccess = new DataAccess();
		if (dataAccess.expireLambdaToken(token)) {
			outcome = Outcomes.SUCCESS;
		}
		BasicResponse basicResponse = new BasicResponse(outcome, reason);
		this.addBasicResponse(model, basicResponse);
		return "result";
	}
}