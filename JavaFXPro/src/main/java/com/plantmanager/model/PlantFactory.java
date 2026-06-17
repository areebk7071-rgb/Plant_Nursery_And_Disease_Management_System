package com.plantmanager.model;

import java.time.LocalDate;

/**
 * Factory that reconstructs the correct Plant subclass from a CSV row.
 * Demonstrates polymorphism via switch on plant type.
 */
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
            default -> null;
        };
    }

    private static double parseDouble(String value, double defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Double.parseDouble(value.trim());
    }
}
