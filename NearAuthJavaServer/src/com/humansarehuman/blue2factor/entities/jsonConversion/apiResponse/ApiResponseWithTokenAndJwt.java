package com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse;

public class ApiResponseWithTokenAndJwt extends ApiResponseWithToken {
    /**
     * 
     */
    private static final long serialVersionUID = 1540904759204158775L;

    private String jwt;

    public ApiResponseWithTokenAndJwt() {
        super();
    }

    public ApiResponseWithTokenAndJwt(int outcome, String reason, String token, String jwt) {
        this.outcome = outcome;
        this.token = token;
        this.reason = reason;
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

}
