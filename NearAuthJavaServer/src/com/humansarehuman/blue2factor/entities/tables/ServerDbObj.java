package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

public class ServerDbObj {
	private String serverQuery = "CREATE TABLE IF NOT EXISTS B2F_SERVER ("
			+ "CREATE_DATE timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP, " + "SERVER_ID varchar(255) DEFAULT NULL, "
			+ "B2F_ID varchar(255) DEFAULT NULL, " + "PERMISSIONS int DEFAULT 0, "
			+ "ACTIVE tinyint(1) NOT NULL DEFAULT 1, " + "ENABLED tinyint(1) NOT NULL DEFAULT 1, "
			+ "SERVER_NAME varchar(1023), " + "COMPANY_ID varchar(255),  " + "PUBLIC_KEY varchar(4095), "
			+ "PRIVATE_KEY varchar(4095) " + ") ENGINE=InnoDB DEFAULT CHARSET=latin1";

	private Timestamp createDate;
	private String serverId;
	private String b2fId;
	private int permissions;
	private boolean active;
	private boolean enabled;
	private String serverName;
	private String companyId;
	private String hostname;
	private String description;

	public ServerDbObj(Timestamp createDate, String serverId, String b2fId, int permissions, boolean active,
			boolean enabled, String serverName, String companyId, String hostname, String description) {
		super();
		this.createDate = createDate;
		this.serverId = serverId;
		this.b2fId = b2fId;
		this.permissions = permissions;
		this.active = active;
		this.enabled = enabled;
		this.serverName = serverName;
		this.companyId = companyId;
		this.hostname = hostname;
		this.description = description;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getServerQuery() {
		return serverQuery;
	}

	public void setServerQuery(String serverQuery) {
		this.serverQuery = serverQuery;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getB2fId() {
		return b2fId;
	}

	public void setB2fId(String b2fId) {
		this.b2fId = b2fId;
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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
