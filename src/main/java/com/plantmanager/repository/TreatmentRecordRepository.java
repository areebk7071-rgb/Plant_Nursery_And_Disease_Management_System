package com.plantmanager.repository;

import com.plantmanager.model.TreatmentEffectiveness;
import com.plantmanager.model.TreatmentRecord;
import com.plantmanager.model.TreatmentStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs full CRUD (Create, Read, Update, Delete) for treatment records
 * against the SQLite {@code treatment_records} table via JDBC.
 */
public class TreatmentRecordRepository {

    /** READ — loads every treatment record from the database. */
    public ObservableList<TreatmentRecord> load() throws IOException {
        try {
            Connection conn = DatabaseManager.getConnection();
            List<TreatmentRecord> records = new ArrayList<>();
            String sql = "SELECT id, plant_id, plant_name, plant_type, disease_name, treatment_name, " +
                    "duration_days, start_date, end_date, status, effectiveness, applications_count " +
                    "FROM treatment_records ORDER BY id";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRow(rs));
                }
            }
            return FXCollections.observableArrayList(records);
        } catch (SQLException e) {
            throw new IOException("Failed to load treatment records from database", e);
        }
    }

    /**
     * CREATE/UPDATE (bulk) — replaces the entire treatment_records table with the given list,
     * matching the existing controller workflow (load once, save the full list after each change).
     */
    public void save(ObservableList<TreatmentRecord> records) throws IOException {
        String upsert = "INSERT INTO treatment_records " +
                "(id, plant_id, plant_name, plant_type, disease_name, treatment_name, duration_days, " +
                "start_date, end_date, status, effectiveness, applications_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET " +
                "plant_id=excluded.plant_id, plant_name=excluded.plant_name, plant_type=excluded.plant_type, " +
                "disease_name=excluded.disease_name, treatment_name=excluded.treatment_name, " +
                "duration_days=excluded.duration_days, start_date=excluded.start_date, " +
                "end_date=excluded.end_date, status=excluded.status, effectiveness=excluded.effectiveness, " +
                "applications_count=excluded.applications_count";

        try {
            Connection conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement(buildDeleteMissingSql(records))) {
                    int i = 1;
                    for (TreatmentRecord r : records) {
                        del.setInt(i++, r.getId());
                    }
                    del.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(upsert)) {
                    for (TreatmentRecord r : records) {
                        stmt.setInt(1, r.getId());
                        stmt.setInt(2, r.getPlantId());
                        stmt.setString(3, r.getPlantName());
                        stmt.setString(4, r.getPlantType());
                        stmt.setString(5, r.getDiseaseName());
                        stmt.setString(6, r.getTreatmentName());
                        stmt.setInt(7, r.getTreatmentDurationDays());
                        stmt.setString(8, r.getStartDate().toString());
                        stmt.setString(9, r.getEndDate() != null ? r.getEndDate().toString() : null);
                        stmt.setString(10, r.getStatus().name());
                        stmt.setString(11, r.getEffectiveness().name());
                        stmt.setInt(12, r.getApplicationsCount());
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
            throw new IOException("Failed to save treatment records to database", e);
        }
    }

    /** DELETE — removes a single treatment record by id. */
    public void deleteRecord(int recordId) throws IOException {
        String sql = "DELETE FROM treatment_records WHERE id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, recordId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IOException("Failed to delete treatment record " + recordId, e);
        }
    }

    public int nextId(ObservableList<TreatmentRecord> records) {
        return records.stream().mapToInt(TreatmentRecord::getId).max().orElse(0) + 1;
    }

    private String buildDeleteMissingSql(ObservableList<TreatmentRecord> records) {
        if (records.isEmpty()) {
            return "DELETE FROM treatment_records WHERE 1=0";
        }
        String placeholders = String.join(",", records.stream().map(r -> "?").toList());
        return "DELETE FROM treatment_records WHERE id NOT IN (" + placeholders + ")";
    }

    private TreatmentRecord mapRow(ResultSet rs) throws SQLException {
        TreatmentRecord record = new TreatmentRecord(
                rs.getInt("id"),
                rs.getInt("plant_id"),
                rs.getString("plant_name"),
                rs.getString("plant_type"),
                rs.getString("disease_name"),
                rs.getString("treatment_name"),
                rs.getInt("duration_days"),
                LocalDate.parse(rs.getString("start_date")),
                TreatmentStatus.fromString(rs.getString("status"))
        );
        String endDate = rs.getString("end_date");
        if (endDate != null && !endDate.isBlank()) {
            record.setEndDate(LocalDate.parse(endDate));
        }
        record.setEffectiveness(TreatmentEffectiveness.fromString(rs.getString("effectiveness")));
        record.setApplicationsCount(rs.getInt("applications_count"));
        return record;
    }
}
