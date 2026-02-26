package com.humansarehuman.blue2factor.authentication.passe;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.humansarehuman.blue2factor.authentication.BaseController;
import com.humansarehuman.blue2factor.constants.Urls;

@Controller
@RequestMapping(Urls.CREATE_ENCRYPTION_KEY_COMPANY)
@SuppressWarnings("ucd")
public class CreateEncryptionKeys extends BaseController {
//    @RequestMapping(method = RequestMethod.GET)
//    public String processGet(HttpServletRequest request, HttpServletResponse httpResponse,
//            ModelMap model) {
//        BasicResponse response = new BasicResponse(Outcomes.FAILURE, "method not allowed");
//        model = this.addBasicResponse(model, response);
//        return "result";
//    }
//
//    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//    public @ResponseBody EncryptionKeyResponse processPost(
//            @RequestBody CreateKeyRequest createKeyRequest, HttpServletRequest request,
//            HttpServletResponse httpResponse, ModelMap model) {
//        int outcome = Outcomes.FAILURE;
//        String pvt = "";
//        String pub = "";
//        showAllRequestParameters(request);
//        CompanyDataAccess dataAccess = new CompanyDataAccess();
//        Encryption encryption = new Encryption();
//        String companyId = createKeyRequest.getCoId();
//        dataAccess.addLog("CreateEncryptionKeys", "companyId: " + companyId);
//        if (!TextUtils.isBlank(companyId)) {
//            CompanyDbObj company = dataAccess.getCompanyById(companyId);
//            if (company != null) {
//                try {
//                    dataAccess.addLog("CreateEncryptionKeys", "company found");
//                    KeyPair b2fServerKeyPair = encryption.generateNewRsaKey();
//                    encryption.printKeys(b2fServerKeyPair);
//                    PrivateKey b2fServerPrivateKey = b2fServerKeyPair.getPrivate();
//                    dataAccess.addLog("CreateEncryptionKeys",
//                            "Private key format: " + b2fServerPrivateKey.getFormat());
//                    String pvtKeyText = encryption.privateKeyToString(b2fServerPrivateKey);
//
//                    PublicKey b2fServerPublicKey = b2fServerKeyPair.getPublic();
//                    dataAccess.addLog("CreateEncryptionKeys",
//                            "Public key format: " + b2fServerPublicKey.getFormat());
//                    pub = encryption.publicKeyToString(b2fServerPublicKey);
//                    KeyDbObj key = new KeyDbObj("", "", companyId,
//                            KeyType.CLIENT_SERVER_PRIVATE_KEY, pvtKeyText, false, "RSA", "");
//                    dataAccess.deactivateOldClientKeysForCompany(companyId);
//                    dataAccess.addKey(key);
//
//                    KeyPair clientServerKeyPair = encryption.generateNewRsaKey();
//                    encryption.printKeys(clientServerKeyPair);
//                    PrivateKey clientServerPrivateKey = clientServerKeyPair.getPrivate();
////                    PublicKey clientServerPublicKey = clientServerKeyPair.getPublic();
////                    String pubKeyText = encryption.publicKeyToString(clientServerPublicKey);
//                    pvt = encryption.privateKeyToString(clientServerPrivateKey);
////                    KeyDbObj key2 = new KeyDbObj("", "", companyId,
////                            KeyType.CLIENT_SERVER_PUBLIC_KEY, pubKeyText, true, "RSA", "");
////                    dataAccess.addKey(key2);
//                    outcome = Outcomes.SUCCESS;
//                } catch (Exception e) {
//                    dataAccess.addLog("CreateEncryptionKeys", e);
//                    pub = e.getLocalizedMessage();
//                }
//            } else {
//                dataAccess.addLog("CreateEncryptionKeys", "company was null");
//                pub = "company was null";
//            }
//        } else {
//            pub = "companyId was null";
//        }
//        return new EncryptionKeyResponse(outcome, pvt);
//    }
}
