package com.plantmanager.model;

import com.plantmanager.repository.CsvUtils;

import java.time.LocalDate;

public class VinePlant extends Plant {

    private String climbingSupport;
    private String growthRate;
    private double maxVineLength;

    public VinePlant(int id, String name, String species, LocalDate plantedDate,
                     String climbingSupport, String growthRate, double maxVineLength) {
        super(id, name, species, plantedDate);
        setClimbingSupport(climbingSupport);
        setGrowthRate(growthRate);
        setMaxVineLength(maxVineLength);
    }

    public String getClimbingSupport() {
        return climbingSupport;
    }

    public void setClimbingSupport(String climbingSupport) {
        if (climbingSupport == null || climbingSupport.isBlank()) {
            throw new IllegalArgumentException("Climbing support cannot be empty");
        }
        this.climbingSupport = climbingSupport.trim();
    }

    public String getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(String growthRate) {
        if (growthRate == null || growthRate.isBlank()) {
            throw new IllegalArgumentException("Growth rate cannot be empty");
        }
        this.growthRate = growthRate.trim();
    }

    public double getMaxVineLength() {
        return maxVineLength;
    }

    public void setMaxVineLength(double maxVineLength) {
        if (maxVineLength < 0) {
            throw new IllegalArgumentException("Max vine length cannot be negative");
        }
        this.maxVineLength = maxVineLength;
    }

    @Override
    public String getPlantType() {
        return "Vine";
    }

    @Override
    public String getCareInstructions() {
        return "Train onto support structure, prune to control spread. " +
                "Climbing support: " + climbingSupport + ". " +
                "Growth rate: " + growthRate + ". Max length: " + maxVineLength + "m.";
    }

    @Override
    public String toCsvRow() {
        return getId() + "," +
                CsvUtils.quote(getName()) + "," +
                CsvUtils.quote(getSpecies()) + "," +
                getPlantedDate() + "," +
                "VinePlant," +
                CsvUtils.quote(getAssignedDiseaseName()) + "," +
                ",,,,,," +
                CsvUtils.quote(categoriesCsv()) + "," +
                ",,,,," +
                CsvUtils.quote(climbingSupport) + "," +
                CsvUtils.quote(growthRate) + "," +
                maxVineLength +
                imageRefCsvSuffix();
    }
}
