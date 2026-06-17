package com.plantmanager.model;

/**
 * User-rated outcome of a completed or ongoing treatment.
 */
public enum TreatmentEffectiveness {
    NONE("Not rated yet", 0.0),
    EFFECTIVE("Effective", 1.0),
    PARTIAL("Partially effective", 0.5),
    INEFFECTIVE("Ineffective", 0.0);

    private final String label;
    private final double weight;

    TreatmentEffectiveness(String label, double weight) {
        this.label = label;
        this.weight = weight;
    }

    public String getLabel() {
        return label;
    }

    public double getWeight() {
        return weight;
    }

    public static TreatmentEffectiveness fromString(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }

    @Override
    public String toString() {
        return label;
    }
}
