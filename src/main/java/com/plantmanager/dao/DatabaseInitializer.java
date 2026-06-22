package com.plantmanager.dao;

import com.plantmanager.model.Disease;
import com.plantmanager.model.DiseaseLibrary;
import com.plantmanager.model.Plant;
import com.plantmanager.repository.DatabaseManager;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Ensures the SQLite database exists with schema, junction links, and seed data
 * so the application runs out-of-the-box on first launch.
 */
public final class DatabaseInitializer {

    private final DiseaseDao diseaseDao = new DiseaseDao();
    private final PlantDao plantDao = new PlantDao();

    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException {
        new DatabaseInitializer().run();
    }

    private void run() throws SQLException {
        migrateLegacyAssignments();
        seedDiseasesIfEmpty();
        seedPlantsIfEmpty();
    }

    private void migrateLegacyAssignments() throws SQLException {
        try (Statement stmt = DatabaseManager.getConnection().createStatement()) {
            stmt.execute("""
                INSERT OR IGNORE INTO plant_diseases (plant_id, disease_name)
                SELECT id, assigned_disease_name
                FROM plants
                WHERE assigned_disease_name IS NOT NULL
                  AND TRIM(assigned_disease_name) <> ''
                """);
        }
    }

    private void seedDiseasesIfEmpty() throws SQLException {
        if (diseaseDao.countAll() > 0) {
            return;
        }
        DiseaseLibrary.loadDefaults();
        diseaseDao.upsertAll(DiseaseLibrary.getAll());
    }

    private void seedPlantsIfEmpty() throws SQLException {
        if (plantDao.countAll() > 0) {
            return;
        }
        List<Plant> samplePlants = plantDao.createSamplePlants();
        plantDao.upsertAll(samplePlants);
    }
}
