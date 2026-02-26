package com.humansarehuman.blue2factor.entities.enums;

public enum Environment {
	DEV("dev"), TEST("test"), PROD("prod");

	private String environment;

	Environment(String environment) {
		this.environment = environment;
	}

	public String checkTypeName() {
		return this.environment;
	}
}
