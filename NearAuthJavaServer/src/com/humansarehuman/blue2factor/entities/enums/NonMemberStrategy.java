package com.humansarehuman.blue2factor.entities.enums;

public enum NonMemberStrategy {
	// ALLOW_NO_DEVICE: users that are registered but have zero devices registered
	// can gain access through first factor authentication
	//
	// ALLOW_NOT_SIGNED_UP: users that aren't registered can gain access through
	// first factor authentication
	//
	// ALLOW_NOT_SIGNED_UP_OR_NO_DEVICE: users can either not be registered or have
	// no devicescan gain access through first factor authentication
	//
	// ALLOW_AUTHENTICATED_ONLY: users must be in the database and have their device
	ALLOW_NOT_SIGNED_UP, ALLOW_NO_DEVICE, ALLOW_NOT_SIGNED_UP_OR_NO_DEVICE, ALLOW_AUTHENTICATED_ONLY
}
