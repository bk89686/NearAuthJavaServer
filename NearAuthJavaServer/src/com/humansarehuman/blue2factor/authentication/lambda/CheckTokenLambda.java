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
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.Encryption;

@Controller
@RequestMapping(Urls.LAMBDA_CHECK_TOKEN)
public class CheckTokenLambda extends B2fApi {
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.APPLICATION_JSON_VALUE)
	public String checkTokenLamdaPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			@PathVariable("apiKey") String apiKey) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		String username = this.getRequestValue(request, Parameters.USER_NAME);
		String encryptedToken = this.getRequestValue(request, Parameters.TOKEN);
		String deviceId = this.getRequestValue(request, Parameters.DEVICE_ID);
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		CompanyDbObj company = dataAccess.getCompanyByApiKey(apiKey);
		if (company != null) {
			dataAccess.addLog("company found");
			GroupDbObj group = dataAccess.getGroupByEmailAndCompanyId(username, company.getCompanyId());
			if (group != null) {
				dataAccess.addLog("group found");
				KeyDbObj privateKey = dataAccess.getKeyByTypeAndDeviceId(KeyType.LAMBDA_PRIVATE_KEY, deviceId);
				if (privateKey != null) {
					dataAccess.addLog("privateKey found");
					Encryption encryption = new Encryption();
					String decryptedToken = encryption.decryptBasedWithOeapAndKey(encryptedToken, privateKey,
							dataAccess);
					if (decryptedToken != null) {
						TokenDbObj tokenObj = dataAccess.getToken(decryptedToken);
						if (tokenObj != null) {
							dataAccess.addLog("decryption worked");
							DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId);
							if (device != null) {
								dataAccess.addLog("device found");
								if (dataAccess.isAccessAllowed(device, "checkTokenLamdaPost")) {
									dataAccess.addLog("access allowed");
									outcome = Outcomes.SUCCESS;
								} else {
									reason = Constants.DEVICE_NOT_LOCAL;
									outcome = this.handleNotProximate(group, device);
								}
								token = encryption.encryptForLambda(group, deviceId, dataAccess);
							}
						} else {
							reason = Constants.TOKEN_NOT_FOUND;
						}
					} else {
						reason = Constants.DECRYPTION_ERROR;
					}
				} else {
					reason = Constants.KEY_NOT_FOUND;
				}
			} else {
				reason = Constants.USER_NOT_FOUND;
			}
		} else {
			reason = Constants.COMPANY_NOT_FOUND;
		}
		BasicResponse basicResponse = new BasicResponse(outcome, reason, token);
		this.addBasicResponse(model, basicResponse);
		return "result";
	}
}
