package com.humansarehuman.blue2factor.authentication.serverAuth;

import java.sql.Timestamp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.humansarehuman.blue2factor.constants.Urls;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.entities.enums.KeyType;
import com.humansarehuman.blue2factor.entities.tables.KeyDbObj;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

@Controller
@RequestMapping(Urls.GET_PUBLIC_KEY)
public class PublicKeyRetrieval {
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public Object processGet(HttpServletRequest request, HttpServletResponse httpResponse,
            ModelMap model, @PathVariable("keyId") String keyId) {
        String keyText = "page not found";
        DataAccess dataAccess = new DataAccess();
        try {
            if (!TextUtils.isBlank(keyId)) {
                dataAccess.addLog("PublicKeyRetrieval", "keyId: " + keyId);
                KeyDbObj key = dataAccess.getKeyById(keyId);
                Timestamp now = DateTimeUtilities.getCurrentTimestamp();
                if (key != null && key.isActive() && key.getExpireDate().after(now)
                        && key.isPublicKey() && key.getKeyType() == KeyType.JWT) {
                    keyText = key.getKeyText();
                    dataAccess.addLog("PublicKeyRetrieval", "key: " + keyText);
                    dataAccess.addLog("PublicKeyRetrieval",
                            "key2" + GeneralUtilities.addNewLineEveryXCharacters(keyText, 64));
                } else {
                    if (key != null) {
                        Timestamp oneDayAgo = DateTimeUtilities.getCurrentTimestampMinusHours(24);
                        if (key.getExpireDate().before(oneDayAgo)) {
                            dataAccess.addLog("PublicKeyRetrieval", "key was expired");
                        } else {
                            if (key.isActive() && key.isPublicKey()) {
                                dataAccess.addLog("PublicKeyRetrieval",
                                        "use toString.equals: " + key.getKeyType());
                                keyText = key.getKeyText();
                            } else {
                                dataAccess.addLog("PublicKeyRetrieval",
                                        "key wasn't active or wasn't public");
                            }
                        }
                    } else {
                        dataAccess.addLog("PublicKeyRetrieval", "key was null");
                    }
                }
            } else {
                dataAccess.addLog("PublicKeyRetrieval", "keyId was blank");
            }
        } catch (Exception e) {
            dataAccess.addLog("PublicKeyRetrieval", e);
        }
        model.addAttribute("keyText", keyText);
        return "publicKey";
    }
}
