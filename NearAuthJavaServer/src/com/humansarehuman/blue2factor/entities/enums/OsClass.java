package com.humansarehuman.blue2factor.entities.enums;

public enum OsClass {
	ANDROID("android"), DUMBPHONE("dumbphone"), IOS("ios"), LINUX("linux"), OSX("osx"), UNKNOWN("unknown"),
	WATCHOS("watchos"), WINDOWS("windows");

	private String osClassName;

	OsClass(String osClassName) {
		this.osClassName = osClassName;
	}

	public String osClassName() {
		return osClassName.toLowerCase();
	}

	@Override
	public String toString() {
		String cls = "";
		switch (this) {
		case ANDROID:
			cls = "Android";
			break;
		case DUMBPHONE:
			cls = "Flip phone";
			break;
		case IOS:
			cls = "iOS";
			break;
		case LINUX:
			cls = "Linux";
			break;
		case OSX:
			cls = "MacOS";
			break;
		case UNKNOWN:
			cls = "Unknown";
			break;
		case WATCHOS:
			cls = "WatchOS";
			break;
		case WINDOWS:
			cls = "Windows";
			break;
		default:
			cls = "Unknown";
			break;

		}
		return cls;
	}
}
