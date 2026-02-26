package com.humansarehuman.blue2factor.entities;

import com.humansarehuman.blue2factor.entities.enums.OsClass;

/**
 * This should probably be restricted to peripherals, since centrals can have
 * multiple services and characteristics. But it's used from the iOs code in
 * central mode.
 */
public class BasicDeviceParameters {

	public BasicDeviceParameters(String deviceId, String serviceUuid, boolean isCentral, OsClass centralType,
			String characteristic, boolean previouslyConnected, boolean multiuser) {
		super();
		this.deviceId = deviceId;
		this.serviceUuid = serviceUuid;
		this.isCentral = isCentral;
		this.centralType = centralType;
		this.characteristic = characteristic;
		this.previouslyConnected = previouslyConnected;
		this.multiuser = multiuser;
	}

	private String deviceId;
	private String serviceUuid;
	private boolean isCentral;
	private OsClass centralType;
	private String characteristic;
	private boolean previouslyConnected;
	private boolean multiuser;

	public boolean isMultiuser() {
		return multiuser;
	}

	public void setMultiuser(boolean multiuser) {
		this.multiuser = multiuser;
	}

	public boolean isCentral() {
		return isCentral;
	}

	public void setCentral(boolean isCentral) {
		this.isCentral = isCentral;
	}

	public boolean isPreviouslyConnected() {
		return previouslyConnected;
	}

	public void setPreviouslyConnected(boolean previouslyConnected) {
		this.previouslyConnected = previouslyConnected;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getServiceUuid() {
		return serviceUuid;
	}

	public void setServiceUuid(String serviceUuid) {
		this.serviceUuid = serviceUuid;
	}

	public OsClass getCentralType() {
		return centralType;
	}

	public void setCentralType(OsClass centralType) {
		this.centralType = centralType;
	}

	public String getCharacteristic() {
		return characteristic;
	}

	public void setCharacteristic(String characteristic) {
		this.characteristic = characteristic;
	}

}
