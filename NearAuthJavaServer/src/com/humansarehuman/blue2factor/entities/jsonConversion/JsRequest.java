package com.humansarehuman.blue2factor.entities.jsonConversion;

import java.io.Serializable;

import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;

public class JsRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3465233568461205329L;
	private String encryptedSession;
	private String token;
	private String reqType;
	private String userAgent;
	private String reqUrl;
	private String deviceId;
	private String companyIdFromUrl;
	private boolean fromBrowser;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public boolean isFromBrowser() {
		return fromBrowser;
	}

	public void setFromBrowser(boolean fromBrowser) {
		this.fromBrowser = fromBrowser;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getReqType() {
		return reqType;
	}

	public void setRequestType(String reqType) {
		this.reqType = reqType;
	}

	public String getReqUrl() {
		return reqUrl;
	}

	public void setReqUrl(String reqUrl) {
		this.reqUrl = reqUrl;
	}

	public String getEncryptedSession() {
		new DataAccess().addLog("encryptedSession: " + encryptedSession);
		return encryptedSession;
	}

	public void setEncryptedSession(String encryptedSession) {
		this.encryptedSession = encryptedSession;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getCompanyIdFromUrl() {
		return companyIdFromUrl;
	}

	public void setCompanyIdFromUrl(String companyIdFromUrl) {
		this.companyIdFromUrl = companyIdFromUrl;
	}

}
