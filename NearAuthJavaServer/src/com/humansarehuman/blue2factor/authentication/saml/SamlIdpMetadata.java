package com.humansarehuman.blue2factor.authentication.saml;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.utilities.saml.Saml;

/**
 * Used when B2f is acting as the IDP Should be passed along to the SP
 * 
 * @author cjm10
 *
 */

@Controller
@RequestMapping(Urls.SAML_IDP_METADATA)
@SuppressWarnings("ucd")
public class SamlIdpMetadata {
	@RequestMapping(method = RequestMethod.GET)
	public String samlMetadataProcessGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			@PathVariable("apiKey") String apiKey) {
		DataAccess dataAccess = new DataAccess();
		try {
			CompanyDbObj company = new CompanyDataAccess().getCompanyByApiKey(apiKey);
			if (company != null) {
				dataAccess.addLog("companyFound");
				String metadata = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
						+ new Saml().buildIdpEntityDescriptor(company);
				model.addAttribute("metadata", metadata);// .replace(">", ">\r\n"));
			} else {
				dataAccess.addLog("company not found");
			}
		} catch (Exception e) {
			new DataAccess().addLog("SamlIdpMetadata", e);
		}
		return "samlMetadata";
	}
}