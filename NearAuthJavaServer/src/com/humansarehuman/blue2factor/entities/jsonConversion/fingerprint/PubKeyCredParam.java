package com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint;

import java.io.Serializable;

public class PubKeyCredParam implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5067021751412775474L;
    private int alg;
    private String type;

    public PubKeyCredParam(int alg, String type) {
        super();
        this.alg = alg;
        this.type = type;
    }

    public int getAlg() {
        return alg;
    }

    public void setAlg(int alg) {
        this.alg = alg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
