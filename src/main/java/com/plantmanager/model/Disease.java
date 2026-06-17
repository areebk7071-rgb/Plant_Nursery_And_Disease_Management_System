package com.plantmanager.model;

/**
 * Represents a predefined plant disease with associated treatment.
 */
public class Disease {

    private String name;
    private String symptoms;
    private String causativeAgent;
    private Treatment treatment;

    public Disease(String name, String symptoms, String causativeAgent, Treatment treatment) {
        setName(name);
        setSymptoms(symptoms);
        setCausativeAgent(causativeAgent);
        setTreatment(treatment);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Disease name cannot be empty");
        }
        this.name = name.trim();
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        if (symptoms == null || symptoms.isBlank()) {
            throw new IllegalArgumentException("Symptoms cannot be empty");
        }
        this.symptoms = symptoms.trim();
    }

    public String getCausativeAgent() {
        return causativeAgent;
    }

    public void setCausativeAgent(String causativeAgent) {
        if (causativeAgent == null || causativeAgent.isBlank()) {
            throw new IllegalArgumentException("Causative agent cannot be empty");
        }
        this.causativeAgent = causativeAgent.trim();
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        if (treatment == null) {
            throw new IllegalArgumentException("Treatment cannot be null");
        }
        this.treatment = treatment;
    }

    public String getFormattedDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Disease: ").append(name).append("\n\n");
        sb.append("Symptoms:\n  ").append(symptoms).append("\n\n");
        sb.append("Causative Agent: ").append(causativeAgent).append("\n\n");
        sb.append("--- Treatment Plan ---\n\n");
        sb.append(treatment.getFormattedDetails());
        return sb.toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
