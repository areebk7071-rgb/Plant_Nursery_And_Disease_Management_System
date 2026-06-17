package com.plantmanager.model;

import com.plantmanager.repository.CsvUtils;

import java.time.LocalDate;

/**
 * Flower plant subclass – adds bloom color and perennial flag.
 */
public class FlowerPlant extends Plant {

    private String bloomColor;
    private boolean isPerennial;

    public FlowerPlant(int id, String name, String species, LocalDate plantedDate,
                       String bloomColor, boolean isPerennial) {
        super(id, name, species, plantedDate);
        setBloomColor(bloomColor);
        this.isPerennial = isPerennial;
    }

    public String getBloomColor() {
        return bloomColor;
    }

    public void setBloomColor(String bloomColor) {
        if (bloomColor == null || bloomColor.isBlank()) {
            throw new IllegalArgumentException("Bloom color cannot be empty");
        }
        this.bloomColor = bloomColor.trim();
    }

    public boolean isPerennial() {
        return isPerennial;
    }

    public void setPerennial(boolean perennial) {
        isPerennial = perennial;
    }

    @Override
    public String getPlantType() {
        return "Flower";
    }

    @Override
    public String getCareInstructions() {
        return "Ensure adequate sunlight, deadhead spent blooms, " +
                (isPerennial ? "perennial – divide every 3 years." : "annual – replant each season.") +
                " Bloom color: " + bloomColor + ".";
    }

    @Override
    public String toCsvRow() {
        return getId() + "," +
                CsvUtils.quote(getName()) + "," +
                CsvUtils.quote(getSpecies()) + "," +
                getPlantedDate() + "," +
                "FlowerPlant," +
                CsvUtils.quote(getAssignedDiseaseName()) + "," +
                ",," +
                CsvUtils.quote(bloomColor) + "," +
                isPerennial + "," +
                "," + imageRefCsvSuffix();
    }
}
