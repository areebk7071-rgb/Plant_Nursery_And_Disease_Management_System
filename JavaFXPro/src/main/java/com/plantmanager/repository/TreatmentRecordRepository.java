package com.plantmanager.repository;

import com.plantmanager.model.Plant;
import com.plantmanager.model.TreatmentEffectiveness;
import com.plantmanager.model.TreatmentRecord;
import com.plantmanager.model.TreatmentStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TreatmentRecordRepository {

    private static final Path FILE = Paths.get("treatment_records.csv");

    public ObservableList<TreatmentRecord> load() throws IOException {
        if (!Files.exists(FILE)) {
            return FXCollections.observableArrayList();
        }

        List<TreatmentRecord> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(FILE)) {
            String header = reader.readLine();
            if (header == null) {
                return FXCollections.observableArrayList();
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                TreatmentRecord record = parseLine(line);
                if (record != null) {
                    records.add(record);
                }
            }
        }
        return FXCollections.observableArrayList(records);
    }

    public void save(ObservableList<TreatmentRecord> records) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(FILE)) {
            writer.write(getHeader());
            writer.newLine();
            for (TreatmentRecord record : records) {
                writer.write(toCsvRow(record));
                writer.newLine();
            }
        }
    }

    public int nextId(ObservableList<TreatmentRecord> records) {
        return records.stream().mapToInt(TreatmentRecord::getId).max().orElse(0) + 1;
    }

    public static String getHeader() {
        return "id,plantId,plantName,plantType,diseaseName,treatmentName,treatmentDurationDays," +
                "startDate,endDate,status,effectiveness,applicationsCount";
    }

    private TreatmentRecord parseLine(String line) {
        try {
            String[] p = CsvUtils.parseLine(line);
            if (p.length < 12) {
                return null;
            }
            TreatmentRecord record = new TreatmentRecord(
                    Integer.parseInt(p[0].trim()),
                    Integer.parseInt(p[1].trim()),
                    p[2].trim(),
                    p[3].trim(),
                    p[4].trim(),
                    p[5].trim(),
                    Integer.parseInt(p[6].trim()),
                    LocalDate.parse(p[7].trim()),
                    TreatmentStatus.fromString(p[9].trim())
            );
            if (!p[8].trim().isEmpty()) {
                record.setEndDate(LocalDate.parse(p[8].trim()));
            }
            record.setEffectiveness(TreatmentEffectiveness.fromString(p[10].trim()));
            record.setApplicationsCount(Integer.parseInt(p[11].trim()));
            return record;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String toCsvRow(TreatmentRecord r) {
        return r.getId() + "," +
                r.getPlantId() + "," +
                CsvUtils.quote(r.getPlantName()) + "," +
                CsvUtils.quote(r.getPlantType()) + "," +
                CsvUtils.quote(r.getDiseaseName()) + "," +
                CsvUtils.quote(r.getTreatmentName()) + "," +
                r.getTreatmentDurationDays() + "," +
                r.getStartDate() + "," +
                (r.getEndDate() != null ? r.getEndDate() : "") + "," +
                r.getStatus().name() + "," +
                r.getEffectiveness().name() + "," +
                r.getApplicationsCount();
    }
}
