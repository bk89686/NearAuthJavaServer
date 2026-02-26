package com.humansarehuman.blue2factor.entities;

public class User {
    public User(String username, String email, String uid, int registeredDevices, int devicesInUse,
            String groupId, boolean setup, String userType) {
        this.username = username;
        this.email = email;
        this.uid = uid;
        this.registeredDevices = registeredDevices;
        this.devicesInUse = devicesInUse;
        this.groupId = groupId;
        this.setup = setup;
        this.userType = userType;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRegisteredDevices() {
        return registeredDevices;
    }

    public void setRegisteredDevices(int registeredDevices) {
        this.registeredDevices = registeredDevices;
    }

    public int getDevicesInUse() {
        return devicesInUse;
    }

    public void setDevicesInUse(int devicesInUse) {
        this.devicesInUse = devicesInUse;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isSetup() {
        return setup;
    }

    public void setSetup(boolean setup) {
        this.setup = setup;
    }

    private String username;
    private String email;
    private int registeredDevices;
    private int devicesInUse;
    private String groupId;
    private String uid;
    private boolean setup;
    private String userType;
}
