package com.humansarehuman.blue2factor.entities;

import java.sql.Timestamp;

public class ConnectionAttrs {
	private String service;
	private String characteristic;
	private String devName;
	private boolean connected;
	private String peripheralInstanceId;
	private String centralInstanceId;
	private String instanceId;
	private Timestamp idDate;
	private Boolean hasBle;
	private Boolean peripheralConnected;
	private Boolean centralConnected;
	private Boolean subscribed;
	private Timestamp lastSuccess;
	private String command;
	private String deviceClass;
	private String peripheralIdentifier;
	private boolean pushFailure;

	public ConnectionAttrs(String service, String characteristic, String devName, boolean connected,
			String centralInstanceId, String peripheralInstanceId, String instanceId, Timestamp idDate, Boolean hasBle,
			boolean peripheralConnected, boolean centralConnected, boolean subscribed, Timestamp lastSuccess,
			String command, String deviceClass, String peripheralIdentifier, boolean pushFailure) {
		super();
		this.service = service;
		this.characteristic = characteristic;
		this.devName = devName;
		this.connected = connected;
		this.centralInstanceId = centralInstanceId;
		this.peripheralInstanceId = peripheralInstanceId;
		this.instanceId = instanceId;
		this.idDate = idDate;
		this.hasBle = hasBle;
		this.peripheralConnected = peripheralConnected;
		this.centralConnected = centralConnected;
		this.subscribed = subscribed;
		this.lastSuccess = lastSuccess;
		this.command = command;
		this.deviceClass = deviceClass;
		this.peripheralIdentifier = peripheralIdentifier;
		this.pushFailure = pushFailure;
	}

	public boolean isPushFailure() {
		return pushFailure;
	}

	public void setPushFailure(boolean pushFailure) {
		this.pushFailure = pushFailure;
	}

	public String getPeripheralIdentifier() {
		return peripheralIdentifier;
	}

	public void setPeripheralIdentifier(String identifier) {
		this.peripheralIdentifier = identifier;
	}

	public String getDeviceClass() {
		return deviceClass;
	}

	public void setDeviceClass(String deviceClass) {
		this.deviceClass = deviceClass;
	}

	public void setService(String service) {
		this.service = service;
	}

	public void setCharacteristic(String characteristic) {
		this.characteristic = characteristic;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Boolean getPeripheralConnected() {
		return peripheralConnected;
	}

	public void setPeripheralConnected(Boolean peripheralConnected) {
		this.peripheralConnected = peripheralConnected;
	}

	public Boolean getCentralConnected() {
		return centralConnected;
	}

	public void setCentralConnected(Boolean centralConnected) {
		this.centralConnected = centralConnected;
	}

	public Boolean getSubscribed() {
		return subscribed;
	}

	public void setSubscribed(Boolean subscribed) {
		this.subscribed = subscribed;
	}

	public Boolean getHasBle() {
		return hasBle;
	}

	public void setHasBle(Boolean hasBle) {
		this.hasBle = hasBle;
	}

	public Timestamp getIdDate() {
		return idDate;
	}

	public void setIdDate(Timestamp idDate) {
		this.idDate = idDate;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String getService() {
		return service;
	}

	public String getCharacteristic() {
		return characteristic;
	}

	public String getDevName() {
		return devName;
	}

	public String getPeripheralInstanceId() {
		return peripheralInstanceId;
	}

	public void setPeripheralInstanceId(String peripheralInstanceId) {
		this.peripheralInstanceId = peripheralInstanceId;
	}

	public String getCentralInstanceId() {
		return centralInstanceId;
	}

	public void setCentralInstanceId(String centralInstanceId) {
		this.centralInstanceId = centralInstanceId;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public Timestamp getLastSuccess() {
		return lastSuccess;
	}

	public void setLastSuccess(Timestamp lastSuccess) {
		this.lastSuccess = lastSuccess;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

}
