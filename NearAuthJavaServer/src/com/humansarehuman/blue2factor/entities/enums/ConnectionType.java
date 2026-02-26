package com.humansarehuman.blue2factor.entities.enums;

public enum ConnectionType {
	PROX("prox"), PUSH("push"), PASSKEY("passkey"), TXT("txt"), ADMIN_CODE("adminCode");

	private String connectionTypeName;

	ConnectionType(String connectionTypeName) {
		this.connectionTypeName = connectionTypeName;
	}

	public String connectionTypeName() {
		return this.connectionTypeName;
	}

	public String getStringName() {
		String connType;
		switch (this) {
		case ADMIN_CODE:
			connType = "administrative code";
			break;
		case PASSKEY:
			connType = "passkey";
			break;
		case PROX:
			connType = "proximity";
			break;
		case PUSH:
			connType = "push notification";
			break;
		case TXT:
			connType = "text message";
			break;
		default:
			connType = "unknown connection type";
			break;
		}
		return connType;
	}

	public static ConnectionType fromString(String sValueOf) {
		ConnectionType connType = null;
		if (sValueOf != null) {
			switch (sValueOf.toLowerCase()) {
			case "prox":
				connType = PROX;
				break;
			case "push":
				connType = PUSH;
				break;
			case "passkey":
				connType = PASSKEY;
				break;
			case "txt":
				connType = TXT;
				break;
			case "adminCode":
				connType = ADMIN_CODE;
				break;
			}
		}
		return connType;
	}

}
