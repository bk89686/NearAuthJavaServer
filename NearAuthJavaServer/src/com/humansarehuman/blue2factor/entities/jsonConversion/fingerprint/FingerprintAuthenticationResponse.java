package com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint;

import java.io.Serializable;

public class FingerprintAuthenticationResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1875210062998791989L;

    private String challenge;
    private int timeout;
    private String rpId;
    private AllowCredentials[] allowCredentials;
    private String userVerification;

    public FingerprintAuthenticationResponse(String challenge, int timeout, String rpId,
            AllowCredentials[] allowCredentials, String userVerification) {
        super();
        this.challenge = challenge;
        this.timeout = timeout;
        this.rpId = rpId;
        this.allowCredentials = allowCredentials;
        this.userVerification = userVerification;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getRpId() {
        return rpId;
    }

    public void setRpId(String rpId) {
        this.rpId = rpId;
    }

    public AllowCredentials[] getAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(AllowCredentials[] allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public String getUserVerification() {
        return userVerification;
    }

    public void setUserVerification(String userVerification) {
        this.userVerification = userVerification;
    }

}
