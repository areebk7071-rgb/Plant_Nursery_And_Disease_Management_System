package com.plantmanager.service;

import com.plantmanager.dao.DiseaseDao;
import com.plantmanager.dao.PlantDao;
import com.plantmanager.model.Disease;
import com.plantmanager.model.Plant;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Loads live garden report data directly from the SQLite database for PDF generation
 * and QR sharing, independent of in-memory UI state or CSV exports.
 */
public final class GardenReportQueryService {

    private final PlantDao plantDao = new PlantDao();
    private final DiseaseDao diseaseDao = new DiseaseDao();

    public GardenReportSnapshot fetchSnapshot() throws IOException {
        try {
            List<Plant> plants = plantDao.findAllOrderedById();
            List<Disease> diseases = diseaseDao.findAllOrderedByName();
            return new GardenReportSnapshot(plants, diseases);
        } catch (SQLException e) {
            throw new IOException("Failed to load garden report data from database", e);
        }
    }

    public record GardenReportSnapshot(List<Plant> plants, List<Disease> diseases) {
    }
}
