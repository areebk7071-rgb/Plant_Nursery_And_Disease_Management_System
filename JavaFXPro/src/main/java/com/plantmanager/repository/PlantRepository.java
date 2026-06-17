package com.plantmanager.repository;

import com.plantmanager.model.DiseaseLibrary;
import com.plantmanager.model.FlowerPlant;
import com.plantmanager.model.FruitPlant;
import com.plantmanager.model.HerbPlant;
import com.plantmanager.model.Plant;
import com.plantmanager.model.PlantIcon;
import com.plantmanager.model.PlantFactory;
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

/**
 * Handles CSV load/save for plant data.
 */
public class PlantRepository {

    private static final Path PLANTS_FILE = Paths.get("plants.csv");

    public ObservableList<Plant> loadPlants() throws IOException {
        if (!Files.exists(PLANTS_FILE)) {
            ObservableList<Plant> sample = createSampleData();
            savePlants(sample);
            return sample;
        }

        List<Plant> plants = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(PLANTS_FILE)) {
            String line = reader.readLine(); // skip header
            if (line == null) {
                return FXCollections.observableArrayList();
            }
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = CsvUtils.parseLine(line);
                Plant plant = PlantFactory.fromCsv(parts);
                if (plant != null) {
                    plants.add(plant);
                }
            }
        }
        return FXCollections.observableArrayList(plants);
    }

    public void savePlants(ObservableList<Plant> plants) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(PLANTS_FILE)) {
            writer.write(Plant.getCsvHeader());
            writer.newLine();
            for (Plant plant : plants) {
                writer.write(plant.toCsvRow());
                writer.newLine();
            }
        }
    }

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
