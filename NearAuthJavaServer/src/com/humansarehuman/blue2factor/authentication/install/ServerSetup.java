package com.humansarehuman.blue2factor.authentication.install;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.entities.tables.ServerDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.Encryption;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

import io.jsonwebtoken.SignatureAlgorithm;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(Urls.SERVER_SETUP)
@SuppressWarnings("ucd")
public class ServerSetup extends BaseController {
	CompanyDataAccess dataAccess = new CompanyDataAccess();

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String serverSetupProcessPostServerSetup(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		String uuid = this.getRequestValue(request, "uuid");
		String purpose = this.getRequestValue(request, "purp");
		String hostname = this.getRequestValue(request, "host");
		dataAccess.addLog("purpose: '" + purpose + "'");
		if (purpose.equals("inst")) {
			String companyId = this.getRequestValue(request, "companyId");
			CompanyDbObj company = dataAccess.getCompanyByApiKey(companyId);
			String publicKeyStr = this.getRequestValue(request, "pubKey");// .replace(" ", "+");
			dataAccess.addLog("publicKey: " + publicKeyStr);
			model = serverSetup(model, uuid, hostname, publicKeyStr, company);
		} else if (purpose.equals("vali")) {
			String companyApi = this.getRequestValue(request, "cid");
			model = validateServer(model, uuid, companyApi);

		} else if (purpose.equals("dele")) {
			model = deleteServer(model, uuid);
		}
		return "result";
	}

	private ModelMap deleteServer(ModelMap model, String uuid) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		ServerDbObj server = dataAccess.getServerByServerId(uuid);
		if (server != null) {
			dataAccess.addLog(uuid, "server exists");
			server.setActive(false);
			dataAccess.updateServer(server);
			outcome = Outcomes.SUCCESS;
		} else {
			reason = "the server could not be found.";
		}
		BasicResponse basicResponse = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, basicResponse);
		return model;
	}

	private ModelMap validateServer(ModelMap model, String uuid, String companyApi) {
		int outcome = Outcomes.FAILURE;
		String reason = "";
		ServerDbObj server = dataAccess.getServerByServerId(uuid);
		CompanyDbObj company = dataAccess.getCompanyByApiKey(companyApi);
		if (server != null && company != null) {
			if (server.getCompanyId().equals(company.getCompanyId())) {
				if (server.isActive() && server.isEnabled()) {
					dataAccess.addLog(uuid, "server exists, is active and enabled");
					outcome = Outcomes.SUCCESS;
				} else {
					reason = "We could not verify that you typed the code into the admin console.";
				}
			} else {
				reason = "The company you entered the server under has different COMPANY_ID from"
						+ " the COMPANY_ID in your config file.";
			}
		} else {
			reason = "the server could not be found.";
		}
		BasicResponse basicResponse = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, basicResponse);
		return model;
	}

	private String addSshServerPrivateKey(ServerDbObj server) throws NoSuchAlgorithmException, InvalidKeySpecException {
		String publicKey = null;
		String companyId = server.getCompanyId();
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		if (!TextUtils.isEmpty(companyId)) {
			KeyDbObj coPrivateKey = dataAccess.createNewPrivateSshKey(server);
			if (coPrivateKey != null) {
				dataAccess.addLog("private Key now exists");
				Encryption encryption = new Encryption();
				publicKey = encryption.getPublicKeyStringFromPrivate(coPrivateKey);
			}
		}
		return publicKey;
	}

	private ModelMap serverSetup(ModelMap model, String uuid, String hostname, String publicKey, CompanyDbObj company) {
		int outcome = Outcomes.FAILURE;
		dataAccess.addLog("addServerAndShowPage");
		String reason = "";
		String token = "";
		if (!TextUtils.isBlank(uuid)) {
			ServerDbObj server = new ServerDbObj(DateTimeUtilities.getCurrentTimestamp(), uuid,
					GeneralUtilities.randomString(), 0, false, false, "None", company.getCompanyId(), hostname, "");
			dataAccess.addServer(server);
			dataAccess.addLog("serverAdded");
			try {
				token = addSshServerPrivateKey(server);
				dataAccess.addLog("token: " + token);
				if (addUploadedSshPublicKey(server, publicKey)) {
					outcome = Outcomes.SUCCESS;
				}
			} catch (Exception e) {
				reason = e.getLocalizedMessage();
				dataAccess.addLog(e);
			}
		} else {
			reason = "no uuid found";
		}

		BasicResponse basicResponse = new BasicResponse(outcome, reason, token);
		model = this.addBasicResponse(model, basicResponse);
		return model;
	}

//	private String generateAndSaveServerSshKeyPair(ServerDbObj server, String companyId)
//			throws NoSuchAlgorithmException {
//		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//		kpg.initialize(2048);
//		KeyPair kp = kpg.generateKeyPair();
//
//		String pubStr = Base64.getMimeEncoder().encodeToString(kp.getPublic().getEncoded());
//		String pvtStr = Base64.getMimeEncoder().encodeToString(kp.getPrivate().getEncoded());
//
//		String pubKeyStr = pubStr.replaceAll("(.{64})", "$1\n");
//		pubKeyStr = "-----BEGIN RSA PUBLIC KEY-----\n" + pubStr + "\n-----END RSA PUBLIC KEY-----\n";
//		dataAccess.addLog(server.getServerId(), "generateAndSaveServerKeyPair",
//				"pubKey: " + pubKeyStr + " for company: " + companyId);
//		KeyDbObj key = new KeyDbObj(GeneralUtilities.randomString(), server.getServerId(), null, null,
//				server.getCompanyId(), KeyType.SERVER_SSH_PRIVATE_KEY, pvtStr, false,
//				SignatureAlgorithm.RS256.getJcaName(), null, DateTimeUtilities.getCurrentTimestampPlusDays(3652));
//		dataAccess.addKey(key);
//		dataAccess.addLog(server.getServerId(), "generateAndSaveServerKeyPair", "key Saved");
//		return pubKeyStr;
//	}

//    private String generateAndSaveKeyPair(ServerDbObj server)
//            throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
//        Encryption encryption = new Encryption();
//        dataAccess.expireKeysByTypeAndDeviceId(KeyType.CLIENT_SSH_PRIVATE_KEY,
//                server.getServerId());
//        String publicKey = encryption.createAndSavePemKeyForLinux(server);
//        dataAccess.addLog(server.getServerId(), "generateAndSaveKeyPair", "keyPair Generated");
//        return publicKey;
//    }

	private boolean addUploadedSshPublicKey(ServerDbObj server, String publicKey) {
		boolean success = false;
		try {
			dataAccess.expireKeysByTypeAndDeviceId(KeyType.SERVER_SSH_PUBLIC_KEY, server.getServerId());
			KeyDbObj key = new KeyDbObj(server.getServerId(), null, null, server.getCompanyId(),
					KeyType.SERVER_SSH_PUBLIC_KEY, publicKey, true, SignatureAlgorithm.RS256.getJcaName(), null);
			dataAccess.addKey(key);
			success = true;
		} catch (Exception e) {
			dataAccess.addLog(e);
		}
		return success;
	}
}
