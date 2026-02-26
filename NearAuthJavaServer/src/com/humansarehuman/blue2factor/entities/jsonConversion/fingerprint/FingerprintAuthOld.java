package com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint;

import java.io.Serializable;

public class FingerprintAuthOld implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2335207156100122332L;
    private String id;
    private String type;
    private String rawId;
    private String b2fSession;
    private AuthResponse response;
    private String reqUrl;

    public FingerprintAuthOld(String id, String type, String rawId, String b2fSession,
            AuthResponse response, String reqUrl) {
        super();
        this.id = id;
        this.type = type;
        this.rawId = rawId;
        this.b2fSession = b2fSession;
        this.response = response;
        this.reqUrl = reqUrl;
    }

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    public String getB2fSession() {
        return b2fSession;
    }

    public void setB2fSession(String b2fSession) {
        this.b2fSession = b2fSession;
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

}
