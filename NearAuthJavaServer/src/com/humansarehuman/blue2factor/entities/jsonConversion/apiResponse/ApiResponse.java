package com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse;

import java.io.Serializable;

public class ApiResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8114339501424295973L;
    public int outcome;
    String reason;

    public ApiResponse() {
    }

    public ApiResponse(int outcome, String reason) {
        this.outcome = outcome;
        this.reason = reason;
    }

    public int getOutcome() {
        return outcome;
    }

    public void setOutcome(int outcome) {
        this.outcome = outcome;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
