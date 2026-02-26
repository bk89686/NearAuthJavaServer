package com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse;

import com.humansarehuman.blue2factor.entities.BasicResponse;

public class ApiResponseWithToken extends ApiResponse {

    /**
     * 
     */
    private static final long serialVersionUID = 4589752083738163048L;
    protected String token;

    public ApiResponseWithToken() {
        super();
    }

    public ApiResponseWithToken(ApiResponse apiResponse, String token) {
        super(apiResponse.outcome, apiResponse.reason);
        this.token = token;
    }

    public ApiResponseWithToken(BasicResponse basicResponse) {
        this.outcome = basicResponse.getOutcome();
        this.reason = basicResponse.getReason();
        this.token = basicResponse.getToken();
    }

    public ApiResponseWithToken(int outcome, String reason, String token) {
        super(outcome, reason);
        this.token = token;
    }

    public ApiResponseWithToken(int outcome, String reason) {
        super(outcome, reason);
        this.token = "";
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
