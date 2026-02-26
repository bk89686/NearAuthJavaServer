package com.humansarehuman.blue2factor.entities.jsonConversion;

public class RsaOaepJsonWebPrivateKey extends RsaOaepJsonWebKey {
    /**
     * 
     */
    private static final long serialVersionUID = 7981968849222073920L;
    private String d;
    private String dp;
    private String dq;
    private String p;
    private String q;
    private String qi;

    public RsaOaepJsonWebPrivateKey() {
    }

//    public RsaOaepJsonWebPrivateKey(String alg, String d, String dp, String dq, String e,
//            Boolean ext, String[] key_ops, String kty, String n, String p, String q, String qi) {
//        super(alg, e, ext, key_ops, kty, n);
//        this.setAlg(alg);
//        this.d = d;
//        this.dp = dp;
//        this.dq = dq;
//        this.setE(e);
//        this.setExt(ext);
//        this.setKey_ops(key_ops);
//        this.setKty(kty);
//        this.setN(n);
//        this.p = p;
//        this.q = q;
//        this.qi = qi;
//    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getDq() {
        return dq;
    }

    public void setDq(String dq) {
        this.dq = dq;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getQi() {
        return qi;
    }

    public void setQi(String qi) {
        this.qi = qi;
    }

}
