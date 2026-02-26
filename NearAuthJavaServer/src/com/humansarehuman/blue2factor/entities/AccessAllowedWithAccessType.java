package com.humansarehuman.blue2factor.entities;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.entities.enums.ConnectionType;

public class AccessAllowedWithAccessType {
	boolean accessAllowed;
	ConnectionType checkType;
	Timestamp checkTime;

	public AccessAllowedWithAccessType(boolean accessAllowed, ConnectionType checkType, Timestamp checkTime) {
		super();
		this.accessAllowed = accessAllowed;
		this.checkType = checkType;
		this.checkTime = checkTime;
	}

	public Timestamp getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(Timestamp checkTime) {
		this.checkTime = checkTime;
	}

	public boolean isAccessAllowed() {
		return accessAllowed;
	}

	public void setAccessAllowed(boolean accessAllowed) {
		this.accessAllowed = accessAllowed;
	}

	public ConnectionType getConnectionType() {
		return checkType;
	}

	public void setConnectionType(ConnectionType checkType) {
		this.checkType = checkType;
	}

}
