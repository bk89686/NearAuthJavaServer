package com.humansarehuman.blue2factor.entities.jsonConversion;

import java.io.Serializable;

public class SshVerificationData implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1437093046092483344L;
    String clientToken;
    String serverToken;
    String encryptedString;
    String signedString;

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getServerToken() {
        return serverToken;
    }

    public void setServerToken(String serverToken) {
        this.serverToken = serverToken;
    }

    public String getEncryptedString() {
        return encryptedString;
    }

    public void setEncryptedString(String encryptedString) {
        this.encryptedString = encryptedString;
    }

    public String getSignedString() {
        return signedString;
    }

    public void setSignedString(String signedString) {
        this.signedString = signedString;
    }

}
