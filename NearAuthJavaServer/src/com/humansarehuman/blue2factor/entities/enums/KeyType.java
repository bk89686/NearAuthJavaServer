package com.humansarehuman.blue2factor.entities.enums;

public enum KeyType {
	// the client server public key matches up to a private key that the client
	// keeps on their
	// server
	// the client server private key matches up to a separate public key that the
	// client keeps on
	// their server
	BROWSER_PUBLIC_KEY, B2F_SERVER_PRIVATE_KEY_FOR_BROWSER, SERVER_SSH_PRIVATE_KEY, SERVER_SSH_PUBLIC_KEY,
	DEVICE_PUBLIC_KEY, SERVER_PRIVATE_KEY_FOR_DEVICE, DEVICE_PUBLIC_KEY_FOREGROUND, IDP_PRIVATE, IDP_CERT, JWT, SP_PRIVATE, SP_CERT,
	TERMINAL_SSH_PUBLIC_KEY, TERMINAL_SSH_PRIVATE_KEY, UNKNOWN, WEB_SERVER_PUBLIC_KEY, LAMBDA_PUBLIC_KEY,
	LAMBDA_PRIVATE_KEY
}
