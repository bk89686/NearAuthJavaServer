package com.humansarehuman.blue2factor.authentication.api;

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

import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse.ApiResponseWithToken;
import com.humansarehuman.blue2factor.entities.tables.CheckDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.ServerDbObj;
import com.humansarehuman.blue2factor.entities.tables.TokenDbObj;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = Urls.PAM_SERVER_CHECK)
@SuppressWarnings("ucd")
public class PamServerCheck extends B2fApi {

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken getJson(HttpServletRequest request, HttpServletResponse httpResponse)
			throws IOException {
		ApiResponseWithToken response = new ApiResponseWithToken(Outcomes.API_F1_FAILURE, "Method not allowed", "");
		return response;
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ApiResponseWithToken pamServerCheckPostJson(HttpServletRequest request,
			HttpServletResponse httpResponse, ModelMap model) {
		CompanyDataAccess dataAccess = new CompanyDataAccess();
		dataAccess.addLog("start");
		String serverId = this.getRequestValue(request, "serverId");
		int outcome = Outcomes.FAILURE;
		String reason = "";
		String token = "";
		ServerDbObj server = dataAccess.getActiveServerByServerId(serverId);
		if (server != null) {
			dataAccess.addLog("serverFound");
			String clientId = this.getRequestValue(request, "clientId");
			DeviceDbObj device = dataAccess.getActiveDeviceByMachineId(clientId);
			String serverInstance = this.getRequestValue(request, "sInstance");
			String clientInstance = this.getRequestValue(request, "cInstance");
			if (device != null) {
				dataAccess.addLog("device Found");
				CheckDbObj check = dataAccess.getServerCheckForSsh(clientId, clientInstance, serverInstance);
				if (check != null) {
					dataAccess.addLog("check Found");
					if (dataAccess.isServerConnectionSetupBool(device, server)) {
						outcome = Outcomes.SUCCESS;
					} else {
						reason = Constants.SSL_CONNECTION_NOT_SETUP;
					}
				} else {
					reason = Constants.CHECK_NOT_FOUND;
				}
			} else {
				reason = Constants.DEVICE_NOT_FOUND;
			}
		} else {
			reason = Constants.SERVER_NOT_FOUND;
			outcome = Outcomes.INCOMPLETE;
		}
		if (outcome != Outcomes.SUCCESS) {
			dataAccess.addLog("failure reason: " + reason);
		}
		return new ApiResponseWithToken(outcome, reason, token);
	}

	ApiResponseWithToken calculateOutput(CompanyDataAccess dataAccess, ServerDbObj server,
			ApiResponseWithToken response) {
		try {
			Encryption encryption = new Encryption();
			String plainTextToken = GeneralUtilities.randomLetters(40);
			KeyDbObj pub = dataAccess.getServerSshPublicKey(server.getServerId());
			response.setToken(encryption.encryptWithPublicKey(pub, plainTextToken));
			KeyDbObj pvt = dataAccess.getCompanySshPrivateKey(server.getCompanyId());
			response.setReason(encryption.signStringForSsh(pvt, plainTextToken));
			if (TextUtils.isBlank(response.getToken()) || TextUtils.isBlank(response.getReason())) {
				response.setOutcome(Outcomes.FAILURE);
			}
		} catch (Exception e) {
			dataAccess.addLog("PamServerCheck", e);
			response.setReason(e.getLocalizedMessage());
			response.setOutcome(Outcomes.FAILURE);
		}
		return response;
	}

	/**
	 * Make sure the user has an SSH token that that is active
	 * 
	 * @param user
	 * @return
	 */
	ApiResponseWithToken validateSshUser(ServerDbObj server, String user, CompanyDataAccess dataAccess) {
		int outcome = Outcomes.INCOMPLETE;
		String reason = "";
		TokenDbObj token = dataAccess.getActiveSshTokenForUser(user);
		if (token != null) {
			dataAccess.addLog("token found");
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(token.getDeviceId());
			if (device != null) {
				if (device.isActive() && device.getSignedIn()) {
					dataAccess.addLog("device not null");
					if (dataAccess.isVeryRecentAccessAllowed(device)) {
						dataAccess.addLog("access allowed");
						outcome = Outcomes.SUCCESS;
					} else {
						outcome = this.handleNotProximate(device);
					}
				} else {
					reason = Constants.SIGNED_OUT;
				}
			} else {
				reason = "device not found for token";
			}
			dataAccess.expireSshTokensForUser(user, server.getCompanyId());
		} else {
			dataAccess.addLog("token not found");
			reason = "active token not found";
		}
		return new ApiResponseWithToken(outcome, reason, "");
	}

}
