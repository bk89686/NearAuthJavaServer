package com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint;

import java.io.Serializable;

public class FingerprintAuth implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2335207156100122332L;

    private String id;
    private String type;
    private String rawId;
    private AuthResponse response;
    private String browserSession;
    private String reqUrl;

    public FingerprintAuth() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRawId() {
        return rawId;
    }

    public void setRawId(String rawId) {
        this.rawId = rawId;
    }

    public AuthResponse getResponse() {
        return response;
    }

    public void setResponse(AuthResponse response) {
        this.response = response;
    }

    public String getBrowserSession() {
        return browserSession;
    }

    public void setBrowserSession(String browserSession) {
        this.browserSession = browserSession;
    }

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    @Override
    public String toString() {
        return "FingerprintAuth [id=" + id + ", type=" + type + ", rawId=" + rawId + ", response="
                + response + ", b2fSession=" + browserSession + ", reqUrl=" + reqUrl + "]";
    }
}
