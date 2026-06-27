package com.plantmanager.model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class PlantFactory {

    private PlantFactory() {
    }

    public static Plant fromCsv(String[] parts) {
        if (parts == null || parts.length < 12) {
            return null;
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            String species = parts[2].trim();
            LocalDate plantedDate = LocalDate.parse(parts[3].trim());
            String plantType = parts[4].trim();
            String diseaseName = parts[5].trim();

            Plant plant = createPlant(plantType, id, name, species, plantedDate, parts);
            if (plant == null) {
                return null;
            }

            DiseaseLibrary.findByName(diseaseName).ifPresent(plant::setAssignedDisease);

            if (parts.length > 13) {
                String categoriesStr = parts[13].trim();
                if (!categoriesStr.isEmpty()) {
                    Set<String> cats = Arrays.stream(categoriesStr.split(";"))
                            .map(String::trim)
                            .filter(c -> !c.isEmpty())
                            .collect(Collectors.toSet());
                    plant.setCategories(cats);
                }
            }

            if (parts.length > 12) {
                plant.setImageRef(com.plantmanager.service.PlantImageService.resolveImageRef(
                        parts[12].trim(), plant.getPlantType()));
            } else {
                plant.setImageRef(PlantIcon.defaultForType(plant.getPlantType()).getKey());
            }
            return plant;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Plant createPlant(String plantType, int id, String name, String species,
                                      LocalDate plantedDate, String[] parts) {
        return switch (plantType) {
            case "FruitPlant" -> {
                double yield = parseDouble(parts[6], 0);
                String harvest = parts[7].trim();
                yield new FruitPlant(id, name, species, plantedDate, yield,
                        harvest.isEmpty() ? "Summer" : harvest);
            }
            case "FlowerPlant" -> {
                String bloomColor = parts[8].trim();
                boolean perennial = Boolean.parseBoolean(parts[9].trim());
                yield new FlowerPlant(id, name, species, plantedDate,
                        bloomColor.isEmpty() ? "Mixed" : bloomColor, perennial);
            }
            case "HerbPlant" -> {
                String culinary = parts[10].trim();
                boolean medicinal = Boolean.parseBoolean(parts[11].trim());
                yield new HerbPlant(id, name, species, plantedDate,
                        culinary.isEmpty() ? "General seasoning" : culinary, medicinal);
            }
            case "VegetablePlant" -> {
                String rootType = parts.length > 14 ? parts[14].trim() : "";
                int daysToHarvest = parts.length > 15 ? parseInt(parts[15], 60) : 60;
                yield new VegetablePlant(id, name, species, plantedDate,
                        rootType.isEmpty() ? "Taproot" : rootType, daysToHarvest);
            }
            case "TreePlant" -> {
                double maxHeight = parts.length > 16 ? parseDouble(parts[16], 5) : 5;
                double canopySpread = parts.length > 17 ? parseDouble(parts[17], 3) : 3;
                yield new TreePlant(id, name, species, plantedDate, maxHeight, canopySpread);
            }
            case "VinePlant" -> {
                String support = parts.length > 19 ? parts[19].trim() : "";
                String growth = parts.length > 20 ? parts[20].trim() : "";
                double length = parts.length > 21 ? parseDouble(parts[21], 3) : 3;
                yield new VinePlant(id, name, species, plantedDate,
                        support.isEmpty() ? "Trellis" : support,
                        growth.isEmpty() ? "Medium" : growth, length);
            }
            default -> null;
        };
    }

    private static double parseDouble(String value, double defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Double.parseDouble(value.trim());
    }

    private static int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }
}
