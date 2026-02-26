package com.humansarehuman.blue2factor.authentication.lambda;

import java.sql.Timestamp;

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
import com.humansarehuman.blue2factor.entities.enums.TokenDescription;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.LAMBDA_CREATE_TOKEN)
public class CreateTokenLambda extends B2fApi {
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.APPLICATION_JSON_VALUE)
	public String createTokenLamdaPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			@PathVariable("apiKey") String apiKey) {
		int outcome = Outcomes.FAILURE;
		String username = this.getRequestValue(request, Parameters.USER_NAME);
		String pubKey = this.getRequestValue(request, Parameters.PUBLIC_KEY);
		String deviceId = this.getRequestValue(request, Parameters.DEVICE_ID);
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("deviceId: " + deviceId);
		CompanyDbObj company = dataAccess.getCompanyByApiKey(apiKey);
		GroupDbObj group = dataAccess.getGroupByEmailAndCompanyId(username, company.getCompanyId());
		DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId, "createTokenLamdaPost");
		String tokenStr = "";
		String newPubKeyString;
		if (device != null) {
			if (group != null) {
				if (device.getGroupId().equals(group.getGroupId())) {
					newPubKeyString = createLambdaKeys(dataAccess, company, group, deviceId, pubKey);
					if (newPubKeyString != null) {
						outcome = Outcomes.SUCCESS;
						tokenStr = GeneralUtilities.randomString();
						Timestamp tenYears = DateTimeUtilities.getCurrentTimestampPlusDays(3652);
						dataAccess.addTokenWithId(group.getGroupId(), deviceId, null, tokenStr, TokenDescription.LAMBDA,
								0, null, tenYears);
					} else {
						newPubKeyString = Constants.KEY_ERROR;
					}
				} else {
					newPubKeyString = Constants.DEVICE_ID_MISMATCH;
				}
			} else {
				newPubKeyString = Constants.USER_NOT_FOUND;
			}
		} else {
			newPubKeyString = Constants.DEVICE_NOT_FOUND;
		}
		BasicResponse basicResponse = new BasicResponse(outcome, newPubKeyString, tokenStr);
		this.addBasicResponse(model, basicResponse);
		return "result";
	}

	private String createLambdaKeys(CompanyDataAccess dataAccess, CompanyDbObj company, GroupDbObj group,
			String deviceId, String pubKey) {
		String newPublicKey = null;
		if (group.getCompanyId().equals(company.getCompanyId())) {
			dataAccess.expireKeysByTypeAndDeviceId(KeyType.LAMBDA_PRIVATE_KEY, deviceId);
			KeyDbObj key = new KeyDbObj(deviceId, null, group.getGroupId(), company.getCompanyId(),
					KeyType.LAMBDA_PUBLIC_KEY, pubKey, true, null, null);
			dataAccess.addKey(key);
			Encryption encryption = new Encryption();
			try {
				newPublicKey = encryption.createAndSaveKeyForLambda(company, group, deviceId, dataAccess);
			} catch (Exception e) {
				dataAccess.addLog(e);
			}
		}
		return newPublicKey;
	}
}
