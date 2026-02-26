package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

public class ServerConnectionDbObj {
	private String serverConnectionsQuery = "CREATE TABLE IF NOT EXISTS B2F_SERVER_CONNECTION ("
			+ "CREATE_DATE timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP, SERVER_ID varchar(255) DEFAULT NULL, "
			+ "CLIENT_ID varchar(255) DEFAULT NULL, " + "PERMISSIONS int DEFAULT 0, "
			+ "ACTIVE tinyint(1) NOT NULL DEFAULT 1" + ") ENGINE=InnoDB DEFAULT CHARSET=latin1";

	private Timestamp createDate;
	private String serverId;
	private String clientId;
	private int permissions;
	private boolean active;

	public ServerConnectionDbObj(Timestamp createDate, String serverId, String clientId, int permissions,
			boolean active) {
		super();
		this.createDate = createDate;
		this.serverId = serverId;
		this.clientId = clientId;
		this.permissions = permissions;
		this.active = active;
	}

	public String getServerConnectionsQuery() {
		return serverConnectionsQuery;
	}

	public void setServerConnectionsQuery(String serverConnectionsQuery) {
		this.serverConnectionsQuery = serverConnectionsQuery;
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

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
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

}
