package com.humansarehuman.blue2factor.entities;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

public class DeviceAndInstanceId {
    DeviceDbObj centralDevice;
    DeviceDbObj peripheralDevice;
    String instanceId;
    Timestamp expireDate;

    public DeviceAndInstanceId(DeviceDbObj centralDevice, DeviceDbObj peripheralDevice,
            String instanceId, Timestamp expireDate) {
        super();
        this.centralDevice = centralDevice;
        this.peripheralDevice = peripheralDevice;
        this.instanceId = instanceId;
        this.expireDate = expireDate;
    }

    public DeviceDbObj getCentralDevice() {
        return centralDevice;
    }

    public void setCentralDevice(DeviceDbObj centralDevice) {
        this.centralDevice = centralDevice;
    }

    public DeviceDbObj getPeripheralDevice() {
        return peripheralDevice;
    }

    public void setPeripheralDevice(DeviceDbObj peripheralDevice) {
        this.peripheralDevice = peripheralDevice;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Timestamp getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Timestamp expireDate) {
        this.expireDate = expireDate;
    }

}
