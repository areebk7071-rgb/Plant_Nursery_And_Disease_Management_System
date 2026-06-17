package com.plantmanager.controller;

import com.plantmanager.model.Disease;
import com.plantmanager.model.Treatment;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TreatmentDialogController {

    @FXML private Label headerIconLabel;
    @FXML private Label diseaseNameLabel;
    @FXML private Label contextLabel;
    @FXML private Label symptomsLabel;
    @FXML private Label agentLabel;
    @FXML private Label chemicalNameLabel;
    @FXML private Label applicationLabel;
    @FXML private Label organicLabel;
    @FXML private Label durationBadgeLabel;
    @FXML private Label precautionsLabel;
    @FXML private VBox precautionsCard;

    public void setDisease(Disease disease, String plantName) {
        Treatment treatment = disease.getTreatment();

        diseaseNameLabel.setText(disease.getName());
        if (plantName != null && !plantName.isBlank()) {
            contextLabel.setText("Treatment plan for " + plantName);
            headerIconLabel.setText("🌱");
        } else {
            contextLabel.setText("Disease library reference");
            headerIconLabel.setText("🦠");
        }

        symptomsLabel.setText(disease.getSymptoms());
        agentLabel.setText(disease.getCausativeAgent());
        chemicalNameLabel.setText(treatment.getChemicalName());
        applicationLabel.setText(treatment.getApplicationMethod());
        organicLabel.setText(treatment.getOrganicAlternative());

        if (treatment.getDurationDays() > 0) {
            durationBadgeLabel.setText(treatment.getDurationDays() + " days");
        } else {
            durationBadgeLabel.setText("Ongoing care");
        }

        String precautions = treatment.getPrecautions();
        if (precautions == null || precautions.isBlank()) {
            precautionsLabel.setText("No special precautions listed.");
            precautionsCard.getStyleClass().add("treatment-card-muted");
        } else {
            precautionsLabel.setText(precautions);
        }
    }

    @FXML
    private void handleClose() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) diseaseNameLabel.getScene().getWindow();
        stage.close();
    }
}
