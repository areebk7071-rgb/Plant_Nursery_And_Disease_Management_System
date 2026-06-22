package com.plantmanager.dao;

import com.plantmanager.model.Disease;
import com.plantmanager.model.Treatment;
import com.plantmanager.repository.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the {@code diseases} table.
 */
public class DiseaseDao {

    public int countAll() throws SQLException {
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM diseases")) {
            return rs.next() ? rs.getInt("cnt") : 0;
        }
    }

    public List<Disease> findAllOrderedByName() throws SQLException {
        List<Disease> diseases = new ArrayList<>();
        String sql = "SELECT name, symptoms, causative_agent, chemical_name, application_method, " +
                "organic_alternative, duration_days, precautions FROM diseases ORDER BY name";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                diseases.add(mapRow(rs));
            }
        }
        return diseases;
    }

    public void upsertAll(List<Disease> diseases) throws SQLException {
        String upsert = "INSERT INTO diseases " +
                "(name, symptoms, causative_agent, chemical_name, application_method, organic_alternative, duration_days, precautions) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(name) DO UPDATE SET " +
                "symptoms=excluded.symptoms, causative_agent=excluded.causative_agent, " +
                "chemical_name=excluded.chemical_name, application_method=excluded.application_method, " +
                "organic_alternative=excluded.organic_alternative, duration_days=excluded.duration_days, " +
                "precautions=excluded.precautions";

        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(false);
        try {
            deleteNotIn(diseases);
            try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                for (Disease disease : diseases) {
                    bindDisease(stmt, disease);
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
    }

    public void deleteByName(String name) throws SQLException {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "DELETE FROM diseases WHERE name = ?")) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        }
    }

    private void deleteNotIn(List<Disease> diseases) throws SQLException {
        if (diseases.isEmpty()) {
            try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                    "DELETE FROM diseases WHERE 1=0")) {
                stmt.executeUpdate();
            }
            return;
        }
        String placeholders = String.join(",", diseases.stream().map(d -> "?").toList());
        String sql = "DELETE FROM diseases WHERE name NOT IN (" + placeholders + ")";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            int i = 1;
            for (Disease disease : diseases) {
                stmt.setString(i++, disease.getName());
            }
            stmt.executeUpdate();
        }
    }

    private void bindDisease(PreparedStatement stmt, Disease disease) throws SQLException {
        Treatment treatment = disease.getTreatment();
        stmt.setString(1, disease.getName());
        stmt.setString(2, disease.getSymptoms());
        stmt.setString(3, disease.getCausativeAgent());
        stmt.setString(4, treatment.getChemicalName());
        stmt.setString(5, treatment.getApplicationMethod());
        stmt.setString(6, treatment.getOrganicAlternative());
        stmt.setInt(7, treatment.getDurationDays());
        stmt.setString(8, treatment.getPrecautions());
    }

    private Disease mapRow(ResultSet rs) throws SQLException {
        Treatment treatment = new Treatment(
                rs.getString("chemical_name"),
                rs.getString("application_method"),
                rs.getString("organic_alternative"),
                rs.getInt("duration_days"),
                rs.getString("precautions")
        );
        return new Disease(
                rs.getString("name"),
                rs.getString("symptoms"),
                rs.getString("causative_agent"),
                treatment
        );
    }
}
