package com.plantmanager.repository;

import com.plantmanager.dao.PlantDao;
import com.plantmanager.model.Plant;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository facade over {@link PlantDao} for plant CRUD against SQLite.
 */
public class PlantRepository {

    private final PlantDao plantDao = new PlantDao();

    /** READ — loads every plant row from the database. */
    public ObservableList<Plant> loadPlants() throws IOException {
        try {
            List<Plant> plants = plantDao.findAllOrderedById();
            return FXCollections.observableArrayList(plants);
        } catch (SQLException e) {
            throw new IOException("Failed to load plants from database", e);
        }
    }

    /**
     * CREATE/UPDATE (bulk) — replaces the plants table with the given list and
     * synchronizes the plant–disease junction table.
     */
    public void savePlants(ObservableList<Plant> plants) throws IOException {
        try {
            plantDao.upsertAll(plants);
        } catch (SQLException e) {
            throw new IOException("Failed to save plants to database", e);
        }
    }

    /** DELETE — removes a single plant by id. */
    public void deletePlant(int plantId) throws IOException {
        try {
            plantDao.deleteById(plantId);
        } catch (SQLException e) {
            throw new IOException("Failed to delete plant " + plantId, e);
        }
    }

    /** Optional CSV backup export (not used by QR/PDF data sheet pipeline). */
    public void exportPlants(ObservableList<Plant> plants, Path destination) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(destination)) {
            writer.write(Plant.getCsvHeader());
            writer.newLine();
            for (Plant plant : plants) {
                writer.write(plant.toCsvRow());
                writer.newLine();
            }
        }
    }

    public int nextId(ObservableList<Plant> plants) {
        return plants.stream().mapToInt(Plant::getId).max().orElse(0) + 1;
    }
}
