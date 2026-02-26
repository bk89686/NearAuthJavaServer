package com.humansarehuman.blue2factor.entities.enums;

public enum AuthorizationMethod {
	NONE("none"), SAML("saml"), API("api"), OPEN_ID_CONNECT("openidconnect"), I_FRAME("iframe"), LDAP("ldap");

	private String f1MethodName;

	AuthorizationMethod(String f1MethodName) {
		this.f1MethodName = f1MethodName;
	}

	public String authMethodName() {
		return f1MethodName;
	}

	public boolean equals(AuthorizationMethod authMethod) {
		return f1MethodName.toString().toLowerCase().equals(authMethod.toString().toLowerCase());
	}
}
