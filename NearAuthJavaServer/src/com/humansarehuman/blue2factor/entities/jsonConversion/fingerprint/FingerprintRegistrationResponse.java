package com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint;

import java.io.Serializable;

public class FingerprintRegistrationResponse implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -5711174977029014694L;
    private String attestation;
    private AuthenticatorSelection authenticatorSelection;
    private String challenge;
    private RequestCredential[] excludeCredentials;
    private PubKeyCredParam[] pubKeyCredParams;
    private Rp rp;
    private int timeout;
    private User user;

    public FingerprintRegistrationResponse(String attestation,
            AuthenticatorSelection authenticatorSelection, String challenge,
            RequestCredential[] excludeCredentials, PubKeyCredParam[] pubKeyCredParams, Rp rp,
            int timeout, User user) {
        super();
        this.attestation = attestation;
        this.authenticatorSelection = authenticatorSelection;
        this.challenge = challenge;
        this.excludeCredentials = excludeCredentials;
        this.pubKeyCredParams = pubKeyCredParams;
        this.rp = rp;
        this.timeout = timeout;
        this.user = user;
    }

    public String getAttestation() {
        return attestation;
    }

    public void setAttestation(String attestation) {
        this.attestation = attestation;
    }

    public AuthenticatorSelection getAuthenticatorSelection() {
        return authenticatorSelection;
    }

    public void setAuthenticatorSelection(AuthenticatorSelection authenticatorSelection) {
        this.authenticatorSelection = authenticatorSelection;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public RequestCredential[] getExcludeCredentials() {
        return excludeCredentials;
    }

    public void setExcludeCredentials(RequestCredential[] excludeCredentials) {
        this.excludeCredentials = excludeCredentials;
    }

    public PubKeyCredParam[] getPubKeyCredParams() {
        return pubKeyCredParams;
    }

    public void setPubKeyCredParams(PubKeyCredParam[] pubKeyCredParams) {
        this.pubKeyCredParams = pubKeyCredParams;
    }

    public Rp getRp() {
        return rp;
    }

    public void setRp(Rp rp) {
        this.rp = rp;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
