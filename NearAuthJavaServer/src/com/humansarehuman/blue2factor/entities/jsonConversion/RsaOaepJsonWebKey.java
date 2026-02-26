package com.humansarehuman.blue2factor.entities.jsonConversion;

import java.io.Serializable;

public class RsaOaepJsonWebKey implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7422033749160185149L;
    private String alg;
    private String e;
    private Boolean ext;
    private String[] key_ops;
    private String kty;
    private String n;

    public RsaOaepJsonWebKey() {
    }

//    RsaOaepJsonWebKey(String alg, String e, Boolean ext, String[] key_ops, String kty, String n) {
//        super();
//        this.alg = alg;
//        this.e = e;
//        this.ext = ext;
//        this.key_ops = key_ops;
//        this.kty = kty;
//        this.n = n;
//    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public String getE() {
        return e;
    }

    public void setE(String e) {
        this.e = e;
    }

    public Boolean getExt() {
        return ext;
    }

    public void setExt(Boolean ext) {
        this.ext = ext;
    }

    public String[] getKey_ops() {
        return key_ops;
    }

    public void setKey_ops(String[] key_ops) {
        this.key_ops = key_ops;
    }

    public String getKty() {
        return kty;
    }

    public void setKty(String kty) {
        this.kty = kty;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

}
