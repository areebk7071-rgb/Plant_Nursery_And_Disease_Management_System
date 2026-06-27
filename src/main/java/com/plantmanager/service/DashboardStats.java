package com.plantmanager.service;

import com.plantmanager.model.DiseaseLibrary;
import com.plantmanager.model.Plant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Computes dashboard statistics and user-facing insights from plant data.
 */
public final class DashboardStats {

    private DashboardStats() {
    }

    public static int countHealthy(List<Plant> plants) {
        return (int) plants.stream().filter(p -> !p.hasDisease()).count();
    }

    public static int countDiseased(List<Plant> plants) {
        return (int) plants.stream().filter(Plant::hasDisease).count();
    }

    public static Map<String, Long> countByType(List<Plant> plants) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("Fruit", plants.stream().filter(p -> "Fruit".equals(p.getPlantType())).count());
        counts.put("Flower", plants.stream().filter(p -> "Flower".equals(p.getPlantType())).count());
        counts.put("Herb", plants.stream().filter(p -> "Herb".equals(p.getPlantType())).count());
        counts.put("Vegetable", plants.stream().filter(p -> "Vegetable".equals(p.getPlantType())).count());
        counts.put("Tree", plants.stream().filter(p -> "Tree".equals(p.getPlantType())).count());
        counts.put("Vine", plants.stream().filter(p -> "Vine".equals(p.getPlantType())).count());
        return counts;
    }

    public static Map<String, Long> countByDisease(List<Plant> plants) {
        return plants.stream()
                .filter(Plant::hasDisease)
                .collect(Collectors.groupingBy(p -> p.getAssignedDisease().getName(), Collectors.counting()));
    }

    public static double healthPercentage(List<Plant> plants) {
        if (plants.isEmpty()) {
            return 0;
        }
        return (countHealthy(plants) * 100.0) / plants.size();
    }

    public static List<String> generateInsights(List<Plant> plants) {
        List<String> insights = new ArrayList<>();
        int total = plants.size();
        int healthy = countHealthy(plants);
        int diseased = countDiseased(plants);

        if (total == 0) {
            insights.add("🌱 Welcome! Add your first plant to start tracking garden health.");
            insights.add("📚 Browse the Disease Library to explore treatment options.");
            return insights;
        }

        double healthPct = healthPercentage(plants);
        insights.add(String.format("📊 Garden health score: %.0f%% (%d of %d plants healthy)",
                healthPct, healthy, total));

        if (diseased > 0) {
            insights.add(String.format("⚠ %d plant(s) need treatment — open Plants tab and click Show Treatment",
                    diseased));
            plants.stream()
                    .filter(Plant::hasDisease)
                    .limit(3)
                    .forEach(p -> insights.add(String.format("   • %s → %s", p.getName(), p.getAssignedDiseaseName())));
        } else {
            insights.add("✅ All plants are healthy — great job maintaining your garden!");
        }

        Map<String, Long> byDisease = countByDisease(plants);
        if (!byDisease.isEmpty()) {
            String topDisease = byDisease.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(e -> e.getKey() + " (" + e.getValue() + " plant(s))")
                    .orElse("");
            insights.add("🔬 Most common issue: " + topDisease);
        }

        Map<String, Long> byType = countByType(plants);
        String dominantType = byType.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .orElse(null);
        if (dominantType != null) {
            insights.add("🌿 Most common plant type: " + dominantType);
        }

        long fruit = byType.getOrDefault("Fruit", 0L);
        long flower = byType.getOrDefault("Flower", 0L);
        long herb = byType.getOrDefault("Herb", 0L);
        long vegetable = byType.getOrDefault("Vegetable", 0L);
        long tree = byType.getOrDefault("Tree", 0L);
        long vine = byType.getOrDefault("Vine", 0L);
        long totalUniqueTypes = (fruit > 0 ? 1 : 0) + (flower > 0 ? 1 : 0) + (herb > 0 ? 1 : 0)
                + (vegetable > 0 ? 1 : 0) + (tree > 0 ? 1 : 0) + (vine > 0 ? 1 : 0);
        if (totalUniqueTypes <= 2) {
            insights.add("💡 Tip: Add more plant varieties (vegetables, trees, or vines) to diversify your garden portfolio.");
        } else if (diseased > 0 && healthPct < 50) {
            insights.add("💡 Tip: Review treatment schedules — early action prevents spread.");
        } else {
            insights.add("💡 Tip: Use Assign Disease to link symptoms with treatment plans quickly.");
        }

        insights.add(String.format("📖 Disease library: %d known conditions available",
                DiseaseLibrary.getAll().size()));

        return insights;
    }
}
