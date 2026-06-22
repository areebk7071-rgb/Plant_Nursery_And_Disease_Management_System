package com.plantmanager.repository;

import com.plantmanager.dao.DiseaseDao;
import com.plantmanager.model.Disease;
import com.plantmanager.model.DiseaseLibrary;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository facade over {@link DiseaseDao} for the disease library.
 */
public class DiseaseRepository {

    private final DiseaseDao diseaseDao = new DiseaseDao();

    /** READ — loads all diseases from the database into {@link DiseaseLibrary}. */
    public void load() throws IOException {
        try {
            List<Disease> diseases = diseaseDao.findAllOrderedByName();
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
        try {
            diseaseDao.upsertAll(DiseaseLibrary.getAll());
        } catch (SQLException e) {
            throw new IOException("Failed to save diseases to database", e);
        }
    }

    /** DELETE — removes a single disease by name. */
    public void deleteDisease(String name) throws IOException {
        try {
            diseaseDao.deleteByName(name);
        } catch (SQLException e) {
            throw new IOException("Failed to delete disease " + name, e);
        }
    }
}
