package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

public class TokenDbObj {
	private String groupId;
	private String deviceId;
	private String tokenId;
	private Timestamp authorizationTime;
	private Timestamp expireTime;
	private Timestamp lastCheck;
	private Timestamp lastUpdate;
	private String description;
	private Boolean needsUpdate;
	private String browserId;
	private int permission;
	private String baseUrl;

	public TokenDbObj(String groupId, String deviceId, String tokenId, Timestamp authorizationTime,
			Timestamp expireTime, Timestamp lastCheck, Timestamp lastUpdate, String description, Boolean needsUpdate,
			String browserId, int permission, String baseUrl) {
		super();
		this.groupId = groupId;
		this.deviceId = deviceId;
		this.tokenId = tokenId;
		this.authorizationTime = authorizationTime;
		this.expireTime = expireTime;
		this.lastCheck = lastCheck;
		this.lastUpdate = lastUpdate;
		this.description = description;
		this.needsUpdate = needsUpdate;
		this.browserId = browserId;
		this.permission = permission;
		this.baseUrl = baseUrl;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getBrowserId() {
		return browserId;
	}

	public void setBrowserId(String browserId) {
		this.browserId = browserId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public Timestamp getAuthorizationTime() {
		return authorizationTime;
	}

	public void setAuthorizationTime(Timestamp authorizationTime) {
		this.authorizationTime = authorizationTime;
	}

	public Timestamp getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Timestamp expireTime) {
		new DataAccess().addLog(this.deviceId, "expire time set to " + expireTime);
		this.expireTime = expireTime;
	}

	public boolean isExpired() {
		Timestamp now = DateTimeUtilities.getCurrentTimestamp();
		boolean expired = expireTime.before(now);
		long nowInt = now.getTime();
		long expireInt = expireTime.getTime();

		DataAccess dataAccess = new DataAccess();
		dataAccess.addLog("now: " + nowInt + ", expire: " + expireInt);
		dataAccess.addLog("isExpired: " + expired + " for " + tokenId + " expireTime: " + expireTime + " before " + now
				+ "? url: " + baseUrl);
		return expired;
	}

	public Timestamp getLastCheck() {
		return lastCheck;
	}

	public void setLastCheck(Timestamp lastCheck) {
		this.lastCheck = lastCheck;
	}

	public Timestamp getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getNeedsUpdate() {
		return needsUpdate;
	}

	public void setNeedsUpdate(Boolean needsUpdate) {
		this.needsUpdate = needsUpdate;
	}

	public int getPermission() {
		return permission;
	}

	public void setPermission(int permission) {
		this.permission = permission;
	}

}
