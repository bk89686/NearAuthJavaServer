package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.constants.LogConstants;
import com.humansarehuman.blue2factor.dataAndAccess.DataAccess;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;

public class AccessCodeDbObj {
//    String tempStrings = "CREATE TABLE IF NOT EXISTS B2F_ACCESS_STRINGS ("
//            + "CREATE_DATE timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP, "
//            + "ACCESS_CODE varchar(255) DEFAULT NULL" + "SERVER_ID varchar(255) DEFAULT NULL, "
//            + "DEVICE_ID varchar(255) DEFAULT NULL, " + "PERMISSIONS int DEFAULT 0,"
//            + "ACTIVE tinyint(1) NOT NULL DEFAULT 1, " + ") ENGINE=InnoDB DEFAULT CHARSET=latin1";
	private Timestamp createDate;
	private String accessCode;
	private String companyId;
	private boolean oneTimeAccess;
	private String serverId;
	private String deviceId;
	private int permissions;
	private boolean active;
	private String browserId;

	public AccessCodeDbObj(Timestamp createDate, String accessCode, String companyId, String serverId, String deviceId,
			int permissions, boolean active, String browserId, boolean oneTimeAccess) {
		super();
		this.createDate = createDate;
		this.accessCode = accessCode;
		this.companyId = companyId;
		this.serverId = serverId;
		this.deviceId = deviceId;
		this.permissions = permissions;
		this.active = active;
		this.browserId = browserId;
		this.oneTimeAccess = oneTimeAccess;
		new DataAccess().addLog("new accessCode: " + accessCode + ", with browser, active = " + active);
	}

	public AccessCodeDbObj(String accessCode, String companyId, String serverId, String deviceId, int permissions,
			boolean active, String browserId, boolean oneTimeAccess) {
		super();
		this.createDate = DateTimeUtilities.getCurrentTimestamp();
		this.accessCode = accessCode;
		this.companyId = companyId;
		this.serverId = serverId;
		this.deviceId = deviceId;
		this.permissions = permissions;
		this.active = active;
		this.browserId = browserId;
		this.oneTimeAccess = oneTimeAccess;
		new DataAccess().addLog("new accessCode, active = " + active);
	}

	public AccessCodeDbObj(Timestamp createDate, String accessCode, String companyId, String serverId, String deviceId,
			int permissions, boolean active, boolean oneTimeAccess) {
		super();
		this.createDate = createDate;
		this.accessCode = accessCode;
		this.companyId = companyId;
		this.serverId = serverId;
		this.deviceId = deviceId;
		this.permissions = permissions;
		this.active = active;
		this.oneTimeAccess = oneTimeAccess;
		new DataAccess().addLog("new accessCode: " + accessCode + ", active = " + active);
	}

	public AccessCodeDbObj(String accessCode, String companyId, String serverId, String deviceId, int permissions,
			boolean active, boolean oneTimeAccess) {
		super();
		this.createDate = DateTimeUtilities.getCurrentTimestamp();
		this.accessCode = accessCode;
		this.companyId = companyId;
		this.serverId = serverId;
		this.deviceId = deviceId;
		this.permissions = permissions;
		this.active = active;
		this.oneTimeAccess = oneTimeAccess;
		new DataAccess().addLog("new accessCode, active = " + active);
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public boolean isOneTimeAccess() {
		return oneTimeAccess;
	}

	public void setOneTimeAccess(boolean oneTimeAccess) {
		this.oneTimeAccess = oneTimeAccess;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public String getAccessCode() {
		return accessCode;
	}

	public void setAccessCode(String accessCode) {
		this.accessCode = accessCode;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getPermissions() {
		return permissions;
	}

	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setActive(boolean active, String caller) {
		new DataAccess().addLog(deviceId, "setting access code to " + active + " from " + caller,
				LogConstants.IMPORTANT);
		this.active = active;
	}

	public String getBrowserId() {
		return browserId;
	}

	public void setBrowserId(String browserId) {
		this.browserId = browserId;
	}

}
