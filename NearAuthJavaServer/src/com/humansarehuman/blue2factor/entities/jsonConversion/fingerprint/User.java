package com.humansarehuman.blue2factor.entities.jsonConversion.fingerprint;

import java.io.Serializable;

public class User implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6289338939422475867L;
    private String displayName;
    private String id;
    private String name;

    public User(String displayName, String id, String name) {
        super();
        this.displayName = displayName;
        this.id = id;
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
