package com.plantmanager.repository;

import com.plantmanager.model.Disease;
import com.plantmanager.model.DiseaseLibrary;
import com.plantmanager.model.Treatment;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs full CRUD (Create, Read, Update, Delete) for the disease library
 * against the SQLite {@code diseases} table via JDBC.
 *
 * <p>The in-memory {@link DiseaseLibrary} registry remains the live source of
 * truth used throughout the running application (unchanged); this repository
 * is only responsible for persisting it to, and restoring it from, the database.</p>
 */
public class DiseaseRepository {

    /** READ — loads all diseases from the database into {@link DiseaseLibrary} (seeds defaults on first run). */
    public void load() throws IOException {
        try {
            Connection conn = DatabaseManager.getConnection();

            try (Statement check = conn.createStatement();
                 ResultSet rs = check.executeQuery("SELECT COUNT(*) AS cnt FROM diseases")) {
                if (rs.next() && rs.getInt("cnt") == 0) {
                    DiseaseLibrary.loadDefaults();
                    save();
                    return;
                }
            }

            List<Disease> diseases = new ArrayList<>();
            String sql = "SELECT name, symptoms, causative_agent, chemical_name, application_method, " +
                    "organic_alternative, duration_days, precautions FROM diseases ORDER BY name";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Treatment treatment = new Treatment(
                            rs.getString("chemical_name"),
                            rs.getString("application_method"),
                            rs.getString("organic_alternative"),
                            rs.getInt("duration_days"),
                            rs.getString("precautions")
                    );
                    diseases.add(new Disease(
                            rs.getString("name"),
                            rs.getString("symptoms"),
                            rs.getString("causative_agent"),
                            treatment
                    ));
                }
            }

            if (diseases.isEmpty()) {
                DiseaseLibrary.loadDefaults();
            } else {
                DiseaseLibrary.replaceAll(diseases);
            }
        } catch (SQLException e) {
            throw new IOException("Failed to load diseases from database", e);
        }
    }

    /** CREATE/UPDATE (bulk) — persists the current in-memory DiseaseLibrary contents to the database. */
    public void save() throws IOException {
        String upsert = "INSERT INTO diseases " +
                "(name, symptoms, causative_agent, chemical_name, application_method, organic_alternative, duration_days, precautions) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(name) DO UPDATE SET " +
                "symptoms=excluded.symptoms, causative_agent=excluded.causative_agent, " +
                "chemical_name=excluded.chemical_name, application_method=excluded.application_method, " +
                "organic_alternative=excluded.organic_alternative, duration_days=excluded.duration_days, " +
                "precautions=excluded.precautions";

        try {
            Connection conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            try {
                List<Disease> current = DiseaseLibrary.getAll();

                // DELETE — remove diseases no longer present in the in-memory library
                try (PreparedStatement del = conn.prepareStatement(buildDeleteMissingSql(current))) {
                    int i = 1;
                    for (Disease d : current) {
                        del.setString(i++, d.getName());
                    }
                    del.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                    for (Disease disease : current) {
                        Treatment t = disease.getTreatment();
                        stmt.setString(1, disease.getName());
                        stmt.setString(2, disease.getSymptoms());
                        stmt.setString(3, disease.getCausativeAgent());
                        stmt.setString(4, t.getChemicalName());
                        stmt.setString(5, t.getApplicationMethod());
                        stmt.setString(6, t.getOrganicAlternative());
                        stmt.setInt(7, t.getDurationDays());
                        stmt.setString(8, t.getPrecautions());
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
            throw new IOException("Failed to save diseases to database", e);
        }
    }

    /** DELETE — removes a single disease by name. */
    public void deleteDisease(String name) throws IOException {
        String sql = "DELETE FROM diseases WHERE name = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IOException("Failed to delete disease " + name, e);
        }
    }

    private String buildDeleteMissingSql(List<Disease> diseases) {
        if (diseases.isEmpty()) {
            return "DELETE FROM diseases WHERE 1=0";
        }
        String placeholders = String.join(",", diseases.stream().map(d -> "?").toList());
        return "DELETE FROM diseases WHERE name NOT IN (" + placeholders + ")";
    }
}
