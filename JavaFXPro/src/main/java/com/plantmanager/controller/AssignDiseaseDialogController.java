package com.plantmanager.controller;

import com.plantmanager.model.Disease;
import com.plantmanager.model.DiseaseLibrary;
import com.plantmanager.model.Plant;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class AssignDiseaseDialogController {

    @FXML private Label plantNameLabel;
    @FXML private ComboBox<Disease> diseaseCombo;

    private Plant plant;
    private boolean confirmed;

    @FXML
    private void initialize() {
        refreshDiseaseCombo();
    }

    private void refreshDiseaseCombo() {
        Disease current = diseaseCombo.getValue();
        diseaseCombo.getItems().setAll(DiseaseLibrary.getAll());
        if (current != null) {
            diseaseCombo.getItems().stream()
                    .filter(d -> d.getName().equals(current.getName()))
                    .findFirst()
                    .ifPresent(diseaseCombo::setValue);
        }
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
        refreshDiseaseCombo();
        plantNameLabel.setText("Select disease for: " + plant.getName());
        if (plant.getAssignedDisease() != null) {
            String assignedName = plant.getAssignedDisease().getName();
            diseaseCombo.getItems().stream()
                    .filter(d -> d.getName().equals(assignedName))
                    .findFirst()
                    .ifPresent(diseaseCombo::setValue);
        } else if (!diseaseCombo.getItems().isEmpty()) {
            diseaseCombo.getSelectionModel().selectFirst();
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Disease getSelectedDisease() {
        return diseaseCombo.getValue();
    }

    @FXML
    private void handleOk() {
        if (diseaseCombo.getValue() != null) {
            confirmed = true;
            diseaseCombo.getScene().getWindow().hide();
        }
    }

    @FXML
    private void handleClear() {
        confirmed = true;
        diseaseCombo.setValue(null);
        diseaseCombo.getScene().getWindow().hide();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        diseaseCombo.getScene().getWindow().hide();
    }
}
