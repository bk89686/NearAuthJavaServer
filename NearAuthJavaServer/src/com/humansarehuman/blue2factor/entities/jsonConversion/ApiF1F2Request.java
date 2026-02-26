package com.humansarehuman.blue2factor.entities.jsonConversion;

import java.io.Serializable;

class ApiF1F2Request implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -7331767848179923935L;

    private String browserSession;
    private String browserToken;
    private String coKey;
    private String cmd;
    private String reqUrl;
    private boolean fromJs;

    public ApiF1F2Request() {
    }

//    ApiF1F2Request(String f1Token, String f2Token, String b2fSession, String coKey, String cmd,
//            String reqUrl) {
//        super();
//        this.f1Token = f1Token;
//        this.f2Token = f2Token;
//        this.b2fSession = b2fSession;
//        this.coKey = coKey;
//        this.cmd = cmd;
//        this.reqUrl = reqUrl;
//    }

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

//    public String getF1Token() {
//        return f1Token;
//    }
//
//    public void setF1Token(String f1Token) {
//        this.f1Token = f1Token;
//    }
//
//    public String getF2Token() {
//        return f2Token;
//    }
//
//    public void setF2Token(String f2Token) {
//        this.f2Token = f2Token;
//    }

    public String getBrowserToken() {
        return browserToken;
    }

    public void setBrowserToken(String browserToken) {
        this.browserToken = browserToken;
    }

    public String getCoKey() {
        return coKey;
    }

    public void setCoKey(String coKey) {
        this.coKey = coKey;
    }

    public String getBrowserSession() {
        return browserSession;
    }

    public void setBrowserSession(String browserSession) {
        this.browserSession = browserSession;
    }

    public boolean isFromJs() {
        return fromJs;
    }

    public void setFromJs(boolean fromJs) {
        this.fromJs = fromJs;
    }

}
