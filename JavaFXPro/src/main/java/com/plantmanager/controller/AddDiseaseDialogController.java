package com.plantmanager.controller;

import com.plantmanager.model.Disease;
import com.plantmanager.model.Treatment;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AddDiseaseDialogController {

    @FXML private TextField nameField;
    @FXML private TextArea symptomsArea;
    @FXML private TextField causativeAgentField;
    @FXML private TextField chemicalNameField;
    @FXML private TextArea applicationMethodArea;
    @FXML private TextField organicAlternativeField;
    @FXML private TextField durationField;
    @FXML private TextArea precautionsArea;
    @FXML private Label validationLabel;

    private boolean confirmed;
    private Disease disease;

    public boolean isConfirmed() {
        return confirmed;
    }

    public Disease getDisease() {
        return disease;
    }

    @FXML
    private void handleSave() {
        if (!validate()) {
            return;
        }
        int duration = durationField.getText().trim().isEmpty()
                ? 0
                : Integer.parseInt(durationField.getText().trim());

        Treatment treatment = new Treatment(
                chemicalNameField.getText().trim(),
                applicationMethodArea.getText().trim(),
                organicAlternativeField.getText().trim(),
                duration,
                precautionsArea.getText().trim()
        );

        disease = new Disease(
                nameField.getText().trim(),
                symptomsArea.getText().trim(),
                causativeAgentField.getText().trim(),
                treatment
        );
        confirmed = true;
        nameField.getScene().getWindow().hide();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        nameField.getScene().getWindow().hide();
    }

    private boolean validate() {
        validationLabel.setText("");

        if (nameField.getText().trim().isEmpty()) {
            return warn("Disease name is required.");
        }
        if (symptomsArea.getText().trim().isEmpty()) {
            return warn("Symptoms are required.");
        }
        if (causativeAgentField.getText().trim().isEmpty()) {
            return warn("Causative agent is required.");
        }
        if (chemicalNameField.getText().trim().isEmpty()) {
            return warn("Chemical treatment name is required.");
        }
        if (applicationMethodArea.getText().trim().isEmpty()) {
            return warn("Application method is required.");
        }
        if (organicAlternativeField.getText().trim().isEmpty()) {
            return warn("Organic alternative is required.");
        }
        if (!durationField.getText().trim().isEmpty()) {
            try {
                int duration = Integer.parseInt(durationField.getText().trim());
                if (duration < 0) {
                    return warn("Duration cannot be negative.");
                }
            } catch (NumberFormatException e) {
                return warn("Duration must be a valid number (0 = ongoing).");
            }
        }
        return true;
    }

    private boolean warn(String message) {
        validationLabel.setText(message);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        return false;
    }
}
