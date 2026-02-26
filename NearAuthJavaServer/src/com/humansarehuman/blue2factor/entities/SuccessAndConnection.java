package com.humansarehuman.blue2factor.entities;

import com.humansarehuman.blue2factor.entities.tables.DeviceConnectionDbObj;

public class SuccessAndConnection {
    private boolean success;
    private DeviceConnectionDbObj connection;

    public SuccessAndConnection(boolean success, DeviceConnectionDbObj connection) {
        this.success = success;
        this.connection = connection;
    }

    public boolean isSuccess() {
        return success;
    }

    public DeviceConnectionDbObj getConnection() {
        return connection;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setConnection(DeviceConnectionDbObj connection) {
        this.connection = connection;
    }

}
