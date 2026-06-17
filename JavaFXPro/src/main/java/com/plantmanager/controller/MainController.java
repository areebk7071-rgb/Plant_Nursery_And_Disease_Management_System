package com.plantmanager.controller;

import com.plantmanager.model.Disease;
import com.plantmanager.model.DiseaseLibrary;
import com.plantmanager.model.FlowerPlant;
import com.plantmanager.model.FruitPlant;
import com.plantmanager.model.HerbPlant;
import com.plantmanager.model.Plant;
import com.plantmanager.model.TreatmentRecord;
import com.plantmanager.model.User;
import com.plantmanager.service.TreatmentTrackingService;
import com.plantmanager.repository.PlantRepository;
import com.plantmanager.repository.DiseaseRepository;
import com.plantmanager.repository.TreatmentRecordRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import com.plantmanager.service.PdfReportService;
import com.plantmanager.service.QrShareService;
import com.plantmanager.service.PlantImageService;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class MainController {

    @FXML private TableView<Plant> plantTable;
    @FXML private TableColumn<Plant, Plant> colImage;
    @FXML private TableColumn<Plant, Number> colId;
    @FXML private TableColumn<Plant, String> colName;
    @FXML private TableColumn<Plant, String> colSpecies;
    @FXML private TableColumn<Plant, String> colType;
    @FXML private TableColumn<Plant, String> colDisease;
    @FXML private TableColumn<Plant, String> colPlantedDate;
    @FXML private TableColumn<Plant, String> colHealth;

    @FXML private TextField searchField;
    @FXML private ListView<Disease> diseaseListView;
    @FXML private TextArea diseaseDetailArea;
    @FXML private Label treatmentReminderLabel;
    @FXML private Label statusLabel;
    @FXML private Label userLabel;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button assignButton;
    @FXML private Button showTreatmentButton;
    @FXML private Button trackTreatmentButton;
    @FXML private Button viewTreatmentButton;
    @FXML private Button addDiseaseButton;

    @FXML private MenuItem exportMenuItem;
    @FXML private MenuItem exportPdfMenuItem;
    @FXML private MenuItem shareCsvQrMenuItem;
    @FXML private MenuItem sharePdfQrMenuItem;
    @FXML private MenuItem logoutMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem aboutMenuItem;

    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton navDashboard;
    @FXML private ToggleButton navPlants;
    @FXML private ToggleButton navDiseases;
    @FXML private ToggleButton navTreatments;
    @FXML private StackPane contentStack;
    @FXML private VBox dashboardPanel;
    @FXML private VBox plantsPanel;
    @FXML private VBox diseasesPanel;
    @FXML private VBox treatmentsPanel;

    @FXML private DashboardController dashboardController;
    @FXML private TreatmentPanelController treatmentPanelController;

    private PlantRepository repository;
    private DiseaseRepository diseaseRepository;
    private TreatmentRecordRepository treatmentRecordRepository;
    private ObservableList<Plant> plants;
    private ObservableList<TreatmentRecord> treatmentRecords;
    private FilteredList<Plant> filteredPlants;
    private Runnable onLogout;

    public void initializeData(PlantRepository repository, DiseaseRepository diseaseRepository,
                               TreatmentRecordRepository treatmentRecordRepository,
                               User currentUser, Runnable onLogout) {
        this.onLogout = onLogout;
        if (userLabel != null) {
            userLabel.setText("Signed in as " + currentUser.getDisplayName());
        }
        this.repository = repository;
        this.diseaseRepository = diseaseRepository;
        this.treatmentRecordRepository = treatmentRecordRepository;
        try {
            plants = repository.loadPlants();
        } catch (IOException e) {
            showError("Failed to load plants", e.getMessage());
            plants = FXCollections.observableArrayList();
        }

        try {
            treatmentRecords = treatmentRecordRepository.load();
            syncTreatmentRecords();
            saveTreatmentRecords();
        } catch (IOException e) {
            showError("Failed to load treatment records", e.getMessage());
            treatmentRecords = FXCollections.observableArrayList();
        }

        setupTable();
        refreshDiseaseLibrary();
        setupFiltering();
        setupSelectionListeners();
        updateButtonStates();

        if (treatmentPanelController != null) {
            treatmentPanelController.bind(plants, treatmentRecords,
                    this::saveTreatmentRecordsAndRefresh, statusLabel::setText);
        }

        refreshDashboard();
        statusLabel.setText(plants.size() + " plant(s) loaded");
    }

    private void syncTreatmentRecords() {
        for (Plant plant : plants) {
            if (plant.hasDisease()
                    && TreatmentTrackingService.findActive(plant.getId(), treatmentRecords).isEmpty()) {
                treatmentRecords.add(TreatmentRecord.fromPlant(plant,
                        treatmentRecordRepository.nextId(treatmentRecords)));
            }
        }
    }

    private void saveTreatmentRecordsAndRefresh() {
        saveTreatmentRecords();
        if (treatmentPanelController != null) {
            treatmentPanelController.refresh();
        }
        refreshDashboard();
        Plant selected = plantTable.getSelectionModel().getSelectedItem();
        updateTreatmentReminder(selected);
    }

    private void saveTreatmentRecords() {
        try {
            treatmentRecordRepository.save(treatmentRecords);
        } catch (IOException e) {
            showError("Failed to save treatment records", e.getMessage());
        }
    }

    private void refreshDashboard() {
        if (dashboardController != null) {
            dashboardController.setTreatmentCallbacks(
                    treatmentRecords,
                    this::applyTreatmentFromDashboard,
                    this::openTreatmentDetails);
            dashboardController.refresh(plants);
        }
    }

    private void applyTreatmentFromDashboard(TreatmentRecord record) {
        if (treatmentPanelController != null) {
            treatmentPanelController.applyTreatment(record);
        } else {
            com.plantmanager.service.TreatmentTrackingService.logApplication(record);
            saveTreatmentRecordsAndRefresh();
            statusLabel.setText("Treatment applied for " + record.getPlantName());
        }
    }

    private void openTreatmentDetails(TreatmentRecord record) {
        navTreatments.setSelected(true);
        if (treatmentPanelController != null) {
            treatmentPanelController.selectPlant(record.getPlantId());
        }
    }

    @FXML
    private void initialize() {
        diseaseListView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                diseaseDetailArea.setText(selected.getFormattedDetails());
            }
        });

        navDashboard.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        navPlants.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        navDiseases.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        navTreatments.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        navGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                navDashboard.setSelected(true);
                return;
            }
            if (newToggle == navDashboard) {
                showPanel(dashboardPanel);
            } else if (newToggle == navPlants) {
                showPanel(plantsPanel);
                updateButtonStates();
            } else if (newToggle == navDiseases) {
                showPanel(diseasesPanel);
            } else if (newToggle == navTreatments) {
                showPanel(treatmentsPanel);
                if (treatmentPanelController != null) {
                    treatmentPanelController.refresh();
                }
            }
            updateActionBarForView(newToggle);
            refreshNavButtonStyles();
        });

        navDashboard.setSelected(true);
        refreshNavButtonStyles();
    }

    private void refreshNavButtonStyles() {
        applyNavStyle(navDashboard, navDashboard.isSelected());
        applyNavStyle(navPlants, navPlants.isSelected());
        applyNavStyle(navDiseases, navDiseases.isSelected());
        applyNavStyle(navTreatments, navTreatments.isSelected());
    }

    private void applyNavStyle(ToggleButton button, boolean selected) {
        if (button.getGraphic() instanceof javafx.scene.layout.HBox inner) {
            for (Node node : inner.getChildren()) {
                if (node instanceof VBox titles) {
                    for (Node child : titles.getChildren()) {
                        if (child instanceof Label label) {
                            if (label.getStyleClass().contains("nav-title")) {
                                label.setStyle(selected
                                        ? "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;"
                                        : "-fx-text-fill: #1b4332; -fx-font-size: 15px; -fx-font-weight: bold;");
                            } else if (label.getStyleClass().contains("nav-sub")) {
                                label.setStyle(selected
                                        ? "-fx-text-fill: #d8f3dc; -fx-font-size: 11px;"
                                        : "-fx-text-fill: #5c6f63; -fx-font-size: 11px;");
                            }
                        }
                    }
                }
            }
        }
    }

    private void showPanel(VBox panel) {
        dashboardPanel.setVisible(false);
        plantsPanel.setVisible(false);
        diseasesPanel.setVisible(false);
        treatmentsPanel.setVisible(false);
        panel.setVisible(true);
    }

    private void updateActionBarForView(javafx.scene.control.Toggle toggle) {
        boolean plantsView = toggle == navPlants;
        addButton.setVisible(plantsView);
        addButton.setManaged(plantsView);
        editButton.setVisible(plantsView);
        editButton.setManaged(plantsView);
        deleteButton.setVisible(plantsView);
        deleteButton.setManaged(plantsView);
        assignButton.setVisible(plantsView);
        assignButton.setManaged(plantsView);
        showTreatmentButton.setVisible(plantsView);
        showTreatmentButton.setManaged(plantsView);
        trackTreatmentButton.setVisible(plantsView);
        trackTreatmentButton.setManaged(plantsView);
        if (!plantsView) {
            treatmentReminderLabel.setText("");
        }
    }

    private void setupTable() {
        filteredPlants = new FilteredList<>(plants, p -> true);
        plantTable.setItems(filteredPlants);

        colImage.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyObjectWrapper<>(data.getValue()));
        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
                setGraphic(imageView);
                setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Plant plant, boolean empty) {
                super.updateItem(plant, empty);
                if (empty || plant == null) {
                    imageView.setImage(null);
                } else {
                    imageView.setImage(PlantImageService.getImage(plant));
                }
            }
        });

        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colSpecies.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSpecies()));
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlantType()));
        colDisease.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAssignedDiseaseName()));
        colPlantedDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlantedDate().toString()));
        colHealth.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHealthStatus()));
        colHealth.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Healthy".equals(item)) {
                        setStyle("-fx-text-fill: #2d6a4f; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e76f51; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void refreshDiseaseLibrary() {
        Disease selected = diseaseListView.getSelectionModel().getSelectedItem();
        String selectedName = selected != null ? selected.getName() : null;

        diseaseListView.getItems().setAll(DiseaseLibrary.getAll());

        if (selectedName != null) {
            diseaseListView.getItems().stream()
                    .filter(d -> d.getName().equals(selectedName))
                    .findFirst()
                    .ifPresent(d -> diseaseListView.getSelectionModel().select(d));
        } else if (!diseaseListView.getItems().isEmpty()) {
            diseaseListView.getSelectionModel().selectFirst();
        }

        Disease current = diseaseListView.getSelectionModel().getSelectedItem();
        diseaseDetailArea.setText(current != null ? current.getFormattedDetails() : "");
    }

    private void setupFiltering() {
        searchField.textProperty().addListener((obs, old, text) -> {
            String filter = text == null ? "" : text.trim().toLowerCase();
            filteredPlants.setPredicate(plant -> {
                if (filter.isEmpty()) {
                    return true;
                }
                return plant.getName().toLowerCase().contains(filter)
                        || plant.getSpecies().toLowerCase().contains(filter)
                        || plant.getAssignedDiseaseName().toLowerCase().contains(filter)
                        || plant.getPlantType().toLowerCase().contains(filter);
            });
        });
    }

    private void setupSelectionListeners() {
        plantTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            updateButtonStates();
            updateTreatmentReminder(selected);
        });
    }

    private void updateTreatmentReminder(Plant selected) {
        if (selected == null || !selected.hasDisease() || treatmentRecords == null) {
            treatmentReminderLabel.setText("");
            return;
        }
        TreatmentTrackingService.findActive(selected.getId(), treatmentRecords).ifPresentOrElse(
                    record -> treatmentReminderLabel.setText(
                            record.getProgressText() + " | Applied " + record.getApplicationsCount()
                                    + "x | " + record.getEffectiveness().getLabel()
                                    + (record.isOverdue() ? " | OVERDUE" : "")),
                    () -> {
                        int days = selected.getAssignedDisease().getTreatment().getDurationDays();
                        if (days > 0) {
                            treatmentReminderLabel.setText("Treatment plan: " + days + " days — assign tracked in Treatments tab");
                        } else {
                            treatmentReminderLabel.setText("Ongoing treatment — open Treatments tab to track");
                        }
                    });
    }

    private void updateButtonStates() {
        Plant selected = plantTable.getSelectionModel().getSelectedItem();
        boolean hasSelection = selected != null;
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        assignButton.setDisable(!hasSelection);
        showTreatmentButton.setDisable(!hasSelection || !selected.hasDisease());
        trackTreatmentButton.setDisable(!hasSelection || !selected.hasDisease());
    }

    @FXML
    private void handleAddPlant() {
        openPlantDialog(null);
    }

    @FXML
    private void handleEditPlant() {
        Plant selected = plantTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openPlantDialog(selected);
        }
    }

    @FXML
    private void handleDeletePlant() {
        Plant selected = plantTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Plant");
        confirm.setHeaderText("Delete " + selected.getName() + "?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TreatmentTrackingService.cancelActive(selected.getId(), treatmentRecords);
            plants.remove(selected);
            savePlants();
            saveTreatmentRecords();
            if (treatmentPanelController != null) {
                treatmentPanelController.refresh();
            }
            refreshDashboard();
            statusLabel.setText("Plant deleted: " + selected.getName());
        }
    }

    @FXML
    private void handleAssignDisease() {
        Plant selected = plantTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/plantmanager/view/assign-disease-dialog.fxml"));
            Parent root = loader.load();
            AssignDiseaseDialogController controller = loader.getController();
            controller.setPlant(selected);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Assign Disease");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            if (controller.isConfirmed()) {
                selected.setAssignedDisease(controller.getSelectedDisease());
                if (selected.hasDisease()) {
                    TreatmentTrackingService.startTracking(selected, treatmentRecords,
                            treatmentRecordRepository.nextId(treatmentRecords));
                } else {
                    TreatmentTrackingService.cancelActive(selected.getId(), treatmentRecords);
                }
                plantTable.refresh();
                savePlants();
                saveTreatmentRecords();
                updateButtonStates();
                updateTreatmentReminder(selected);
                if (treatmentPanelController != null) {
                    treatmentPanelController.refresh();
                }
                refreshDashboard();
                statusLabel.setText("Disease assigned to " + selected.getName());
            }
        } catch (IOException e) {
            showError("Dialog Error", e.getMessage());
        }
    }

    @FXML
    private void handleTrackTreatment() {
        Plant selected = plantTable.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.hasDisease()) {
            return;
        }
        navTreatments.setSelected(true);
        if (treatmentPanelController != null) {
            treatmentPanelController.selectPlant(selected.getId());
        }
    }

    @FXML
    private void handleShowTreatment() {
        Plant selected = plantTable.getSelectionModel().getSelectedItem();
        if (selected != null && selected.hasDisease()) {
            openTreatmentDialog(selected.getAssignedDisease(), selected.getName());
        }
    }

    @FXML
    private void handleViewTreatment() {
        Disease selected = diseaseListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openTreatmentDialog(selected, null);
        }
    }

    @FXML
    private void handleAddDisease() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/plantmanager/view/add-disease-dialog.fxml"));
            Parent root = loader.load();
            AddDiseaseDialogController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add Disease");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            if (controller.isConfirmed()) {
                Disease newDisease = controller.getDisease();
                if (DiseaseLibrary.exists(newDisease.getName())) {
                    showError("Duplicate Disease", "A disease named \"" + newDisease.getName() + "\" already exists.");
                    return;
                }
                DiseaseLibrary.add(newDisease);
                try {
                    diseaseRepository.save();
                } catch (IOException e) {
                    showError("Save Failed", "Disease was added but could not be saved: " + e.getMessage());
                }
                refreshDiseaseLibrary();
                diseaseListView.getSelectionModel().select(newDisease);
                refreshDashboard();
                statusLabel.setText("Disease added: " + newDisease.getName());
            }
        } catch (IllegalArgumentException e) {
            showError("Cannot Add Disease", e.getMessage());
        } catch (IOException e) {
            showError("Dialog Error", e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Plants CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        chooser.setInitialFileName("plants_backup.csv");

        Stage stage = (Stage) plantTable.getScene().getWindow();
        java.io.File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try {
                repository.exportPlants(plants, Path.of(file.toURI()));
                statusLabel.setText("Exported CSV: " + file.getName());
            } catch (IOException e) {
                showError("Export Failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Garden Health Report (PDF)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        chooser.setInitialFileName("garden_health_report.pdf");

        Stage stage = (Stage) plantTable.getScene().getWindow();
        java.io.File file = chooser.showSaveDialog(stage);
        if (file != null) {
            Path path = Path.of(file.toURI());
            if (!path.toString().toLowerCase().endsWith(".pdf")) {
                path = Path.of(path + ".pdf");
            }
            try {
                PdfReportService.exportGardenReport(plants, path);
                statusLabel.setText("Exported PDF: " + path.getFileName());
            } catch (IOException e) {
                showError("PDF Export Failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleShareCsvQr() {
        Stage stage = (Stage) plantTable.getScene().getWindow();
        try {
            Path temp = Files.createTempFile("plants_share_", ".csv");
            temp.toFile().deleteOnExit();
            repository.exportPlants(plants, temp);
            QrShareService.shareFile(temp, "plants.csv", stage);
            statusLabel.setText("QR share ready for plants.csv");
        } catch (Exception e) {
            showError("QR Share Failed", e.getMessage());
        }
    }

    @FXML
    private void handleSharePdfQr() {
        Stage stage = (Stage) plantTable.getScene().getWindow();
        try {
            Path temp = Files.createTempFile("garden_report_", ".pdf");
            temp.toFile().deleteOnExit();
            PdfReportService.exportGardenReport(plants, temp);
            QrShareService.shareFile(temp, "garden_health_report.pdf", stage);
            statusLabel.setText("QR share ready for PDF report");
        } catch (Exception e) {
            showError("QR Share Failed", e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        QrShareService.stopActiveServer();
        if (onLogout != null) {
            onLogout.run();
        }
    }

    @FXML
    private void handleExit() {
        QrShareService.stopActiveServer();
        Stage stage = (Stage) plantTable.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About");
        about.setHeaderText("Botanical Treatment Advisor");
        about.setContentText("""
                A JavaFX desktop application for tracking plants,
                assigning diseases, and viewing predefined treatments.

                Demonstrates OOP: encapsulation, inheritance,
                polymorphism, and abstraction.

                Data stored in plants.csv (no database required).
                """);
        about.showAndWait();
    }

    private void openPlantDialog(Plant existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/plantmanager/view/plant-dialog.fxml"));
            Parent root = loader.load();
            PlantDialogController controller = loader.getController();
            controller.setExistingPlant(existing);
            if (existing == null) {
                controller.setNextId(repository.nextId(plants));
            }

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(existing == null ? "Add Plant" : "Edit Plant");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            if (controller.isConfirmed()) {
                try {
                    Plant plant = controller.buildPlant();
                    if (existing == null) {
                        plants.add(plant);
                    } else {
                        int index = plants.indexOf(existing);
                        if (index >= 0) {
                            plants.set(index, plant);
                        }
                    }
                    plantTable.refresh();
                    savePlants();
                    refreshDashboard();
                    statusLabel.setText((existing == null ? "Added" : "Updated") + ": " + plant.getName());
                } catch (IOException e) {
                    showError("Image Save Failed", e.getMessage());
                }
            }
        } catch (IOException e) {
            showError("Dialog Error", e.getMessage());
        }
    }

    private void openTreatmentDialog(Disease disease, String plantName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/plantmanager/view/treatment-dialog.fxml"));
            Parent root = loader.load();
            TreatmentDialogController controller = loader.getController();
            controller.setDisease(disease, plantName);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(plantName != null
                    ? "Treatment — " + plantName
                    : "Treatment — " + disease.getName());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/plantmanager/view/styles.css").toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) {
            showError("Dialog Error", e.getMessage());
        }
    }

    private void savePlants() {
        try {
            repository.savePlants(plants);
        } catch (IOException e) {
            showError("Save Failed", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
