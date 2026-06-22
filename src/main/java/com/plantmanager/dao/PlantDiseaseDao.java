package com.plantmanager.dao;

import com.plantmanager.repository.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Data access for the {@code plant_diseases} junction table linking plants to diseases.
 */
public class PlantDiseaseDao {

    public void syncAssignment(int plantId, String diseaseName) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement delete = conn.prepareStatement(
                "DELETE FROM plant_diseases WHERE plant_id = ?")) {
            delete.setInt(1, plantId);
            delete.executeUpdate();
        }
        if (diseaseName != null && !diseaseName.isBlank()) {
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO plant_diseases (plant_id, disease_name) VALUES (?, ?)")) {
                insert.setInt(1, plantId);
                insert.setString(2, diseaseName.trim());
                insert.executeUpdate();
            }
        }
    }

    public Optional<String> findDiseaseNameForPlant(int plantId) throws SQLException {
        String sql = "SELECT disease_name FROM plant_diseases WHERE plant_id = ? LIMIT 1";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, plantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString("disease_name"));
                }
            }
        }
        return Optional.empty();
    }

    public Map<Integer, String> findAllAssignments() throws SQLException {
        Map<Integer, String> assignments = new HashMap<>();
        String sql = "SELECT plant_id, disease_name FROM plant_diseases";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                assignments.put(rs.getInt("plant_id"), rs.getString("disease_name"));
            }
        }
        return assignments;
    }

    public void removeAssignmentsNotIn(java.util.Collection<Integer> plantIds) throws SQLException {
        if (plantIds.isEmpty()) {
            try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                    "DELETE FROM plant_diseases")) {
                stmt.executeUpdate();
            }
            return;
        }
        String placeholders = String.join(",", plantIds.stream().map(id -> "?").toList());
        String sql = "DELETE FROM plant_diseases WHERE plant_id NOT IN (" + placeholders + ")";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            int i = 1;
            for (int plantId : plantIds) {
                stmt.setInt(i++, plantId);
            }
            stmt.executeUpdate();
        }
    }
}
