package com.humansarehuman.blue2factor.authentication.api;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Parameters;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.GroupDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.ServerDbObj;
import com.humansarehuman.blue2factor.entities.tables.SshConnectionDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = Urls.PAM_SERVER_CHECK2)
@SuppressWarnings("ucd")
public class PamServerCheck2 extends B2fApi {
	private String delimeter = ":249#982!:";

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken pamServerCheck2Get(HttpServletRequest request,
			HttpServletResponse httpResponse) throws IOException {
		ApiResponseWithToken response = new ApiResponseWithToken(Outcomes.API_F1_FAILURE, "Method not allowed", "");
		return response;
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken pamServerCheck2PostJson(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		try {
			String serverId = this.getRequestValue(request, Parameters.SERVER_ID_FOR_SERVER);
			String encryptedText = this.getRequestValue(request, Parameters.ENCRYPTED_FOR_SERVER);
			String signature = this.getRequestValue(request, Parameters.SIGNATURE_FOR_SERVER);
			ServerDbObj server = dataAccess.getActiveServerByServerId(serverId);
			if (server != null) {
				dataAccess.addLog("serverFound");
				KeyDbObj privateKey = dataAccess.getKeyByTypeAndServerId(KeyType.SERVER_SSH_PRIVATE_KEY, serverId);
				if (privateKey != null) {
					Encryption encryption = new Encryption();
					String decrypted = encryption.decryptWithPrivateKey(privateKey, encryptedText);
					if (decrypted != null) {
						KeyDbObj publicKey = dataAccess.getKeyByTypeAndServerId(KeyType.SERVER_SSH_PUBLIC_KEY,
								serverId);
						if (encryption.verifyPostSshSignature(publicKey, decrypted, signature, dataAccess)) {
							BasicResponse basicResponse = this.matchServerWithDevice(decrypted, server, dataAccess);
							outcome = basicResponse.getOutcome();
							reason = basicResponse.getReason();
						} else {
							reason = Constants.SIGNATURE_VALIDATION_FAILED;
						}
					} else {
						reason = Constants.DECRYPTION_ERROR;
					}
				} else {
					reason = Constants.KEY_NOT_FOUND;
				}
			} else {
				reason = Constants.SERVER_NOT_FOUND;
			}
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		if (!reason.equals("")) {
			dataAccess.addLog("failure reason: " + reason);
		}
		ApiResponseWithToken response = new ApiResponseWithToken(outcome, reason, token);
		return response;
	}

	private BasicResponse matchServerWithDevice(String stringFromServer, ServerDbObj server,
			CompanyDataAccess dataAccess) {
		int outcome = Outcomes.FAILURE;
		String[] serverArray = stringFromServer.split(delimeter);
		String reason = "";
		if (serverArray.length > 4) {
			String remoteUser = serverArray[1];
			String remoteIp = serverArray[2];
			String localUser = serverArray[3];
			String localIp = serverArray[4];
			SshConnectionDbObj ssh = dataAccess.getMostRecentOpenSshConnection(remoteUser, remoteIp, localUser, localIp,
					server.getCompanyId());
			if (ssh != null) {
				long sshSecondsAgo = DateTimeUtilities.timestampSecondAgo(ssh.getCreateDate());
				dataAccess.addLog("last matching ssh was " + sshSecondsAgo + " seconds ago.");
				if (sshSecondsAgo < Constants.SSH_TIMEOUT) {
					DeviceDbObj device = dataAccess.getDeviceByDeviceId(ssh.getDeviceId());
					if (device != null) {
						if (dataAccess.isAccessAllowed(device, "matchServerWithDevice")) {
							outcome = Outcomes.SUCCESS;
						} else {
							reason = Constants.NOT_PERMITTED;
						}
					} else {
						reason = Constants.DEVICE_NOT_FOUND;
					}

				} else {
					ssh.setServerOutcome(Outcomes.FAILURE);
					if (sshSecondsAgo < Constants.SSH_NOT_THIS_TIME) {
						reason = Constants.TIMED_OUT;
					} else {
						reason = Constants.SSH_RECORD_NOT_FOUND;
					}
				}
				ssh.setCompletionDate(DateTimeUtilities.getCurrentTimestamp());
				ssh.setServerId(server.getServerId());
				ssh.setServerIpAddress(localIp);
				ssh.setServerOutcome(outcome);
				dataAccess.updateSshConnection(ssh);
			} else {
				CompanyDbObj company = dataAccess.getCompanyById(server.getCompanyId());
				if (company != null) {
					GroupDbObj group = dataAccess.getGroupByUid(localUser, server.getCompanyId());
					if (dataAccess.isPassThroughAllowed(company, group)) {
						outcome = Outcomes.SUCCESS;
					} else {
						reason = Constants.SSH_RECORD_NOT_FOUND;
					}
				} else {
					reason = Constants.COMPANY_NOT_FOUND;
				}
			}
		}

		return new BasicResponse(outcome, reason);
	}

}
