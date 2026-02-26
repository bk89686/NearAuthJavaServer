package com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint;

import java.io.Serializable;

public class FingerprintRegistrationRequest implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -4419825074608167112L;
    private String browserSession;
    private String attestation;
    private AuthenticatorSelection authenticatorSelection;
    private String reqUrl;

    public String getBrowserSession() {
        return browserSession;
    }

    public void setBrowserSession(String browserSession) {
        this.browserSession = browserSession;
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

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

}
