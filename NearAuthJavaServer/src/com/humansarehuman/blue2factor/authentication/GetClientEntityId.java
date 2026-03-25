package com.humansarehuman.blue2factor.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.saml.SamlAndLdapResponse;
import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.utilities.Encryption;

@Controller
@RequestMapping(Urls.SAML_CLIENT_ENTITY_ID)
@SuppressWarnings("ucd")
public class GetClientEntityId extends SamlAndLdapResponse {

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	public String getClientEntityId(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model, @PathVariable("apiKey") String apiKey) {
		int outcome = Outcomes.FAILURE;
		int logLevel = LogConstants.TEMPORARILY_IMPORTANT;
		String entityId = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		CompanyDbObj company = dataAccess.getCompanyByApiKey(apiKey);

		String plainText = this.getRequestValue(request, "issuerIdVal");
		String signature = this.getRequestValue(request, "signature");
		String siteUrl = this.getRequestValue(request, "requester");
		dataAccess.addLog("plainText: " + plainText, logLevel);
		dataAccess.addLog("signature: " + signature, logLevel);
		if (company != null) {
			Encryption encryption = new Encryption();
			if (encryption.verifyWebServerSignature(company, plainText, signature)) {
				entityId = doesUrlMatchRegex(company, siteUrl, dataAccess);
				if (entityId == null) {
					dataAccess.addLog("url didn't match", LogConstants.WARNING);
					entityId = "regEx comparison failed";
				} else {
					outcome = Outcomes.SUCCESS;
				}
			} else {
				entityId = "signature not verified";
				dataAccess.addLog("signature failed", LogConstants.WARNING);
			}
		} else {
			entityId = "company not found";
			dataAccess.addLog(entityId, LogConstants.WARNING);
		}
		BasicResponse response = new BasicResponse(outcome, entityId);
		model = this.addBasicResponse(model, response);
		return "result";
	}
}
