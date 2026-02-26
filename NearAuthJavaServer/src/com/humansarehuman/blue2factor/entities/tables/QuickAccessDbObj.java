package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

public class QuickAccessDbObj {
	private String quickCheckId;
	private Timestamp lastUpdate;
	private String deviceId;
	private Boolean connected;
	private String method;

	public QuickAccessDbObj(String quickCheckId, String deviceId, Boolean connected, Timestamp lastUpdate,
			String method) {
		super();
		this.quickCheckId = quickCheckId;
		this.lastUpdate = lastUpdate;
		this.deviceId = deviceId;
		this.connected = connected;
		this.method = method;
	}

	public Timestamp getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Boolean getConnected() {
		return connected;
	}

	public void setConnected(Boolean connected) {
		this.connected = connected;
	}

	public String getQuickCheckId() {
		return quickCheckId;
	}

	public void setQuickCheckId(String quickCheckId) {
		this.quickCheckId = quickCheckId;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

}
