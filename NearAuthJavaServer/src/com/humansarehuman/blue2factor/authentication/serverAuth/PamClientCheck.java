package com.humansarehuman.blue2factor.authentication.serverAuth;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.authentication.api.B2fApi;
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.ServerDbObj;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = Urls.PAM_CLIENT_CHECK)
@SuppressWarnings("ucd")
public class PamClientCheck extends B2fApi {

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken getJson(HttpServletRequest request, HttpServletResponse httpResponse)
			throws IOException {
		ApiResponseWithToken response = new ApiResponseWithToken(Outcomes.API_F1_FAILURE, "Method not allowed", "");
		return response;
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken postJson(HttpServletRequest request, HttpServletResponse httpResponse,
			ModelMap model) {
		int outcome = Outcomes.FAILURE;
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		String reason = "";
		String token = "";
		String clientToken = this.getRequestValue(request, "clientToken");
		String serverToken = this.getRequestValue(request, "serverToken");
		String encryptedString = this.getRequestValue(request, "encryptedString");
		String signedString = this.getRequestValue(request, "signedString");
		DeviceDbObj device = dataAccess.getDeviceByToken(clientToken, "PamClientCheck");
		if (device != null) {
			if (dataAccess.isAccessAllowed(device, "postJson")) {
				CompanyDbObj company = dataAccess.getCompanyByToken(serverToken, "PamClientCheck");
				if (company != null) {
					CompanyDbObj devCompany = dataAccess.getCompanyByDevId(device.getDeviceId());
					if (serverAndDeviceConnected(company, devCompany)) {
						dataAccess.addLog("PamClientCheck", "Server and device connected");
						String serverId = decryptAndCheckSignature(company, clientToken, serverToken, encryptedString,
								signedString);
						if (serverId != null) {
							dataAccess.addLog("PamClientCheck", "ServerID: " + serverId);
							String random = GeneralUtilities.randomString() + ":b2f";
							ServerDbObj server = dataAccess.getServerByServerId(serverId);
							KeyDbObj key = dataAccess.getServerSshPublicKey(server.getServerId());
							if (key != null) {
								dataAccess.addLog("PamClientCheck", "key found");
								try {
									Encryption encryption = new Encryption();
									token = encryption.encryptWithPublicKey(key, random);
									outcome = Outcomes.SUCCESS;
								} catch (Exception e) {
									reason = e.getLocalizedMessage();
									dataAccess.addLog("PamClientCheck", e);
								}
							} else {
								reason = Constants.KEY_NOT_FOUND;
							}
						} else {
							reason = Constants.SERVER_NOT_FOUND;
						}
					} else {
						reason = Constants.SIGNATURE_VALIDATION_FAILED;
					}
				} else {
					reason = Constants.COMPANY_NOT_FOUND;
				}
			} else {
				outcome = this.handleNotProximate(device);
			}
		} else {
			device = dataAccess.getDeviceByTokenIgnoringExpiration(clientToken, "PamClientCheck");
			if (device != null) {
				reason = Constants.TOKEN_EXPIRED;
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
			}
		}
		if (!TextUtils.isBlank(serverToken) && !TextUtils.isBlank(clientToken)) {
			dataAccess.expireTwoTokens(serverToken, clientToken);
		}
		ApiResponseWithToken response = new ApiResponseWithToken(outcome, reason, token);
		return response;
	}

	private String decryptAndCheckSignature(CompanyDbObj company, String clientToken, String serverToken,
			String encryptedString, String signature) {
		String serverId = null;
		DataAccess dataAccess = new DataAccess();
		Encryption encryption = new Encryption();
		// double check that we want to use the client Server Key and not ssh key
		String decrypted = encryption.decryptClientServerText(company, encryptedString).trim();
		if (decrypted != null && decrypted.length() > 20) {
			String[] splitDecryptedStrings = decrypted.split(":");
			if (splitDecryptedStrings.length == 2) {
				dataAccess.addLog("decryptAndCheckSignature",
						"comparing: '" + splitDecryptedStrings[0] + "' and '" + clientToken + "'");
				if (splitDecryptedStrings[0].equals(clientToken)) {
					if (encryption.verifySignature(company, serverToken, signature)) {
						serverId = splitDecryptedStrings[1];
					} else {
						dataAccess.addLog("decryptAndCheckSignature", "signature couldn't be verified");
					}
				} else {
					dataAccess.addLog("decryptAndCheckSignature", "clientTokens unequal");
				}
			} else {
				dataAccess.addLog("decryptAndCheckSignature", "string incorrectly formatted");
			}
		} else {
			dataAccess.addLog("decryptAndCheckSignature", "decryption failure");
		}
		return serverId;
	}

	private boolean serverAndDeviceConnected(CompanyDbObj company, CompanyDbObj devCompany) {
		boolean connected = false;
		if (company != null) {
			connected = company.getCompanyId().equals(devCompany.getCompanyId());
		}
		return connected;
	}
}
