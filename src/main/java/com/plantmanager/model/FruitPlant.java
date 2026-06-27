package com.plantmanager.model;

import com.plantmanager.repository.CsvUtils;

import java.time.LocalDate;

/**
 * Fruit plant subclass – adds yield and harvest period fields.
 */
public class FruitPlant extends Plant {

    private double expectedYield;
    private String harvestPeriod;

    public FruitPlant(int id, String name, String species, LocalDate plantedDate,
                      double expectedYield, String harvestPeriod) {
        super(id, name, species, plantedDate);
        setExpectedYield(expectedYield);
        setHarvestPeriod(harvestPeriod);
    }

    public double getExpectedYield() {
        return expectedYield;
    }

    public void setExpectedYield(double expectedYield) {
        if (expectedYield < 0) {
            throw new IllegalArgumentException("Expected yield cannot be negative");
        }
        this.expectedYield = expectedYield;
    }

    public String getHarvestPeriod() {
        return harvestPeriod;
    }

    public void setHarvestPeriod(String harvestPeriod) {
        if (harvestPeriod == null || harvestPeriod.isBlank()) {
            throw new IllegalArgumentException("Harvest period cannot be empty");
        }
        this.harvestPeriod = harvestPeriod.trim();
    }

    @Override
    public String getPlantType() {
        return "Fruit";
    }

    @Override
    public String getCareInstructions() {
        return "Water regularly, fertilize during growing season, prune after harvest. " +
                "Expected yield: " + expectedYield + " kg. Harvest in " + harvestPeriod + ".";
    }

    @Override
    public String toCsvRow() {
        return getId() + "," +
                CsvUtils.quote(getName()) + "," +
                CsvUtils.quote(getSpecies()) + "," +
                getPlantedDate() + "," +
                "FruitPlant," +
                CsvUtils.quote(getAssignedDiseaseName()) + "," +
                expectedYield + "," +
                CsvUtils.quote(harvestPeriod) + "," +
                ",,," +
                imageRefCsvSuffix() + "," +
                CsvUtils.quote(categoriesCsv()) + ",,,,,,,,,";
    }
}
