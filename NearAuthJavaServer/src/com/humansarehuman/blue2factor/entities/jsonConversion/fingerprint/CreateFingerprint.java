package com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint;

import java.io.Serializable;

public class CreateFingerprint implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4503042068774154580L;
    private String id;
    private String rawId;
    private String type;
    private CreateResponse response;
    private String rpId;
    private String rpName;
    private String browserSession;
    private String reqUrl;

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    public String getBrowserSession() {
        return browserSession;
    }

    public void setBrowserSession(String browserSession) {
        this.browserSession = browserSession;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRawId() {
        return rawId;
    }

    public void setRawId(String rawId) {
        this.rawId = rawId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CreateResponse getResponse() {
        return response;
    }

    public void setResponse(CreateResponse response) {
        this.response = response;
    }

    public String getRpId() {
        return rpId;
    }

    public void setRpId(String rpId) {
        this.rpId = rpId;
    }

    public String getRpName() {
        return rpName;
    }

    public void setRpName(String rpName) {
        this.rpName = rpName;
    }

}
