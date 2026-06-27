package com.plantmanager.model;

import com.plantmanager.repository.CsvUtils;

import java.time.LocalDate;

/**
 * Herb plant subclass – adds culinary use and medicinal flag.
 */
public class HerbPlant extends Plant {

    private String culinaryUse;
    private boolean isMedicinal;

    public HerbPlant(int id, String name, String species, LocalDate plantedDate,
                     String culinaryUse, boolean isMedicinal) {
        super(id, name, species, plantedDate);
        setCulinaryUse(culinaryUse);
        this.isMedicinal = isMedicinal;
    }

    public String getCulinaryUse() {
        return culinaryUse;
    }

    public void setCulinaryUse(String culinaryUse) {
        if (culinaryUse == null || culinaryUse.isBlank()) {
            throw new IllegalArgumentException("Culinary use cannot be empty");
        }
        this.culinaryUse = culinaryUse.trim();
    }

    public boolean isMedicinal() {
        return isMedicinal;
    }

    public void setMedicinal(boolean medicinal) {
        isMedicinal = medicinal;
    }

    @Override
    public String getPlantType() {
        return "Herb";
    }

    @Override
    public String getCareInstructions() {
        return "Harvest leaves regularly to encourage growth. " +
                "Culinary use: " + culinaryUse + ". " +
                (isMedicinal ? "Has medicinal properties – consult before use." : "Ornamental/culinary only.");
    }

    @Override
    public String toCsvRow() {
        return getId() + "," +
                CsvUtils.quote(getName()) + "," +
                CsvUtils.quote(getSpecies()) + "," +
                getPlantedDate() + "," +
                "HerbPlant," +
                CsvUtils.quote(getAssignedDiseaseName()) + "," +
                ",,,," +
                CsvUtils.quote(culinaryUse) + "," +
                isMedicinal +
                imageRefCsvSuffix() + "," +
                CsvUtils.quote(categoriesCsv()) + ",,,,,,,,,";
    }
}
