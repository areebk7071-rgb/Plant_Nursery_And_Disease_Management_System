package com.plantmanager.repository;

import com.plantmanager.dao.DatabaseInitializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the single SQLite connection used across all repositories and
 * creates the database schema on first run.
 *
 * <p>The application uses one embedded SQLite file ({@code botanical_treatment_advisor.db})
 * stored in the working directory, so no separate database server needs to be
 * installed or started. All CRUD operations (Create, Read, Update, Delete) for
 * plants, diseases, users, and treatment records are performed against this
 * database via JDBC PreparedStatements.</p>
 */
public final class DatabaseManager {

    private static final String DB_FILE = "botanical_treatment_advisor.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;

    private static Connection connection;

    private DatabaseManager() {
    }

    /**
     * Returns the single shared connection, creating it (and the schema, if needed)
     * on first call.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            initializeSchema();
        }
        return connection;
    }

    private static void initializeSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            // USERS table — login and registration
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    username      TEXT PRIMARY KEY,
                    password_hash TEXT NOT NULL,
                    display_name  TEXT NOT NULL
                )
            """);

            // DISEASES table — disease library
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS diseases (
                    name                TEXT PRIMARY KEY,
                    symptoms            TEXT,
                    causative_agent     TEXT,
                    chemical_name       TEXT,
                    application_method  TEXT,
                    organic_alternative TEXT,
                    duration_days       INTEGER,
                    precautions         TEXT
                )
            """);

            // PLANTS table — Fruit/Flower/Herb plants (single-table inheritance)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS plants (
                    id                    INTEGER PRIMARY KEY,
                    name                  TEXT NOT NULL,
                    species               TEXT NOT NULL,
                    planted_date          TEXT NOT NULL,
                    plant_type            TEXT NOT NULL,
                    assigned_disease_name TEXT,
                    expected_yield        REAL,
                    harvest_period        TEXT,
                    bloom_color           TEXT,
                    is_perennial          INTEGER,
                    culinary_use          TEXT,
                    is_medicinal          INTEGER,
                    image_ref             TEXT,
                    FOREIGN KEY (assigned_disease_name) REFERENCES diseases(name)
                        ON DELETE SET NULL ON UPDATE CASCADE
                )
            """);

            // PLANT_DISEASES junction table — links plants to diseases (many-to-many capable)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS plant_diseases (
                    plant_id      INTEGER NOT NULL,
                    disease_name  TEXT NOT NULL,
                    assigned_at   TEXT DEFAULT (datetime('now')),
                    PRIMARY KEY (plant_id, disease_name),
                    FOREIGN KEY (plant_id) REFERENCES plants(id)
                        ON DELETE CASCADE ON UPDATE CASCADE,
                    FOREIGN KEY (disease_name) REFERENCES diseases(name)
                        ON DELETE CASCADE ON UPDATE CASCADE
                )
            """);

            // TREATMENT_RECORDS table — treatment history per plant
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS treatment_records (
                    id                  INTEGER PRIMARY KEY,
                    plant_id            INTEGER NOT NULL,
                    plant_name          TEXT,
                    plant_type          TEXT,
                    disease_name        TEXT,
                    treatment_name      TEXT,
                    duration_days       INTEGER,
                    start_date          TEXT,
                    end_date            TEXT,
                    status              TEXT,
                    effectiveness       TEXT,
                    applications_count  INTEGER,
                    FOREIGN KEY (plant_id) REFERENCES plants(id)
                        ON DELETE CASCADE ON UPDATE CASCADE
                )
            """);
        }
        DatabaseInitializer.initialize();
    }

    /** Closes the shared connection. Call on application shutdown if desired. */
    public static synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // nothing to do on shutdown
            }
            connection = null;
        }
    }
}
