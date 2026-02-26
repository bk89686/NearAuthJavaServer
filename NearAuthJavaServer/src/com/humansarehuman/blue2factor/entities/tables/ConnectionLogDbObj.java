package com.humansarehuman.blue2factor.entities.tables;

import java.sql.Timestamp;

import com.humansarehuman.blue2factor.entities.enums.ConnectionType;
import com.humansarehuman.blue2factor.utilities.DateTimeUtilities;
import com.humansarehuman.blue2factor.utilities.GeneralUtilities;

public class ConnectionLogDbObj {

	private String connectionLogId;
	private String connectionId;
	private String deviceId;
	private boolean connected;
	private String connectedString;
	private Timestamp eventTimestamp;
	private String src;
	private String description;
	private ConnectionType connectionType;
	private boolean ignoreEvent;

	public ConnectionLogDbObj(String connectionId, String deviceId, boolean connected, Timestamp eventTimestamp,
			String src, String description, ConnectionType connectionType) {
		super();
		this.connectionLogId = GeneralUtilities.randomString();
		this.connectionId = connectionId;
		this.deviceId = deviceId;
		this.connected = connected;
		if (connected) {
			this.connectedString = "Connected";
		} else {
			this.connectedString = "Disconnected";
		}
		this.eventTimestamp = eventTimestamp;
		this.src = src;
		this.description = description;
		this.connectionType = connectionType;
		this.ignoreEvent = false;
	}

	public ConnectionLogDbObj(String connectionLogId, String connectionId, String deviceId, boolean connected,
			Timestamp eventTimestamp, String src, String description, ConnectionType connectionType,
			boolean ignoreEvent) {
		super();
		this.connectionLogId = connectionLogId;
		this.connectionId = connectionId;
		this.deviceId = deviceId;
		this.connected = connected;
		if (connected) {
			this.connectedString = "Connected";
		} else {
			this.connectedString = "Disconnected";
		}
		this.eventTimestamp = eventTimestamp;
		this.src = src;
		this.description = description;
		this.connectionType = connectionType;
		this.ignoreEvent = ignoreEvent;
	}

	public ConnectionLogDbObj(String connectionId, String deviceId, boolean connected, String src, String description,
			ConnectionType connectionType) {
		super();
		this.connectionLogId = GeneralUtilities.randomString();
		this.connectionId = connectionId;
		this.deviceId = deviceId;
		this.connected = connected;
		if (connected) {
			this.connectedString = "Connected";
		} else {
			this.connectedString = "Disconnected";
		}
		this.eventTimestamp = DateTimeUtilities.getCurrentTimestamp();
		this.src = src;
		this.description = description;
		this.connectionType = connectionType;
	}

	public String getConnectionLogId() {
		return connectionLogId;
	}

	public void setConnectionLogId(String connectionLogId) {
		this.connectionLogId = connectionLogId;
	}

	public boolean isIgnoreEvent() {
		return ignoreEvent;
	}

	public void setIgnoreEvent(boolean ignoreEvent) {
		this.ignoreEvent = ignoreEvent;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	public String getConnectedString() {
		return connectedString;
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public Timestamp getEventTimestamp() {
		return eventTimestamp;
	}

	public void setEventTimestamp(Timestamp eventTimestamp) {
		this.eventTimestamp = eventTimestamp;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	@Override
	public String toString() {
		return "ConnectionLogDbObj [connectionLogId=" + connectionLogId + ", connectionId=" + connectionId
				+ ", connected=" + connected + ", connectedString=" + connectedString + ", eventTimestamp="
				+ eventTimestamp + ", src=" + src + ", description=" + description + ", connectionType="
				+ connectionType + ", ignoreEvent=" + ignoreEvent + "]";
	}

}
