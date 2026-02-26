package com.humansarehuman.blue2factor.entities.jsonConversion;

import java.io.Serializable;

public class CreateKeyRequest implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1249191162022833623L;
    private String coId;

    public String getCoId() {
        return coId;
    }

    public void setCoId(String coId) {
        this.coId = coId;
    }

}
