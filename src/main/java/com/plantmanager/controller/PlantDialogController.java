package com.plantmanager.controller;

import com.plantmanager.model.FlowerPlant;
import com.plantmanager.model.FruitPlant;
import com.plantmanager.model.HerbPlant;
import com.plantmanager.model.Plant;
import com.plantmanager.model.PlantIcon;
import com.plantmanager.model.TreePlant;
import com.plantmanager.model.VegetablePlant;
import com.plantmanager.model.VinePlant;
import com.plantmanager.service.PlantImageService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class PlantDialogController {

    private static final List<String> ALL_TYPES = List.of("Fruit", "Flower", "Herb", "Vegetable", "Tree", "Vine");

    @FXML private TextField nameField;
    @FXML private TextField speciesField;
    @FXML private DatePicker plantedDatePicker;

    @FXML private RadioButton fruitRadio;
    @FXML private RadioButton flowerRadio;
    @FXML private RadioButton herbRadio;
    @FXML private RadioButton vegetableRadio;
    @FXML private RadioButton treeRadio;
    @FXML private RadioButton vineRadio;
    @FXML private ToggleGroup plantTypeGroup;

    @FXML private ComboBox<String> categoryCombo;

    @FXML private ImageView iconPreview;
    @FXML private ComboBox<PlantIcon> iconCombo;
    @FXML private Label customPhotoLabel;

    @FXML private VBox fruitFields;
    @FXML private TextField yieldField;
    @FXML private ComboBox<String> harvestPeriodCombo;

    @FXML private VBox flowerFields;
    @FXML private ComboBox<String> bloomColorCombo;
    @FXML private CheckBox perennialCheck;

    @FXML private VBox herbFields;
    @FXML private TextArea culinaryUseArea;
    @FXML private CheckBox medicinalCheck;

    @FXML private VBox vegetableFields;
    @FXML private ComboBox<String> rootTypeCombo;
    @FXML private TextField daysToHarvestField;

    @FXML private VBox treeFields;
    @FXML private TextField maxHeightField;
    @FXML private TextField canopySpreadField;

    @FXML private VBox vineFields;
    @FXML private ComboBox<String> climbingSupportCombo;
    @FXML private ComboBox<String> growthRateCombo;
    @FXML private TextField maxVineLengthField;

    @FXML private Label validationLabel;

    private Plant existingPlant;
    private int nextId = 1;
    private boolean confirmed;
    private Path pendingCustomPhoto;
    private String existingCustomRef = "";

    @FXML
    private void initialize() {
        harvestPeriodCombo.getItems().addAll("Spring", "Summer", "Autumn", "Winter");
        harvestPeriodCombo.setValue("Summer");

        bloomColorCombo.getItems().addAll("Red", "Pink", "White", "Yellow", "Purple", "Orange", "Mixed");
        bloomColorCombo.setValue("Mixed");

        rootTypeCombo.getItems().addAll("Taproot", "Fibrous", "Tuber", "Bulb", "Rhizome");
        rootTypeCombo.setValue("Taproot");

        climbingSupportCombo.getItems().addAll("Trellis", "Arbor", "Wall", "Ground Cover", "Pergola");
        climbingSupportCombo.setValue("Trellis");

        growthRateCombo.getItems().addAll("Slow", "Medium", "Fast");
        growthRateCombo.setValue("Medium");

        plantedDatePicker.setValue(LocalDate.now());
        iconPreview.setFitWidth(64);
        iconPreview.setFitHeight(64);
        iconPreview.setPreserveRatio(true);

        plantTypeGroup.selectedToggleProperty().addListener((obs, old, selected) -> {
            updateVisibleFields();
            refreshIconOptions(false);
            refreshCategoryOptions();
        });

        iconCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null && pendingCustomPhoto == null) {
                iconPreview.setImage(PlantImageService.getBuiltinIcon(selected));
                customPhotoLabel.setText("");
            }
        });

        updateVisibleFields();
        refreshIconOptions(true);
        refreshCategoryOptions();
    }

    public void setExistingPlant(Plant plant) {
        this.existingPlant = plant;
        pendingCustomPhoto = null;
        existingCustomRef = "";

        if (plant != null) {
            nameField.setText(plant.getName());
            speciesField.setText(plant.getSpecies());
            plantedDatePicker.setValue(plant.getPlantedDate());

            String ref = plant.getImageRef();
            if (ref.startsWith("file:")) {
                existingCustomRef = ref;
                iconPreview.setImage(PlantImageService.getImage(plant));
                customPhotoLabel.setText("Custom photo: " + Path.of(ref.substring(5)).getFileName());
            } else {
                PlantIcon icon = PlantIcon.fromKey(ref);
                if (icon != null) {
                    iconCombo.setValue(icon);
                    iconPreview.setImage(PlantImageService.getBuiltinIcon(icon));
                }
            }

            if (plant instanceof FruitPlant fruit) {
                fruitRadio.setSelected(true);
                yieldField.setText(String.valueOf(fruit.getExpectedYield()));
                harvestPeriodCombo.setValue(fruit.getHarvestPeriod());
            } else if (plant instanceof FlowerPlant flower) {
                flowerRadio.setSelected(true);
                bloomColorCombo.setValue(flower.getBloomColor());
                perennialCheck.setSelected(flower.isPerennial());
            } else if (plant instanceof HerbPlant herb) {
                herbRadio.setSelected(true);
                culinaryUseArea.setText(herb.getCulinaryUse());
                medicinalCheck.setSelected(herb.isMedicinal());
            } else if (plant instanceof VegetablePlant veg) {
                vegetableRadio.setSelected(true);
                rootTypeCombo.setValue(veg.getRootType());
                daysToHarvestField.setText(String.valueOf(veg.getDaysToHarvest()));
            } else if (plant instanceof TreePlant tree) {
                treeRadio.setSelected(true);
                maxHeightField.setText(String.valueOf(tree.getMaxHeight()));
                canopySpreadField.setText(String.valueOf(tree.getCanopySpread()));
            } else if (plant instanceof VinePlant vine) {
                vineRadio.setSelected(true);
                climbingSupportCombo.setValue(vine.getClimbingSupport());
                growthRateCombo.setValue(vine.getGrowthRate());
                maxVineLengthField.setText(String.valueOf(vine.getMaxVineLength()));
            }

            refreshCategoryOptions();
            for (String cat : plant.getCategories()) {
                if (!cat.equals(plant.getPlantType())) {
                    categoryCombo.setValue(cat);
                    break;
                }
            }

            refreshIconOptions(false);
        }
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Plant buildPlant() throws IOException {
        int id = existingPlant != null ? existingPlant.getId() : nextId;
        String name = nameField.getText().trim();
        String species = speciesField.getText().trim();
        LocalDate plantedDate = plantedDatePicker.getValue();

        Plant plant;
        if (fruitRadio.isSelected()) {
            plant = new FruitPlant(id, name, species, plantedDate,
                    Double.parseDouble(yieldField.getText().trim()),
                    harvestPeriodCombo.getValue());
        } else if (flowerRadio.isSelected()) {
            plant = new FlowerPlant(id, name, species, plantedDate,
                    bloomColorCombo.getValue(), perennialCheck.isSelected());
        } else if (herbRadio.isSelected()) {
            plant = new HerbPlant(id, name, species, plantedDate,
                    culinaryUseArea.getText().trim(), medicinalCheck.isSelected());
        } else if (vegetableRadio.isSelected()) {
            plant = new VegetablePlant(id, name, species, plantedDate,
                    rootTypeCombo.getValue(),
                    Integer.parseInt(daysToHarvestField.getText().trim()));
        } else if (treeRadio.isSelected()) {
            plant = new TreePlant(id, name, species, plantedDate,
                    Double.parseDouble(maxHeightField.getText().trim()),
                    Double.parseDouble(canopySpreadField.getText().trim()));
        } else {
            plant = new VinePlant(id, name, species, plantedDate,
                    climbingSupportCombo.getValue(),
                    growthRateCombo.getValue(),
                    Double.parseDouble(maxVineLengthField.getText().trim()));
        }

        String secondaryCat = categoryCombo.getValue();
        if (secondaryCat != null && !secondaryCat.isEmpty()) {
            plant.addCategory(secondaryCat);
        }

        if (pendingCustomPhoto != null) {
            plant.setImageRef(PlantImageService.saveCustomImage(id, name, pendingCustomPhoto));
        } else if (!existingCustomRef.isEmpty() && pendingCustomPhoto == null
                && customPhotoLabel.getText().startsWith("Custom")) {
            plant.setImageRef(existingCustomRef);
        } else if (iconCombo.getValue() != null) {
            plant.setImageRef(iconCombo.getValue().getKey());
        } else {
            plant.setImageRef(PlantIcon.defaultForType(plant.getPlantType()).getKey());
        }

        if (existingPlant != null && existingPlant.getAssignedDisease() != null) {
            plant.setAssignedDisease(existingPlant.getAssignedDisease());
        }
        return plant;
    }

    @FXML
    private void handleUploadPhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Plant Photo");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        java.io.File file = chooser.showOpenDialog(iconPreview.getScene().getWindow());
        if (file != null) {
            pendingCustomPhoto = file.toPath();
            existingCustomRef = "";
            iconPreview.setImage(PlantImageService.previewFile(pendingCustomPhoto));
            customPhotoLabel.setText("Custom photo selected: " + file.getName());
        }
    }

    @FXML
    private void handleClearPhoto() {
        pendingCustomPhoto = null;
        existingCustomRef = "";
        customPhotoLabel.setText("");
        refreshIconOptions(true);
    }

    @FXML
    private void handleSave() {
        if (!validate()) {
            return;
        }
        confirmed = true;
        nameField.getScene().getWindow().hide();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        nameField.getScene().getWindow().hide();
    }

    private void refreshCategoryOptions() {
        String primaryType = fruitRadio.isSelected() ? "Fruit"
                : flowerRadio.isSelected() ? "Flower"
                : herbRadio.isSelected() ? "Herb"
                : vegetableRadio.isSelected() ? "Vegetable"
                : treeRadio.isSelected() ? "Tree"
                : "Vine";

        String current = categoryCombo.getValue();
        categoryCombo.setItems(FXCollections.observableArrayList(
                ALL_TYPES.stream()
                        .filter(t -> !t.equals(primaryType))
                        .toList()
        ));
        if (current != null && !current.equals(primaryType) && ALL_TYPES.contains(current)) {
            categoryCombo.setValue(current);
        } else {
            categoryCombo.setValue(null);
        }
    }

    private void refreshIconOptions(boolean selectDefault) {
        String type = fruitRadio.isSelected() ? "Fruit"
                : flowerRadio.isSelected() ? "Flower"
                : herbRadio.isSelected() ? "Herb"
                : vegetableRadio.isSelected() ? "Vegetable"
                : treeRadio.isSelected() ? "Tree"
                : "Vine";
        PlantIcon current = iconCombo.getValue();
        iconCombo.setItems(FXCollections.observableArrayList(
                Arrays.stream(PlantIcon.values())
                        .filter(icon -> icon.getPlantType().equals(type))
                        .toList()
        ));
        if (current != null && iconCombo.getItems().contains(current)) {
            iconCombo.setValue(current);
        } else if (selectDefault || iconCombo.getValue() == null) {
            iconCombo.setValue(PlantIcon.defaultForType(type));
        }
        if (pendingCustomPhoto == null && existingCustomRef.isEmpty() && iconCombo.getValue() != null) {
            iconPreview.setImage(PlantImageService.getBuiltinIcon(iconCombo.getValue()));
        }
    }

    private boolean validate() {
        validationLabel.setText("");

        if (nameField.getText().trim().isEmpty()) {
            showValidationWarning("Plant name is required.");
            return false;
        }
        if (speciesField.getText().trim().isEmpty()) {
            showValidationWarning("Species is required.");
            return false;
        }
        if (plantedDatePicker.getValue() == null) {
            showValidationWarning("Planted date is required.");
            return false;
        }
        if (plantedDatePicker.getValue().isAfter(LocalDate.now())) {
            showValidationWarning("Planted date cannot be in the future.");
            return false;
        }

        if (fruitRadio.isSelected()) {
            try {
                double yield = Double.parseDouble(yieldField.getText().trim());
                if (yield < 0) {
                    showValidationWarning("Expected yield cannot be negative.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showValidationWarning("Expected yield must be a valid number.");
                return false;
            }
            if (harvestPeriodCombo.getValue() == null) {
                showValidationWarning("Harvest period is required.");
                return false;
            }
        } else if (flowerRadio.isSelected()) {
            if (bloomColorCombo.getValue() == null) {
                showValidationWarning("Bloom color is required.");
                return false;
            }
        } else if (herbRadio.isSelected()) {
            if (culinaryUseArea.getText().trim().isEmpty()) {
                showValidationWarning("Culinary use is required.");
                return false;
            }
        } else if (vegetableRadio.isSelected()) {
            if (rootTypeCombo.getValue() == null) {
                showValidationWarning("Root type is required.");
                return false;
            }
            try {
                int days = Integer.parseInt(daysToHarvestField.getText().trim());
                if (days < 0) {
                    showValidationWarning("Days to harvest cannot be negative.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showValidationWarning("Days to harvest must be a valid number.");
                return false;
            }
        } else if (treeRadio.isSelected()) {
            try {
                double h = Double.parseDouble(maxHeightField.getText().trim());
                if (h < 0) {
                    showValidationWarning("Max height cannot be negative.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showValidationWarning("Max height must be a valid number.");
                return false;
            }
            try {
                double s = Double.parseDouble(canopySpreadField.getText().trim());
                if (s < 0) {
                    showValidationWarning("Canopy spread cannot be negative.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showValidationWarning("Canopy spread must be a valid number.");
                return false;
            }
        } else if (vineRadio.isSelected()) {
            if (climbingSupportCombo.getValue() == null) {
                showValidationWarning("Climbing support is required.");
                return false;
            }
            if (growthRateCombo.getValue() == null) {
                showValidationWarning("Growth rate is required.");
                return false;
            }
            try {
                double l = Double.parseDouble(maxVineLengthField.getText().trim());
                if (l < 0) {
                    showValidationWarning("Max vine length cannot be negative.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showValidationWarning("Max vine length must be a valid number.");
                return false;
            }
        }

        return true;
    }

    private void showValidationWarning(String message) {
        validationLabel.setText(message);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateVisibleFields() {
        fruitFields.setVisible(fruitRadio.isSelected());
        fruitFields.setManaged(fruitRadio.isSelected());
        flowerFields.setVisible(flowerRadio.isSelected());
        flowerFields.setManaged(flowerRadio.isSelected());
        herbFields.setVisible(herbRadio.isSelected());
        herbFields.setManaged(herbRadio.isSelected());
        vegetableFields.setVisible(vegetableRadio.isSelected());
        vegetableFields.setManaged(vegetableRadio.isSelected());
        treeFields.setVisible(treeRadio.isSelected());
        treeFields.setManaged(treeRadio.isSelected());
        vineFields.setVisible(vineRadio.isSelected());
        vineFields.setManaged(vineRadio.isSelected());
    }
}
