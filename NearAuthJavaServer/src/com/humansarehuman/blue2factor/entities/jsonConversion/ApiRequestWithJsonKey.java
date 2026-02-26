package com.humansarehuman.blue2factor.entities.jsonConversion;

public class ApiRequestWithJsonKey extends ApiF1F2Request {

    /**
     * 
     */
    private static final long serialVersionUID = 4654541129799051962L;
    private RsaOaepJsonWebKey jwk;
    private RsaOaepJsonWebPrivateKey jwpk;

    public ApiRequestWithJsonKey() {
    }

//    public ApiRequestWithJsonKey(String f1Token, String f2Token, String b2fSession, String coKey,
//            String cmd, RsaOaepJsonWebKey jwk, RsaOaepJsonWebPrivateKey jwpk) {
//        super();
//        this.f1Token = f1Token;
//        this.f2Token = f2Token;
//        this.b2fSession = b2fSession;
//        this.coKey = coKey;
//        this.cmd = cmd;
//        this.jwk = jwk;
//        this.jwpk = jwpk;
//    }

    public RsaOaepJsonWebKey getJwk() {
        return jwk;
    }

    public void setJwk(RsaOaepJsonWebKey jwk) {
        this.jwk = jwk;
    }

    public RsaOaepJsonWebPrivateKey getJwpk() {
        return jwpk;
    }

    public void setJwpk(RsaOaepJsonWebPrivateKey jwpk) {
        this.jwpk = jwpk;
    }

}
