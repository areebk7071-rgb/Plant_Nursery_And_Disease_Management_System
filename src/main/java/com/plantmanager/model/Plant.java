package com.plantmanager.model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract base class for all plant types.
 * Demonstrates abstraction, encapsulation, and inheritance.
 */
public abstract class Plant implements Treatable {

    private int id;
    private String name;
    private String species;
    private LocalDate plantedDate;
    private Disease assignedDisease;
    private String imageRef;
    private Set<String> categories;

    protected Plant(int id, String name, String species, LocalDate plantedDate) {
        this(id, name, species, plantedDate, "");
    }

    protected Plant(int id, String name, String species, LocalDate plantedDate, String imageRef) {
        this.id = id;
        setName(name);
        setSpecies(species);
        setPlantedDate(plantedDate);
        setImageRef(imageRef);
        this.categories = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Plant name cannot be empty");
        }
        this.name = name.trim();
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        if (species == null || species.isBlank()) {
            throw new IllegalArgumentException("Species cannot be empty");
        }
        this.species = species.trim();
    }

    public LocalDate getPlantedDate() {
        return plantedDate;
    }

    public void setPlantedDate(LocalDate plantedDate) {
        if (plantedDate == null) {
            throw new IllegalArgumentException("Planted date cannot be null");
        }
        if (plantedDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Planted date cannot be in the future");
        }
        this.plantedDate = plantedDate;
    }

    @Override
    public Disease getAssignedDisease() {
        return assignedDisease;
    }

    @Override
    public void setAssignedDisease(Disease disease) {
        this.assignedDisease = disease;
    }

    public String getImageRef() {
        return imageRef != null ? imageRef : "";
    }

    public void setImageRef(String imageRef) {
        this.imageRef = imageRef != null ? imageRef.trim() : "";
    }

    public Set<String> getCategories() {
        Set<String> all = new HashSet<>(categories);
        all.add(getPlantType());
        return Collections.unmodifiableSet(all);
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories != null ? new HashSet<>(categories) : new HashSet<>();
    }

    public void addCategory(String category) {
        if (category != null && !category.isBlank()) {
            categories.add(category.trim());
        }
    }

    public void removeCategory(String category) {
        categories.remove(category);
    }

    public String getDisplayType() {
        Set<String> all = getCategories();
        if (all.size() <= 1) {
            return getPlantType();
        }
        return String.join(", ", all);
    }

    public String categoriesCsv() {
        return categories.stream()
                .filter(c -> !c.equals(getPlantType()))
                .collect(Collectors.joining(";"));
    }

    public String getHealthStatus() {
        return hasDisease() ? "Diseased" : "Healthy";
    }

    public String getAssignedDiseaseName() {
        return assignedDisease != null ? assignedDisease.getName() : "None";
    }

    /** Polymorphic – each subclass returns its own type label. */
    public abstract String getPlantType();

    /** Polymorphic – care instructions differ per plant type. */
    public abstract String getCareInstructions();

    /** Polymorphic – each subclass serializes its type-specific fields. */
    public abstract String toCsvRow();

    public static String getCsvHeader() {
        return "id,name,species,plantedDate,plantType,assignedDiseaseName," +
                "expectedYield,harvestPeriod,bloomColor,isPerennial,culinaryUse,isMedicinal,imageRef," +
                "categories,rootType,daysToHarvest,maxHeight,canopySpread,trunkDiameter,climbingSupport,growthRate,maxVineLength";
    }

    protected String imageRefCsvSuffix() {
        return "," + com.plantmanager.repository.CsvUtils.quote(getImageRef());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Plant plant = (Plant) o;
        return id == plant.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + getPlantType() + ")";
    }
}
