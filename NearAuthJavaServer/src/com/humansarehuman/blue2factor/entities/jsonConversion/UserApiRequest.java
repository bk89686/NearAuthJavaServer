package com.humansarehuman.blue2factor.entities.jsonConversion;

import java.io.Serializable;

public class UserApiRequest implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 761338319820728142L;
    private String coKey;
    private String cmd;
    private String uid;
    private String email;
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public String getCoKey() {
        return coKey;
    }

    public void setCoKey(String coKey) {
        this.coKey = coKey;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
