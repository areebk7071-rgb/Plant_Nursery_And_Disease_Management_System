package com.plantmanager.dao;

import com.plantmanager.model.DiseaseLibrary;
import com.plantmanager.model.FlowerPlant;
import com.plantmanager.model.FruitPlant;
import com.plantmanager.model.HerbPlant;
import com.plantmanager.model.Plant;
import com.plantmanager.model.PlantFactory;
import com.plantmanager.model.PlantIcon;
import com.plantmanager.repository.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DAO for the {@code plants} table and plant–disease junction links.
 */
public class PlantDao {

    private final PlantDiseaseDao plantDiseaseDao = new PlantDiseaseDao();

    public int countAll() throws SQLException {
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM plants")) {
            return rs.next() ? rs.getInt("cnt") : 0;
        }
    }

    public List<Plant> findAllOrderedById() throws SQLException {
        Map<Integer, String> assignments = plantDiseaseDao.findAllAssignments();
        List<Plant> plants = new ArrayList<>();
        String sql = "SELECT id, name, species, planted_date, plant_type, assigned_disease_name, " +
                "expected_yield, harvest_period, bloom_color, is_perennial, culinary_use, is_medicinal, image_ref " +
                "FROM plants ORDER BY id";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Plant plant = mapRowToPlant(rs, assignments);
                if (plant != null) {
                    plants.add(plant);
                }
            }
        }
        return plants;
    }

    public void upsertAll(List<Plant> plants) throws SQLException {
        String upsert = "INSERT INTO plants " +
                "(id, name, species, planted_date, plant_type, assigned_disease_name, " +
                "expected_yield, harvest_period, bloom_color, is_perennial, culinary_use, is_medicinal, image_ref) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET " +
                "name=excluded.name, species=excluded.species, planted_date=excluded.planted_date, " +
                "plant_type=excluded.plant_type, assigned_disease_name=excluded.assigned_disease_name, " +
                "expected_yield=excluded.expected_yield, harvest_period=excluded.harvest_period, " +
                "bloom_color=excluded.bloom_color, is_perennial=excluded.is_perennial, " +
                "culinary_use=excluded.culinary_use, is_medicinal=excluded.is_medicinal, image_ref=excluded.image_ref";

        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(false);
        try {
            deleteNotIn(plants);
            try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                for (Plant plant : plants) {
                    bindPlant(stmt, plant);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            for (Plant plant : plants) {
                String diseaseName = plant.getAssignedDisease() != null
                        ? plant.getAssignedDisease().getName()
                        : null;
                plantDiseaseDao.syncAssignment(plant.getId(), diseaseName);
            }
            plantDiseaseDao.removeAssignmentsNotIn(plants.stream().map(Plant::getId).toList());
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void deleteById(int plantId) throws SQLException {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "DELETE FROM plants WHERE id = ?")) {
            stmt.setInt(1, plantId);
            stmt.executeUpdate();
        }
    }

    public List<Plant> createSamplePlants() {
        List<Plant> sample = new ArrayList<>();

        FruitPlant tomato = new FruitPlant(1, "Tomato", "Solanum lycopersicum",
                LocalDate.of(2024, 4, 15), 5.0, "Summer");
        tomato.setImageRef(PlantIcon.TOMATO.getKey());
        DiseaseLibrary.findByName("Late Blight").ifPresent(tomato::setAssignedDisease);
        sample.add(tomato);

        FlowerPlant rose = new FlowerPlant(2, "Rose", "Rosa rubiginosa",
                LocalDate.of(2023, 3, 10), "Pink", true);
        rose.setImageRef(PlantIcon.ROSE.getKey());
        DiseaseLibrary.findByName("Powdery Mildew").ifPresent(rose::setAssignedDisease);
        sample.add(rose);

        HerbPlant basil = new HerbPlant(3, "Basil", "Ocimum basilicum",
                LocalDate.of(2024, 5, 1), "Italian cuisine, pesto", false);
        basil.setImageRef(PlantIcon.BASIL.getKey());
        sample.add(basil);

        FruitPlant apple = new FruitPlant(4, "Apple Tree", "Malus domestica",
                LocalDate.of(2022, 9, 20), 50.0, "Autumn");
        apple.setImageRef(PlantIcon.APPLE.getKey());
        DiseaseLibrary.findByName("Aphid Infestation").ifPresent(apple::setAssignedDisease);
        sample.add(apple);

        return sample;
    }

    private void deleteNotIn(List<Plant> plants) throws SQLException {
        if (plants.isEmpty()) {
            try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                    "DELETE FROM plants WHERE 1=0")) {
                stmt.executeUpdate();
            }
            return;
        }
        String placeholders = String.join(",", plants.stream().map(p -> "?").toList());
        String sql = "DELETE FROM plants WHERE id NOT IN (" + placeholders + ")";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            int i = 1;
            for (Plant plant : plants) {
                stmt.setInt(i++, plant.getId());
            }
            stmt.executeUpdate();
        }
    }

    private void bindPlant(PreparedStatement stmt, Plant plant) throws SQLException {
        stmt.setInt(1, plant.getId());
        stmt.setString(2, plant.getName());
        stmt.setString(3, plant.getSpecies());
        stmt.setString(4, plant.getPlantedDate().toString());
        stmt.setString(5, plant.getClass().getSimpleName());
        stmt.setString(6, plant.getAssignedDisease() != null ? plant.getAssignedDisease().getName() : null);

        if (plant instanceof FruitPlant fp) {
            stmt.setDouble(7, fp.getExpectedYield());
            stmt.setString(8, fp.getHarvestPeriod());
        } else {
            stmt.setNull(7, java.sql.Types.REAL);
            stmt.setString(8, "");
        }

        if (plant instanceof FlowerPlant fl) {
            stmt.setString(9, fl.getBloomColor());
            stmt.setInt(10, fl.isPerennial() ? 1 : 0);
        } else {
            stmt.setString(9, "");
            stmt.setInt(10, 0);
        }

        if (plant instanceof HerbPlant hb) {
            stmt.setString(11, hb.getCulinaryUse());
            stmt.setInt(12, hb.isMedicinal() ? 1 : 0);
        } else {
            stmt.setString(11, "");
            stmt.setInt(12, 0);
        }

        stmt.setString(13, plant.getImageRef());
    }

    private Plant mapRowToPlant(ResultSet rs, Map<Integer, String> assignments) throws SQLException {
        int plantId = rs.getInt("id");
        String diseaseName = assignments.getOrDefault(plantId, rs.getString("assigned_disease_name"));
        if (diseaseName == null) {
            diseaseName = "";
        }

        String[] row = new String[13];
        row[0] = String.valueOf(plantId);
        row[1] = rs.getString("name");
        row[2] = rs.getString("species");
        row[3] = rs.getString("planted_date");
        row[4] = rs.getString("plant_type");
        row[5] = nullToEmpty(diseaseName);
        row[6] = String.valueOf(rs.getDouble("expected_yield"));
        row[7] = nullToEmpty(rs.getString("harvest_period"));
        row[8] = nullToEmpty(rs.getString("bloom_color"));
        row[9] = String.valueOf(rs.getInt("is_perennial") == 1);
        row[10] = nullToEmpty(rs.getString("culinary_use"));
        row[11] = String.valueOf(rs.getInt("is_medicinal") == 1);
        row[12] = nullToEmpty(rs.getString("image_ref"));
        return PlantFactory.fromCsv(row);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
