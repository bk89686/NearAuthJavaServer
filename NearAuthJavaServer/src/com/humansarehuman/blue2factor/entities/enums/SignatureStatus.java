package com.humansarehuman.blue2factor.entities.enums;

public enum SignatureStatus {
    UNVALIDATE(0), // default
    SUCCESS(1), FAILURE(2);

    private int value;

    private SignatureStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
