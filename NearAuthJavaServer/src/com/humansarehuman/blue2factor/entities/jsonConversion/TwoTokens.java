package com.humansarehuman.blue2factor.entities.jsonConversion;

import java.io.Serializable;

public class TwoTokens implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8031510249092672586L;
    String token1;
    String token2;
    String reason;
    Integer outcome;

    public TwoTokens(Integer outcome, String token1, String token2, String reason) {
        this.outcome = outcome;
        this.token1 = token1;
        this.token2 = token2;
        this.reason = reason;
    }

    public String getToken1() {
        return token1;
    }

    public void setToken1(String token1) {
        this.token1 = token1;
    }

    public String getToken2() {
        return token2;
    }

    public void setToken2(String token2) {
        this.token2 = token2;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getOutcome() {
        return outcome;
    }

    public void setOutcome(Integer outcome) {
        this.outcome = outcome;
    }

}
