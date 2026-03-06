package com.humansarehuman.blue2factor.entities.jsonConversion;

import java.io.Serializable;

public class ApiRequest implements Serializable {
	private static final long serialVersionUID = 5973453771837231888L;
	
	private String encryptedSession;
	private String reqUrl;
	private String token;
	public ApiRequest(String encryptedSession, String reqUrl, String token) {
		super();
		this.encryptedSession = encryptedSession;
		this.reqUrl = reqUrl;
		this.token = token;
	}
	public String getEncryptedSession() {
		return encryptedSession;
	}
	public void setEncryptedSession(String encryptedSession) {
		this.encryptedSession = encryptedSession;
	}
	public String getReqUrl() {
		return reqUrl;
	}
	public void setReqUrl(String reqUrl) {
		this.reqUrl = reqUrl;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	

}
