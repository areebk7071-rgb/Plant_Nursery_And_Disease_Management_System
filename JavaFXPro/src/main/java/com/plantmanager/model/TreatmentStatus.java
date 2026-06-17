package com.plantmanager.model;

public enum TreatmentStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED;

    public static TreatmentStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVE;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTIVE;
        }
    }
}
