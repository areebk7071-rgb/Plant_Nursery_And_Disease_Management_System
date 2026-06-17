package com.plantmanager.model;

/**
 * Encapsulates a predefined treatment plan for a plant disease.
 */
public class Treatment {

    private String chemicalName;
    private String applicationMethod;
    private String organicAlternative;
    private int durationDays;
    private String precautions;

    public Treatment(String chemicalName, String applicationMethod, String organicAlternative,
                       int durationDays, String precautions) {
        setChemicalName(chemicalName);
        setApplicationMethod(applicationMethod);
        setOrganicAlternative(organicAlternative);
        setDurationDays(durationDays);
        this.precautions = precautions != null ? precautions : "";
    }

    public String getChemicalName() {
        return chemicalName;
    }

    public void setChemicalName(String chemicalName) {
        if (chemicalName == null || chemicalName.isBlank()) {
            throw new IllegalArgumentException("Chemical name cannot be empty");
        }
        this.chemicalName = chemicalName.trim();
    }

    public String getApplicationMethod() {
        return applicationMethod;
    }

    public void setApplicationMethod(String applicationMethod) {
        if (applicationMethod == null || applicationMethod.isBlank()) {
            throw new IllegalArgumentException("Application method cannot be empty");
        }
        this.applicationMethod = applicationMethod.trim();
    }

    public String getOrganicAlternative() {
        return organicAlternative;
    }

    public void setOrganicAlternative(String organicAlternative) {
        if (organicAlternative == null || organicAlternative.isBlank()) {
            throw new IllegalArgumentException("Organic alternative cannot be empty");
        }
        this.organicAlternative = organicAlternative.trim();
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        if (durationDays < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        this.durationDays = durationDays;
    }

    public String getPrecautions() {
        return precautions;
    }

    public void setPrecautions(String precautions) {
        this.precautions = precautions != null ? precautions.trim() : "";
    }

    public String getFormattedDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Chemical Treatment:\n  ")
                .append(chemicalName).append(" – ").append(applicationMethod).append("\n\n");
        sb.append("Organic Alternative:\n  ").append(organicAlternative).append("\n\n");
        if (durationDays > 0) {
            sb.append("Recommended Duration: ").append(durationDays).append(" days\n");
        } else {
            sb.append("Recommended Duration: Ongoing\n");
        }
        if (!precautions.isEmpty()) {
            sb.append("\nPrecautions:\n  ").append(precautions);
        }
        return sb.toString();
    }
}
