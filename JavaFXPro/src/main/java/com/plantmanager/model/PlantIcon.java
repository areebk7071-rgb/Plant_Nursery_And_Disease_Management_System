package com.plantmanager.model;

/**
 * Built-in plant icons and metadata for the icon picker.
 * Custom photos use imageRef prefix {@code file:} (see PlantImageService).
 */
public enum PlantIcon {

    FRUIT("builtin:fruit", "Default Fruit 🍊", "Fruit", "🍊", "#40916c"),
    TOMATO("builtin:tomato", "Tomato 🍅", "Fruit", "🍅", "#d62828"),
    APPLE("builtin:apple", "Apple 🍎", "Fruit", "🍎", "#bc4749"),
    FLOWER("builtin:flower", "Default Flower 🌸", "Flower", "🌸", "#e9c46a"),
    ROSE("builtin:rose", "Rose 🌹", "Flower", "🌹", "#e76f51"),
    HERB("builtin:herb", "Default Herb 🌿", "Herb", "🌿", "#457b9d"),
    BASIL("builtin:basil", "Basil 🌿", "Herb", "🌿", "#52b788"),
    ALOE("builtin:aloe", "Aloe / Succulent 🪴", "Herb", "🪴", "#2d6a4f");

    private final String key;
    private final String label;
    private final String plantType;
    private final String emoji;
    private final String color;

    PlantIcon(String key, String label, String plantType, String emoji, String color) {
        this.key = key;
        this.label = label;
        this.plantType = plantType;
        this.emoji = emoji;
        this.color = color;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getPlantType() {
        return plantType;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getColor() {
        return color;
    }

    public static PlantIcon defaultForType(String plantType) {
        return switch (plantType) {
            case "Flower" -> FLOWER;
            case "Herb" -> HERB;
            default -> FRUIT;
        };
    }

    public static PlantIcon fromKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        for (PlantIcon icon : values()) {
            if (icon.key.equals(key.trim())) {
                return icon;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return label;
    }
}
