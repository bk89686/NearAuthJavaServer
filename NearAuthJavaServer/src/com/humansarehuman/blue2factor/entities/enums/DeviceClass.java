package com.humansarehuman.blue2factor.entities.enums;

public enum DeviceClass {
	COMPUTER("computer"), IOT("iot"), PHONE("phone"), TABLET("tablet/secondary phone"), WATCH("watch"), TEMP("temp"),
	UNKNOWN("unknown");

	private String deviceClassName;

	DeviceClass(String deviceClassName) {
		this.deviceClassName = deviceClassName;
	}

	public String deviceClassName() {
		return deviceClassName.toLowerCase();
	}
}
