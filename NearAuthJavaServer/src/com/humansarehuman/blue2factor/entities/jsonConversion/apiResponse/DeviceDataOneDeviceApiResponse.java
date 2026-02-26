package com.humansarehuman.blue2factor.entities.jsonConversion.apiResponse;

import java.util.ArrayList;

import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

public class DeviceDataOneDeviceApiResponse extends ApiResponse {
	private static final long serialVersionUID = 1918116665249584219L;
	ArrayList<DeviceDataOneDevice> deviceDataOneDevices = new ArrayList<>();
	String deviceType;
	String deviceId;
	String connectionId;
	String currentTime;
	String centralType;

	public DeviceDataOneDeviceApiResponse(String deviceId) {
		super();
		this.currentTime = DateTimeUtilities.currentTimestampToReadableAltWithNyTimezone();
		this.deviceId = deviceId;
		this.connectionId = "";
		this.deviceType = "";
		this.centralType = "";
	}

	public DeviceDataOneDeviceApiResponse(String deviceType, String centralType, String deviceId, String connectionId) {
		super();
		this.currentTime = DateTimeUtilities.currentTimestampToReadableAltWithNyTimezone();
		this.deviceType = deviceType;
		this.deviceId = deviceId;
		this.connectionId = connectionId;
		this.centralType = centralType;
	}

	public DeviceDataOneDeviceApiResponse(ArrayList<DeviceDataOneDevice> deviceDataOneDevices) {
		super();
		this.deviceDataOneDevices = deviceDataOneDevices;
		this.currentTime = DateTimeUtilities.currentTimestampToReadableAltWithNyTimezone();
	}

	public DeviceDataOneDeviceApiResponse(DeviceDataOneDevice deviceDataOneDevice) {
		super();
		this.deviceDataOneDevices = new ArrayList<DeviceDataOneDevice>();
		this.deviceDataOneDevices.add(deviceDataOneDevice);
		this.currentTime = DateTimeUtilities.currentTimestampToReadableAltWithNyTimezone();
	}

	public void addDevice(DeviceDataOneDevice deviceDataOneDevice) {
		this.deviceDataOneDevices.add(0, deviceDataOneDevice);
	}

	public ArrayList<DeviceDataOneDevice> getDeviceDataOneDevices() {
		return this.deviceDataOneDevices;
	}

	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

	public void setDeviceData(ArrayList<DeviceDataOneDevice> deviceDataOneDevices) {
		this.deviceDataOneDevices = deviceDataOneDevices;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	public void setDeviceDataOneDevices(ArrayList<DeviceDataOneDevice> deviceDataOneDevices) {
		this.deviceDataOneDevices = deviceDataOneDevices;
	}

	public String getCentralType() {
		return centralType;
	}

	public void setCentralType(String centralType) {
		this.centralType = centralType;
	}

}
