package com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse;

import java.io.Serializable;

public class EncryptionKeyResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1975176706444431443L;
    private int outcome;
    private String pvt;
    private String pub;

    public EncryptionKeyResponse() {

    }

    public EncryptionKeyResponse(int outcome, String pvt, String pub) {
        super();
        this.outcome = outcome;
        this.pvt = pvt;
        this.pub = pub;
    }

    public EncryptionKeyResponse(int outcome, String pvt) {
        super();
        this.outcome = outcome;
        this.pvt = pvt;
        this.pub = null;
    }

    public int getOutcome() {
        return outcome;
    }

    public void setOutcome(int outcome) {
        this.outcome = outcome;
    }

    public String getPvt() {
        return pvt;
    }

    public void setPvt(String pvt) {
        this.pvt = pvt;
    }

    public String getPub() {
        return pub;
    }

    public void setPub(String pub) {
        this.pub = pub;
    }

}
