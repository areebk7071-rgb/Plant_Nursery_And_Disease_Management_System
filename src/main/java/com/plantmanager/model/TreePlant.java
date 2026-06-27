package com.plantmanager.model;

import com.plantmanager.repository.CsvUtils;

import java.time.LocalDate;

public class TreePlant extends Plant {

    private double maxHeight;
    private double canopySpread;

    public TreePlant(int id, String name, String species, LocalDate plantedDate,
                     double maxHeight, double canopySpread) {
        super(id, name, species, plantedDate);
        setMaxHeight(maxHeight);
        setCanopySpread(canopySpread);
    }

    public double getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(double maxHeight) {
        if (maxHeight < 0) {
            throw new IllegalArgumentException("Max height cannot be negative");
        }
        this.maxHeight = maxHeight;
    }

    public double getCanopySpread() {
        return canopySpread;
    }

    public void setCanopySpread(double canopySpread) {
        if (canopySpread < 0) {
            throw new IllegalArgumentException("Canopy spread cannot be negative");
        }
        this.canopySpread = canopySpread;
    }

    @Override
    public String getPlantType() {
        return "Tree";
    }

    @Override
    public String getCareInstructions() {
        return "Provide sturdy staking if young, water deeply during dry spells. " +
                "Mature height: " + maxHeight + "m. Canopy spread: " + canopySpread + "m.";
    }

    @Override
    public String toCsvRow() {
        return getId() + "," +
                CsvUtils.quote(getName()) + "," +
                CsvUtils.quote(getSpecies()) + "," +
                getPlantedDate() + "," +
                "TreePlant," +
                CsvUtils.quote(getAssignedDiseaseName()) + "," +
                ",,,,,," +
                CsvUtils.quote(categoriesCsv()) + "," +
                ",," +
                maxHeight + "," +
                canopySpread + ",,," +
                imageRefCsvSuffix();
    }
}
