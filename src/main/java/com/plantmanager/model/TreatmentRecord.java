package com.plantmanager.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Tracks an active or historical treatment cycle for a plant.
 */
public class TreatmentRecord {

    private int id;
    private int plantId;
    private String plantName;
    private String plantType;
    private String diseaseName;
    private String treatmentName;
    private int treatmentDurationDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private TreatmentStatus status;
    private TreatmentEffectiveness effectiveness;
    private int applicationsCount;

    public TreatmentRecord(int id, int plantId, String plantName, String plantType,
                           String diseaseName, String treatmentName, int treatmentDurationDays,
                           LocalDate startDate, TreatmentStatus status) {
        this.id = id;
        this.plantId = plantId;
        this.plantName = plantName;
        this.plantType = plantType;
        this.diseaseName = diseaseName;
        this.treatmentName = treatmentName;
        this.treatmentDurationDays = treatmentDurationDays;
        this.startDate = startDate;
        this.status = status;
        this.effectiveness = TreatmentEffectiveness.NONE;
        this.applicationsCount = 0;
    }

    public int getId() {
        return id;
    }

    public int getPlantId() {
        return plantId;
    }

    public String getPlantName() {
        return plantName;
    }

    public String getPlantType() {
        return plantType;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public String getTreatmentName() {
        return treatmentName;
    }

    public int getTreatmentDurationDays() {
        return treatmentDurationDays;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public TreatmentStatus getStatus() {
        return status;
    }

    public void setStatus(TreatmentStatus status) {
        this.status = status;
    }

    public TreatmentEffectiveness getEffectiveness() {
        return effectiveness;
    }

    public void setEffectiveness(TreatmentEffectiveness effectiveness) {
        this.effectiveness = effectiveness != null ? effectiveness : TreatmentEffectiveness.NONE;
    }

    public int getApplicationsCount() {
        return applicationsCount;
    }

    public void setApplicationsCount(int applicationsCount) {
        this.applicationsCount = Math.max(0, applicationsCount);
    }

    public void incrementApplications() {
        applicationsCount++;
    }

    public long getDaysElapsed() {
        LocalDate until = endDate != null ? endDate : LocalDate.now();
        return Math.max(1, ChronoUnit.DAYS.between(startDate, until) + 1);
    }

    public boolean isOverdue() {
        return status == TreatmentStatus.ACTIVE
                && treatmentDurationDays > 0
                && getDaysElapsed() > treatmentDurationDays;
    }

    public String getProgressText() {
        if (treatmentDurationDays > 0) {
            return "Day " + getDaysElapsed() + " of " + treatmentDurationDays;
        }
        return "Day " + getDaysElapsed() + " (ongoing plan)";
    }

    public String getReminderSummary() {
        String urgency = isOverdue() ? "OVERDUE" : "Active";
        return String.format("[%s] %s | %s | %s | Applied %dx | %s",
                urgency, plantName, diseaseName, getProgressText(),
                applicationsCount, effectiveness.getLabel());
    }

    public static TreatmentRecord fromPlant(Plant plant, int id) {
        Disease disease = plant.getAssignedDisease();
        return new TreatmentRecord(
                id,
                plant.getId(),
                plant.getName(),
                plant.getPlantType(),
                disease.getName(),
                disease.getTreatment().getChemicalName(),
                disease.getTreatment().getDurationDays(),
                LocalDate.now(),
                TreatmentStatus.ACTIVE
        );
    }
}
