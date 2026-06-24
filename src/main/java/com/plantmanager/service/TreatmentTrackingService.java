package com.plantmanager.service;

import com.plantmanager.model.Plant;
import com.plantmanager.model.TreatmentEffectiveness;
import com.plantmanager.model.TreatmentRecord;
import com.plantmanager.model.TreatmentStatus;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Treatment tracking, reminders, and weight-based effectiveness recommendations.
 */
public final class TreatmentTrackingService {

    private TreatmentTrackingService() {
    }

    public static void syncWithPlants(List<Plant> plants, ObservableList<TreatmentRecord> records, int nextId) {
        for (Plant plant : plants) {
            if (plant.hasDisease() && findActive(plant.getId(), records).isEmpty()) {
                records.add(TreatmentRecord.fromPlant(plant, nextId++));
            }
        }
    }

    public static TreatmentRecord startTracking(Plant plant, ObservableList<TreatmentRecord> records, int nextId) {
        cancelActive(plant.getId(), records);
        if (!plant.hasDisease()) {
            return null;
        }
        TreatmentRecord record = TreatmentRecord.fromPlant(plant, nextId);
        records.add(record);
        return record;
    }

    public static void cancelActive(int plantId, ObservableList<TreatmentRecord> records) {
        findActive(plantId, records).ifPresent(record -> {
            record.setStatus(TreatmentStatus.CANCELLED);
            record.setEndDate(LocalDate.now());
        });
    }

    public static Optional<TreatmentRecord> findActive(int plantId, ObservableList<TreatmentRecord> records) {
        return records.stream()
                .filter(r -> r.getPlantId() == plantId && r.getStatus() == TreatmentStatus.ACTIVE)
                .findFirst();
    }

    public static List<TreatmentRecord> getActiveReminders(ObservableList<TreatmentRecord> records) {
        return records.stream()
                .filter(r -> r.getStatus() == TreatmentStatus.ACTIVE)
                .sorted(Comparator.comparing(TreatmentRecord::isOverdue).reversed()
                        .thenComparing(TreatmentRecord::getPlantName))
                .collect(Collectors.toList());
    }

    public static void logApplication(TreatmentRecord record) {
        record.incrementApplications();
    }

    public static void setEffectiveness(TreatmentRecord record, TreatmentEffectiveness effectiveness) {
        record.setEffectiveness(effectiveness);
    }

    public static void completeTreatment(TreatmentRecord record, TreatmentEffectiveness effectiveness) {
        record.setEffectiveness(effectiveness);
        record.setStatus(TreatmentStatus.COMPLETED);
        record.setEndDate(LocalDate.now());
    }

    public static Optional<TreatmentRecommendation> recommendTreatment(
            String diseaseName, String plantType, ObservableList<TreatmentRecord> records) {

        List<TreatmentRecord> history = records.stream()
                .filter(r -> r.getStatus() == TreatmentStatus.COMPLETED)
                .filter(r -> r.getDiseaseName().equalsIgnoreCase(diseaseName))
                .filter(r -> r.getPlantType().equalsIgnoreCase(plantType))
                .filter(r -> r.getEffectiveness() != TreatmentEffectiveness.NONE)
                .toList();

        if (history.isEmpty()) {
            history = records.stream()
                    .filter(r -> r.getStatus() == TreatmentStatus.COMPLETED)
                    .filter(r -> r.getDiseaseName().equalsIgnoreCase(diseaseName))
                    .filter(r -> r.getEffectiveness() != TreatmentEffectiveness.NONE)
                    .toList();
        }

        if (history.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Double> scoreSum = new HashMap<>();
        Map<String, Integer> scoreCount = new HashMap<>();

        for (TreatmentRecord record : history) {
            String key = record.getTreatmentName();
            double weight = record.getEffectiveness().getWeight();
            scoreSum.merge(key, weight, Double::sum);
            scoreCount.merge(key, 1, Integer::sum);
        }

        return scoreSum.entrySet().stream()
                .map(e -> {
                    int count = scoreCount.get(e.getKey());
                    double avg = e.getValue() / count;
                    return new TreatmentRecommendation(e.getKey(), avg, count, diseaseName, plantType);
                })
                .max(Comparator.comparingDouble(TreatmentRecommendation::score));
    }

    public record TreatmentRecommendation(
            String treatmentName,
            double score,
            int sampleSize,
            String diseaseName,
            String plantType
    ) {
        public String getSummary() {
            return String.format(
                    "Recommended: %s (%.0f%% effectiveness, %d past case(s) for %s on %s plants)",
                    treatmentName, score * 100, sampleSize, diseaseName, plantType);
        }
    }
}
