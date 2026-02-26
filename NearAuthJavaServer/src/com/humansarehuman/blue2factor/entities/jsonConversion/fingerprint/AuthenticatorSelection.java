package com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint;

import java.io.Serializable;

public class AuthenticatorSelection implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -8070502428933923774L;
    private String authenticatorAttachment;
    public String userVerification;
    private boolean requireResidentKey;

    public String getAuthenticatorAttachment() {
        return authenticatorAttachment;
    }

    public void setAuthenticatorAttachment(String authenticatorAttachment) {
        this.authenticatorAttachment = authenticatorAttachment;
    }

    public String getUserVerification() {
        return userVerification;
    }

    public void setUserVerification(String userVerification) {
        this.userVerification = userVerification;
    }

    public boolean isRequireResidentKey() {
        return requireResidentKey;
    }

    public void setRequireResidentKey(boolean requireResidentKey) {
        this.requireResidentKey = requireResidentKey;
    }

}
