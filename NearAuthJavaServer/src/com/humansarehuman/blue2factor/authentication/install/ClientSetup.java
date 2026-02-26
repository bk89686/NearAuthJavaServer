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
import com.humansarehuman.blue2factor.constants.Constants;
import com.humansarehuman.blue2factor.constants.Outcomes;
import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.CompanyDataAccess;
import com.humansarehuman.blue2factor.dataAndAccess.DeviceDataAccess;
import com.humansarehuman.blue2factor.entities.BasicResponse;
import com.humansarehuman.blue2factor.entities.tables.CompanyDbObj;
import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.utilities.Encryption;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(Urls.CLIENT_SETUP)
@SuppressWarnings("ucd")
public class ClientSetup extends BaseController {
	CompanyDataAccess dataAccess = new CompanyDataAccess();

	@RequestMapping(method = RequestMethod.GET)
	public String processGet(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
		model = this.addBasicResponse(model, response);
		return "result";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String clientSetupProcessPost(HttpServletRequest request, HttpServletResponse httpResponse, ModelMap model) {
		String deviceId = this.getRequestValue(request, "deviceId");
		dataAccess.addLog(deviceId, "setting up client machine for ssh, devId: " + deviceId);
		model = clientSshSetup(model, deviceId);

		return "result";
	}

	private String addClientSshKey(DeviceDbObj device, CompanyDbObj company)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		String publicKey = null;
		String companyId = company.getCompanyId();
		DeviceDataAccess dataAccess = new DeviceDataAccess();
		if (!TextUtils.isEmpty(companyId)) {
			KeyDbObj coPrivateKey = dataAccess.getCompanyServerPrivateSshKey(companyId);
			if (coPrivateKey == null) {
				// publicKey = generateAndSaveClientSshKeyPair(device, companyId);
			} else {
				Encryption encryption = new Encryption();
				publicKey = encryption.getPublicKeyStringFromPrivate(coPrivateKey);
			}
		}
		return publicKey;
	}

	private ModelMap clientSshSetup(ModelMap model, String deviceId) {
		int outcome = Outcomes.FAILURE;
		dataAccess.addLog(deviceId, "start");
		String reason = "";
		if (!TextUtils.isBlank(deviceId)) {
			DeviceDbObj device = dataAccess.getDeviceByDeviceId(deviceId, "clientSshSetup");
			if (device != null) {
				dataAccess.addLog("device found");
				CompanyDbObj company = dataAccess.getCompanyByDevId(device.getDeviceId());
				if (company != null) {
					try {
						dataAccess.addLog("company found");
						reason = addClientSshKey(device, company);
						outcome = Outcomes.SUCCESS;
//                        if (addSshPublicKey(company, device, publicKey)) {
//                            outcome = Outcomes.SUCCESS;
//                        } else {
//                            dataAccess.addLog("clientSshSetup", "addKeyError");
//                            reason = Constants.KEY_ERROR;
//                        }
					} catch (Exception e) {
						reason = e.getLocalizedMessage();
						dataAccess.addLog(e);
					}
				} else {
					reason = Constants.COMPANY_NOT_FOUND;
				}
			} else {
				reason = Constants.DEVICE_ID_WAS_BLANK;
			}
		} else {
			reason = "deviceId was empty";
		}

		BasicResponse basicResponse = new BasicResponse(outcome, reason);
		model = this.addBasicResponse(model, basicResponse);
		return model;
	}

//	private String generateAndSaveClientSshKeyPair(DeviceDbObj device, String companyId)
//			throws NoSuchAlgorithmException {
//		Security.addProvider(new BouncyCastleProvider());
//		new JcaPEMKeyConverter().setProvider("BC");
//		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//		kpg.initialize(2048);
//		KeyPair kp = kpg.generateKeyPair();
//
//		String pubStr = Base64.getMimeEncoder().encodeToString(kp.getPublic().getEncoded());
//		String pvtStr = Base64.getMimeEncoder().encodeToString(kp.getPrivate().getEncoded());
//
//		String pubKeyStr = pubStr.replaceAll("(.{64})", "$1\n");
//		pubKeyStr = "-----BEGIN RSA PUBLIC KEY-----\n" + pubStr + "\n-----END RSA PUBLIC KEY-----\n";
//		dataAccess.addLog(device.getDeviceId(), "generateAndSaveServerKeyPair",
//				"pubKey: " + pubKeyStr + " for company: " + companyId);
//		KeyDbObj privateKey = new KeyDbObj("", "", "", companyId, KeyType.SERVER_SSH_PRIVATE_KEY, pvtStr, false, "RSA",
//				"");
//		dataAccess.addKey(privateKey);
//		dataAccess.addLog(device.getDeviceId(), "generateAndSaveServerKeyPair", "key Saved");
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

}
