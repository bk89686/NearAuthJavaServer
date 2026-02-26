package com.humansarehuman.blue2factor.authentication.company;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.utilities.Encryption;

@Controller
@RequestMapping(value = { Urls.DOWNLOAD_CERT })
@SuppressWarnings("ucd")
public class DownloadCert {

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model,
			@PathVariable("companyID") String companyId) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		KeyDbObj key = dataAccess.getActiveKeyByTypeAndCompanyId(KeyType.IDP_CERT, companyId);
		if (key != null) {
			dataAccess.addLog("DownloadCert", "keyFound");
			Encryption encryption = new Encryption();

			String keyText = key.getKeyText().replace("-----BEGIN CERTIFICATE-----", "");
			keyText = keyText.replace("-----END CERTIFICATE-----", "");
			String fileText = encryption.addNewLinesToKeys(keyText);
			fileText = "-----BEGIN CERTIFICATE-----\n" + fileText + "-----END CERTIFICATE-----\n";
			model.addAttribute("metadata", fileText);
			dataAccess.addLog("DownloadCert", "writing stream with " + fileText);

		} else {
			dataAccess.addLog("cert not found");
		}
		return "samlMetadata";
	}

	/*
	 * 
	 * 
	 * URL oracle = new URL("http://www.example.com/file/download?"); BufferedReader
	 * in = new BufferedReader( new InputStreamReader(oracle.openStream()));
	 * 
	 * String inputLine; while ((inputLine = in.readLine()) != null)
	 * System.out.println(inputLine); in.close();
	 * 
	 */
}
