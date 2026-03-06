package com.humansarehuman.blue2factor.entities;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.humansarehuman.blue2factor.entities.enums.ConnectionType;

public class AccessAllowedWithAccessType {
	boolean accessAllowed;
	@JsonIgnore 
	ConnectionType connectionType;
	String authenticationMethod;
	Timestamp checkTime;
	String token;
	String reason;

	public AccessAllowedWithAccessType(boolean accessAllowed, ConnectionType connectionType, Timestamp checkTime) {
		super();
		this.accessAllowed = accessAllowed;
		this.connectionType = connectionType;
		this.authenticationMethod = connectionType.getStringName();
		this.checkTime = checkTime;
		this.token = "";
		this.reason = "";
	}
	
	public AccessAllowedWithAccessType(boolean accessAllowed, ConnectionType connectionType, Timestamp checkTime, String token,
			String reason) {
		super();
		this.accessAllowed = accessAllowed;
		this.connectionType = connectionType;
		this.authenticationMethod = connectionType.getStringName();
		this.checkTime = checkTime;
		this.token = token;
		this.reason = reason;
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
		return connectionType;
	}

	public void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
		this.authenticationMethod = connectionType.getStringName();
	}

	public ConnectionType getCheckType() {
		return connectionType;
	}

	public void setCheckType(ConnectionType checkType) {
		this.connectionType = checkType;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public String getAuthenticationMethod() {
		return authenticationMethod;
	}
	
}
