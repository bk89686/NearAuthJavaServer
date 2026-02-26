package com.humansarehuman.blue2factor.entities.enums;

public enum CheckType {
	PROX("prox"), PUSH("push"), PASSKEY("passkey"), SILENT("silent"), TXT("txt"), SSHPUSH("sshpush"),
	SSH_CONN("sshConn"), CONNECTION_FROM_PERIPHERAL("connectionFromPeripheral"), ADMIN_CODE("adminCode"),
	RECENT_BT_CONNECTION("recentBtConnection"), RECENT_BT_SUBSCRIPTION("recentBtSubscription");

	private String checkTypeName;

	CheckType(String checkTypeName) {
		this.checkTypeName = checkTypeName;
	}

	public String checkTypeName() {
		return this.checkTypeName;
	}

	@Override
	public String toString() {
		String ct = "";
		switch (this) {
		case PROX:
			ct = "proximity";
			break;
		case ADMIN_CODE:
			ct = "administrative code";
			break;
		case CONNECTION_FROM_PERIPHERAL:
			ct = "peripheral connection";
			break;
		case PASSKEY:
			ct = "passkey";
			break;
		case PUSH:
			ct = "push notification";
			break;
		case RECENT_BT_CONNECTION:
			ct = "recent bluetooth connection";
			break;
		case RECENT_BT_SUBSCRIPTION:
			ct = "recent bluetooth subscription";
			break;
		case SILENT:
			ct = "silent";
			break;
		case SSHPUSH:
			ct = "ssh push";
			break;
		case SSH_CONN:
			ct = "ssh connection";
			break;
		case TXT:
			ct = "text message";
			break;
		default:
			ct = "unknown";
			break;
		}
		return ct;
	}

	public ConnectionType getConnectionType() {
		ConnectionType connType = null;
		switch (this) {
		case PROX:
			connType = ConnectionType.PROX;
			break;
		case ADMIN_CODE:
			connType = ConnectionType.ADMIN_CODE;
			break;
		case PASSKEY:
			connType = ConnectionType.PASSKEY;
			break;
		case PUSH:
			connType = ConnectionType.PUSH;
			break;
		case TXT:
			connType = ConnectionType.TXT;
			break;
		default:
			break;
		}
		return connType;
	}

}
