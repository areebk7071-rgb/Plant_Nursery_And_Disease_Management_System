package com.plantmanager.controller;

import com.plantmanager.model.Plant;
import com.plantmanager.model.TreatmentEffectiveness;
import com.plantmanager.model.TreatmentRecord;
import com.plantmanager.model.TreatmentStatus;
import com.plantmanager.service.TreatmentTrackingService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.util.function.Consumer;

public class TreatmentPanelController {

    @FXML private ListView<TreatmentRecord> reminderListView;
    @FXML private Label detailTitleLabel;
    @FXML private Label progressLabel;
    @FXML private Label applicationsLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea treatmentDetailArea;
    @FXML private ComboBox<TreatmentEffectiveness> effectivenessCombo;
    @FXML private Label recommendationLabel;
    @FXML private Button applyTreatmentButton;
    @FXML private Button saveRatingButton;
    @FXML private Button completeButton;

    private ObservableList<Plant> plants;
    private ObservableList<TreatmentRecord> records;
    private Consumer<String> onStatusUpdate;
    private Runnable onRecordsChanged;

    @FXML
    private void initialize() {
        effectivenessCombo.setItems(FXCollections.observableArrayList(
                TreatmentEffectiveness.EFFECTIVE,
                TreatmentEffectiveness.PARTIAL,
                TreatmentEffectiveness.INEFFECTIVE
        ));

        reminderListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(TreatmentRecord item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getReminderSummary());
                    if (item.isOverdue()) {
                        setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #1b4332;");
                    }
                }
            }
        });

        reminderListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> showRecordDetails(selected));

        effectivenessCombo.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    TreatmentRecord record = reminderListView.getSelectionModel().getSelectedItem();
                    if (record != null && selected != null && record.getStatus() == TreatmentStatus.ACTIVE) {
                        // preview only until save
                    }
                });
    }

    public void bind(ObservableList<Plant> plants, ObservableList<TreatmentRecord> records,
                     Runnable onRecordsChanged, Consumer<String> onStatusUpdate) {
        this.plants = plants;
        this.records = records;
        this.onRecordsChanged = onRecordsChanged;
        this.onStatusUpdate = onStatusUpdate;
        refresh();
    }

    public void refresh() {
        if (records == null) {
            return;
        }
        TreatmentRecord selected = reminderListView.getSelectionModel().getSelectedItem();
        int selectedId = selected != null ? selected.getId() : -1;

        reminderListView.setItems(FXCollections.observableArrayList(
                TreatmentTrackingService.getActiveReminders(records)));

        if (selectedId >= 0) {
            reminderListView.getItems().stream()
                    .filter(r -> r.getId() == selectedId)
                    .findFirst()
                    .ifPresent(r -> reminderListView.getSelectionModel().select(r));
        } else if (!reminderListView.getItems().isEmpty()) {
            reminderListView.getSelectionModel().selectFirst();
        } else {
            showRecordDetails(null);
        }
    }

    public void selectPlant(int plantId) {
        reminderListView.getItems().stream()
                .filter(r -> r.getPlantId() == plantId)
                .findFirst()
                .ifPresent(r -> reminderListView.getSelectionModel().select(r));
    }

    @FXML
    private void handleApplyTreatment() {
        TreatmentRecord record = reminderListView.getSelectionModel().getSelectedItem();
        if (record == null) {
            return;
        }
        applyTreatment(record);
    }

    public void applyTreatment(TreatmentRecord record) {
        if (record == null || record.getStatus() != TreatmentStatus.ACTIVE) {
            return;
        }
        TreatmentTrackingService.logApplication(record);
        notifyChange("Treatment applied for " + record.getPlantName()
                + " (" + record.getApplicationsCount() + " total)");
        refresh();
    }

    @FXML
    private void handleSaveRating() {
        TreatmentRecord record = reminderListView.getSelectionModel().getSelectedItem();
        TreatmentEffectiveness rating = effectivenessCombo.getValue();
        if (record == null || rating == null) {
            showWarning("Select a treatment and choose Effective, Partial, or Ineffective.");
            return;
        }
        TreatmentTrackingService.setEffectiveness(record, rating);
        notifyChange("Saved rating (" + rating.getLabel() + ") for " + record.getPlantName());
        refresh();
    }

    @FXML
    private void handleComplete() {
        TreatmentRecord record = reminderListView.getSelectionModel().getSelectedItem();
        TreatmentEffectiveness rating = effectivenessCombo.getValue();
        if (record == null) {
            return;
        }
        if (rating == null) {
            showWarning("Choose effectiveness (Effective / Partial / Ineffective) before completing.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Complete Treatment");
        confirm.setHeaderText("Mark treatment complete for " + record.getPlantName() + "?");
        confirm.setContentText("Rating: " + rating.getLabel() + "\nThe plant will remain assigned the disease unless you clear it separately.");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == javafx.scene.control.ButtonType.OK) {
                TreatmentTrackingService.completeTreatment(record, rating);
                notifyChange("Treatment completed for " + record.getPlantName());
                refresh();
            }
        });
    }

    private void showRecordDetails(TreatmentRecord record) {
        if (record == null) {
            detailTitleLabel.setText("Select a treatment reminder");
            progressLabel.setText("");
            applicationsLabel.setText("");
            statusLabel.setText("");
            treatmentDetailArea.clear();
            recommendationLabel.setText("Assign a disease to a plant to start tracking treatments.");
            effectivenessCombo.setDisable(true);
            applyTreatmentButton.setDisable(true);
            saveRatingButton.setDisable(true);
            completeButton.setDisable(true);
            return;
        }

        Plant plant = findPlant(record.getPlantId());
        detailTitleLabel.setText(record.getPlantName() + " — " + record.getDiseaseName());
        progressLabel.setText("Progress: " + record.getProgressText()
                + (record.isOverdue() ? "  (OVERDUE — extend or complete)" : ""));
        applicationsLabel.setText("Applications logged: " + record.getApplicationsCount());
        statusLabel.setText("Status: " + record.getStatus().name()
                + "  |  Current rating: " + record.getEffectiveness().getLabel());

        if (plant != null && plant.hasDisease()) {
            treatmentDetailArea.setText(plant.getAssignedDisease().getFormattedDetails());
        } else {
            treatmentDetailArea.setText("Treatment: " + record.getTreatmentName()
                    + "\nDisease: " + record.getDiseaseName());
        }

        TreatmentTrackingService.recommendTreatment(record.getDiseaseName(), record.getPlantType(), records)
                .ifPresentOrElse(
                        rec -> recommendationLabel.setText("Data-driven tip: " + rec.getSummary()),
                        () -> recommendationLabel.setText(
                                "No historical data yet — complete treatments with ratings to unlock recommendations."));

        boolean active = record.getStatus() == TreatmentStatus.ACTIVE;
        effectivenessCombo.setDisable(!active);
        applyTreatmentButton.setDisable(!active);
        saveRatingButton.setDisable(!active);
        completeButton.setDisable(!active);

        if (record.getEffectiveness() != TreatmentEffectiveness.NONE) {
            effectivenessCombo.setValue(record.getEffectiveness());
        } else {
            effectivenessCombo.getSelectionModel().clearSelection();
        }
    }

    private Plant findPlant(int plantId) {
        if (plants == null) {
            return null;
        }
        return plants.stream().filter(p -> p.getId() == plantId).findFirst().orElse(null);
    }

    private void notifyChange(String message) {
        if (onRecordsChanged != null) {
            onRecordsChanged.run();
        }
        if (onStatusUpdate != null) {
            onStatusUpdate.accept(message);
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Treatment Tracker");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
