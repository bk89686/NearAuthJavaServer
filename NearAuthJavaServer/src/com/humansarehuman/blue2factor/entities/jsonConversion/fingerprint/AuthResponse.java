package com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint;

import java.io.Serializable;

public class AuthResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7636619455790942812L;
    private String authenticatorData;
    private String clientDataJSON;
    private String signature;
    private String userHandle;
//    private String clientExtensionJSON;

    public AuthResponse() {

    }

    public AuthResponse(String authenticatorData, String clientDataJSON, String signature,
            String userHandle) {
        super();
        this.authenticatorData = authenticatorData;
        this.clientDataJSON = clientDataJSON;
        this.signature = signature;
        this.userHandle = userHandle;
//        this.clientExtensionJSON = clientExtensionJSON;
    }

//    public String getClientExtensionJSON() {
//        return clientExtensionJSON;
//    }
//
//    public void setClientExtensionJSON(String clientExtensionJSON) {
//        this.clientExtensionJSON = clientExtensionJSON;
//    }

    public String getAuthenticatorData() {
        return authenticatorData;
    }

    public void setAuthenticatorData(String authenticatorData) {
        this.authenticatorData = authenticatorData;
    }

    public String getClientDataJSON() {
        return clientDataJSON;
    }

    public void setClientDataJSON(String clientDataJson) {
        this.clientDataJSON = clientDataJson;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getUserHandle() {
        return userHandle;
    }

    public void setUserHandle(String userHandle) {
        this.userHandle = userHandle;
    }

}
