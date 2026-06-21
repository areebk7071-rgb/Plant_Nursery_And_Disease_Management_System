package com.plantmanager.repository;

import com.plantmanager.model.DiseaseLibrary;
import com.plantmanager.model.FlowerPlant;
import com.plantmanager.model.FruitPlant;
import com.plantmanager.model.HerbPlant;
import com.plantmanager.model.Plant;
import com.plantmanager.model.PlantFactory;
import com.plantmanager.model.PlantIcon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs full CRUD (Create, Read, Update, Delete) for plant records against
 * the SQLite {@code plants} table via JDBC.
 *
 * <p>Public method names/signatures are kept identical to the previous CSV-based
 * implementation ({@code loadPlants}, {@code savePlants}, {@code nextId},
 * {@code exportPlants}) so that {@code MainController} and the rest of the
 * application did not need to change when the storage backend moved from
 * CSV files to a real embedded database.</p>
 */
public class PlantRepository {

    /** READ — loads every plant row from the database (creates sample data on first run). */
    public ObservableList<Plant> loadPlants() throws IOException {
        try {
            Connection conn = DatabaseManager.getConnection();

            // First run: table empty -> seed sample data, same as the old CSV behaviour.
            try (Statement check = conn.createStatement();
                 ResultSet rs = check.executeQuery("SELECT COUNT(*) AS cnt FROM plants")) {
                if (rs.next() && rs.getInt("cnt") == 0) {
                    ObservableList<Plant> sample = createSampleData();
                    savePlants(sample);
                    return sample;
                }
            }

            List<Plant> plants = new ArrayList<>();
            String sql = "SELECT id, name, species, planted_date, plant_type, assigned_disease_name, " +
                    "expected_yield, harvest_period, bloom_color, is_perennial, culinary_use, is_medicinal, image_ref " +
                    "FROM plants ORDER BY id";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Plant plant = mapRowToPlant(rs);
                    if (plant != null) {
                        plants.add(plant);
                    }
                }
            }
            return FXCollections.observableArrayList(plants);
        } catch (SQLException e) {
            throw new IOException("Failed to load plants from database", e);
        }
    }

    /**
     * CREATE/UPDATE (bulk) — replaces the entire plants table with the given list.
     * Kept as a single "save everything" operation to match the existing
     * controller workflow (load once at startup, save the full list after each change).
     */
    public void savePlants(ObservableList<Plant> plants) throws IOException {
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

        try {
            Connection conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            try {
                // DELETE — remove rows for plants no longer present in the list (handles deletions)
                try (PreparedStatement del = conn.prepareStatement(buildDeleteMissingSql(plants))) {
                    int i = 1;
                    for (Plant p : plants) {
                        del.setInt(i++, p.getId());
                    }
                    del.executeUpdate();
                }

                // CREATE/UPDATE — upsert every plant currently in the list
                try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                    for (Plant plant : plants) {
                        bindPlant(stmt, plant);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IOException("Failed to save plants to database", e);
        }
    }

    /** DELETE — removes a single plant by id (kept as a convenience method for callers that want single-row delete). */
    public void deletePlant(int plantId) throws IOException {
        String sql = "DELETE FROM plants WHERE id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, plantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IOException("Failed to delete plant " + plantId, e);
        }
    }

    /** Exports the current plant list to a CSV file (used by the "Export" feature — unrelated to the DB itself). */
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

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    private String buildDeleteMissingSql(ObservableList<Plant> plants) {
        if (plants.isEmpty()) {
            return "DELETE FROM plants WHERE 1=0"; // no-op, nothing to keep but nothing to delete either
        }
        String placeholders = String.join(",", plants.stream().map(p -> "?").toList());
        return "DELETE FROM plants WHERE id NOT IN (" + placeholders + ")";
    }

    private void bindPlant(PreparedStatement stmt, Plant plant) throws SQLException {
        stmt.setInt(1, plant.getId());
        stmt.setString(2, plant.getName());
        stmt.setString(3, plant.getSpecies());
        stmt.setString(4, plant.getPlantedDate().toString());
        stmt.setString(5, plant.getClass().getSimpleName()); // "FruitPlant" / "FlowerPlant" / "HerbPlant"
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

    private Plant mapRowToPlant(ResultSet rs) throws SQLException {
        // Reuse PlantFactory.fromCsv(...) so the polymorphic subclass-construction
        // logic stays in exactly one place, regardless of whether the row came
        // from a CSV file or (as here) a SQLite ResultSet.
        String[] row = new String[13];
        row[0] = String.valueOf(rs.getInt("id"));
        row[1] = rs.getString("name");
        row[2] = rs.getString("species");
        row[3] = rs.getString("planted_date");
        row[4] = rs.getString("plant_type");
        row[5] = nullToEmpty(rs.getString("assigned_disease_name"));
        row[6] = String.valueOf(rs.getDouble("expected_yield"));
        row[7] = nullToEmpty(rs.getString("harvest_period"));
        row[8] = nullToEmpty(rs.getString("bloom_color"));
        row[9] = String.valueOf(rs.getInt("is_perennial") == 1);
        row[10] = nullToEmpty(rs.getString("culinary_use"));
        row[11] = String.valueOf(rs.getInt("is_medicinal") == 1);
        row[12] = nullToEmpty(rs.getString("image_ref"));
        return PlantFactory.fromCsv(row);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private ObservableList<Plant> createSampleData() {
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

        return FXCollections.observableArrayList(sample);
    }
}
