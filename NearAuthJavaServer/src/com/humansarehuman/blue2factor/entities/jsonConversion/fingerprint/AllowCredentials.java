package com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint;

import java.io.Serializable;

public class AllowCredentials implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7490523987382192585L;
    private String id;
    private String type;
    private String[] transports;

    public AllowCredentials(String id, String type, String[] transports) {
        super();
        this.id = id;
        this.type = type;
        this.transports = transports;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getTransport() {
        return transports;
    }

    public void setTransport(String[] transport) {
        this.transports = transport;
    }

}
