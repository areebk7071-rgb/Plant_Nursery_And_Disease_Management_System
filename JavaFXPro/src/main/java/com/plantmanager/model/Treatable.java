package com.plantmanager.model;

/**
 * Interface demonstrating abstraction – any treatable entity can expose
 * its assigned disease and apply a treatment plan.
 */
public interface Treatable {

    Disease getAssignedDisease();

    void setAssignedDisease(Disease disease);

    default boolean hasDisease() {
        return getAssignedDisease() != null;
    }

    default String applyTreatment() {
        if (!hasDisease()) {
            return "No disease assigned – no treatment required.";
        }
        return getAssignedDisease().getTreatment().getFormattedDetails();
    }
}
