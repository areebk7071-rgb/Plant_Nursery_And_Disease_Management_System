package com.plantmanager.repository;

import com.plantmanager.model.Disease;
import com.plantmanager.model.DiseaseLibrary;
import com.plantmanager.model.Treatment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and saves the disease library to diseases.csv.
 */
public class DiseaseRepository {

    private static final Path DISEASES_FILE = Paths.get("diseases.csv");

    public void load() throws IOException {
        if (!Files.exists(DISEASES_FILE)) {
            DiseaseLibrary.loadDefaults();
            save();
            return;
        }

        List<Disease> diseases = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(DISEASES_FILE)) {
            String line = reader.readLine();
            if (line == null) {
                DiseaseLibrary.loadDefaults();
                save();
                return;
            }
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Disease disease = parseLine(line);
                if (disease != null) {
                    diseases.add(disease);
                }
            }
        }

        if (diseases.isEmpty()) {
            DiseaseLibrary.loadDefaults();
        } else {
            DiseaseLibrary.replaceAll(diseases);
        }
    }

    public void save() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(DISEASES_FILE)) {
            writer.write(getCsvHeader());
            writer.newLine();
            for (Disease disease : DiseaseLibrary.getAll()) {
                writer.write(toCsvRow(disease));
                writer.newLine();
            }
        }
    }

    public static String getCsvHeader() {
        return "name,symptoms,causativeAgent,chemicalName,applicationMethod,organicAlternative,durationDays,precautions";
    }

    private Disease parseLine(String line) {
        try {
            String[] parts = CsvUtils.parseLine(line);
            if (parts.length < 8) {
                return null;
            }
            int duration = parts[6].isBlank() ? 0 : Integer.parseInt(parts[6].trim());
            Treatment treatment = new Treatment(
                    parts[3].trim(),
                    parts[4].trim(),
                    parts[5].trim(),
                    duration,
                    parts[7].trim()
            );
            return new Disease(parts[0].trim(), parts[1].trim(), parts[2].trim(), treatment);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String toCsvRow(Disease disease) {
        Treatment treatment = disease.getTreatment();
        return CsvUtils.quote(disease.getName()) + "," +
                CsvUtils.quote(disease.getSymptoms()) + "," +
                CsvUtils.quote(disease.getCausativeAgent()) + "," +
                CsvUtils.quote(treatment.getChemicalName()) + "," +
                CsvUtils.quote(treatment.getApplicationMethod()) + "," +
                CsvUtils.quote(treatment.getOrganicAlternative()) + "," +
                treatment.getDurationDays() + "," +
                CsvUtils.quote(treatment.getPrecautions());
    }
}
