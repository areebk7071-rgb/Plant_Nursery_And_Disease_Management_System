package com.plantmanager.model;

import com.plantmanager.repository.CsvUtils;

import java.time.LocalDate;

public class VegetablePlant extends Plant {

    private String rootType;
    private int daysToHarvest;

    public VegetablePlant(int id, String name, String species, LocalDate plantedDate,
                          String rootType, int daysToHarvest) {
        super(id, name, species, plantedDate);
        setRootType(rootType);
        setDaysToHarvest(daysToHarvest);
    }

    public String getRootType() {
        return rootType;
    }

    public void setRootType(String rootType) {
        if (rootType == null || rootType.isBlank()) {
            throw new IllegalArgumentException("Root type cannot be empty");
        }
        this.rootType = rootType.trim();
    }

    public int getDaysToHarvest() {
        return daysToHarvest;
    }

    public void setDaysToHarvest(int daysToHarvest) {
        if (daysToHarvest < 0) {
            throw new IllegalArgumentException("Days to harvest cannot be negative");
        }
        this.daysToHarvest = daysToHarvest;
    }

    @Override
    public String getPlantType() {
        return "Vegetable";
    }

    @Override
    public String getCareInstructions() {
        return "Water consistently, ensure well-drained soil. " +
                "Root type: " + rootType + ". Ready to harvest in " + daysToHarvest + " days.";
    }

    @Override
    public String toCsvRow() {
        return getId() + "," +
                CsvUtils.quote(getName()) + "," +
                CsvUtils.quote(getSpecies()) + "," +
                getPlantedDate() + "," +
                "VegetablePlant," +
                CsvUtils.quote(getAssignedDiseaseName()) + "," +
                ",,,,,," +
                CsvUtils.quote(categoriesCsv()) + "," +
                CsvUtils.quote(rootType) + "," +
                daysToHarvest + ",,,,," +
                imageRefCsvSuffix();
    }
}
