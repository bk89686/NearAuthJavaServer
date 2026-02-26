package com.humansarehuman.blue2factor.entities;

import com.humansarehuman.blue2factor.entities.tables.DeviceDbObj;

public class SecondContact {
    private String centralId;
    private String peripheralId;
    private String instanceId;
    private String ssid;
    private String bssid;
    private String ipAddress;
    private boolean completeCheck;
    private DeviceDbObj centralDevice;
    private DeviceDbObj peripheralDevice;
    private int gmtOffset;

//	public SecondContact(String centralId, String peripheralId, String instanceId, String ssid,
//			String ipAddress, boolean completeCheck, int gmtOffset){
//		this.centralId = centralId;
//		this.peripheralId = peripheralId;
//		this.instanceId = instanceId;
//		this.ssid = ssid;
//		this.bssid = ipAddress;
//		this.ipAddress = ipAddress;
//		this.completeCheck = completeCheck;
//		this.gmtOffset = gmtOffset;
//	}

    public SecondContact(DeviceDbObj centralDevice, DeviceDbObj peripheralDevice, String instanceId,
            String ssid, String ipAddress, boolean completeCheck, int gmtOffset) {
        this.centralDevice = centralDevice;
        this.peripheralDevice = peripheralDevice;
        this.instanceId = instanceId;
        this.ssid = ssid;
        this.bssid = ipAddress;
        this.ipAddress = ipAddress;
        this.completeCheck = completeCheck;
        this.gmtOffset = gmtOffset;
    }

    public String getCentralId() {
        return centralId;
    }

    public void setCentralId(String centralId) {
        this.centralId = centralId;
    }

    public String getPeripheralId() {
        return peripheralId;
    }

    public void setPeripheralId(String peripheralId) {
        this.peripheralId = peripheralId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() {
        return "SecondContact [senderId=" + centralId + ", receiverId=" + peripheralId
                + ", instanceId=" + instanceId + ", ssid=" + ssid + ", bssid=" + bssid
                + ", ipAddress=" + ipAddress + ", completeCheck=" + completeCheck + "]";
    }

    public boolean isCompleteCheck() {
        return completeCheck;
    }

    public void setCompleteCheck(boolean completeCheck) {
        this.completeCheck = completeCheck;
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

    public int getGmtOffset() {
        return gmtOffset;
    }

    public void setGmtOffset(int gmtOffset) {
        this.gmtOffset = gmtOffset;
    }

}
