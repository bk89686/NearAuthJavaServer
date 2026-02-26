package com.humansarehuman.blue2factor.entities;

import com.humansarehuman.blue2factor.entities.enums.ConnectionType;

public class ConnectedAndConnectionType {
	private boolean connected;
	private ConnectionType connectionType;

	public ConnectedAndConnectionType(boolean connected, ConnectionType connectionType) {
		super();
		this.connected = connected;
		this.connectionType = connectionType;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}

}
