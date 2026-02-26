package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

public class AuthorizationDbObj {
	public AuthorizationDbObj(String userId, Timestamp authorizationTime, boolean authorizationComplete,
			String requestingDevice, String centralDevice) {
		this.userId = userId;
		this.authorizationTime = authorizationTime;
		this.authorizationComplete = authorizationComplete;
		this.requestingDevice = requestingDevice;
		this.centralDevice = centralDevice;
	}
	
	private String userId;
	private Timestamp authorizationTime;
	private boolean authorizationComplete;
	private String requestingDevice;
	private String centralDevice;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Timestamp getAuthorizationTime() {
		return authorizationTime;
	}
	public void setAuthorizationTime(Timestamp authorizationTime) {
		this.authorizationTime = authorizationTime;
	}
	public boolean isAuthorizationComplete() {
		return authorizationComplete;
	}
	public void setAuthorizationComplete(boolean authorizationComplete) {
		this.authorizationComplete = authorizationComplete;
	}
	public String getRequestingDevice() {
		return requestingDevice;
	}
	public void setRequestingDevice(String requestingDevice) {
		this.requestingDevice = requestingDevice;
	}
	public String getCentralDevice() {
		return centralDevice;
	}
	public void setCentralDevice(String centralDevice) {
		this.centralDevice = centralDevice;
	}
	
	
}
