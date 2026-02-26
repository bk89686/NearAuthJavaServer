package com.humansarehuman.blue2factor.entities.enums;

public enum CompanyAllowed2FaMethods {
	ALL(0), //default
	PUSH(1),
	TEXT(2), // not implemented
	VOICE(4), // not implemented
	BLE(8), // alway on - even when it's not
	NFC(16); // not implemented
	
	private int value;
	
	private CompanyAllowed2FaMethods(int value) {
		this.value = value;
	}
	
	public int getValue() {
        return value;
    }
}
