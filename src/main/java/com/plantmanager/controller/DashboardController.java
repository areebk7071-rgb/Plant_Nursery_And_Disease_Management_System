package com.plantmanager.controller;

import com.plantmanager.model.Plant;
import com.plantmanager.model.TreatmentRecord;
import com.plantmanager.service.DashboardStats;
import com.plantmanager.service.TreatmentTrackingService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DashboardController {

    @FXML private Label totalPlantsValue;
    @FXML private Label healthyPlantsValue;
    @FXML private Label diseasedPlantsValue;
    @FXML private Label diseaseLibraryValue;
    @FXML private Label healthScoreValue;
    @FXML private Label reminderCountLabel;
    @FXML private VBox treatmentRemindersBox;
    @FXML private HBox kpiCardsBox;
    @FXML private VBox chartsContainer;
    @FXML private ListView<String> insightsList;

    private Consumer<TreatmentRecord> onApplyTreatment;
    private Consumer<TreatmentRecord> onSeeTreatmentDetails;
    private ObservableList<TreatmentRecord> treatmentRecords;

    private PieChart healthPieChart;
    private BarChart<String, Number> typeBarChart;
    private BarChart<String, Number> diseaseBarChart;
    private Label typeChartDetail;
    private Label diseaseChartDetail;
    private Label healthChartDetail;

    private Node selectedTypeBar;
    private Node selectedDiseaseBar;

    private static final String TYPE_FRUIT = "#40916c";
    private static final String TYPE_FLOWER = "#e9c46a";
    private static final String TYPE_HERB = "#457b9d";
    private static final String TYPE_VEGETABLE = "#e67e22";
    private static final String TYPE_TREE = "#27ae60";
    private static final String TYPE_VINE = "#8e44ad";
    private static final String DISEASE_COLOR = "#e76f51";

    @FXML
    private void initialize() {
        buildCharts();
    }

    public void setTreatmentCallbacks(ObservableList<TreatmentRecord> records,
                                      Consumer<TreatmentRecord> onApplyTreatment,
                                      Consumer<TreatmentRecord> onSeeTreatmentDetails) {
        this.treatmentRecords = records;
        this.onApplyTreatment = onApplyTreatment;
        this.onSeeTreatmentDetails = onSeeTreatmentDetails;
    }

    public void refresh(List<Plant> plants) {
        int total = plants.size();
        int healthy = DashboardStats.countHealthy(plants);
        int diseased = DashboardStats.countDiseased(plants);
        int librarySize = com.plantmanager.model.DiseaseLibrary.getAll().size();
        double healthPct = DashboardStats.healthPercentage(plants);

        totalPlantsValue.setText(String.valueOf(total));
        healthyPlantsValue.setText(String.valueOf(healthy));
        diseasedPlantsValue.setText(String.valueOf(diseased));
        diseaseLibraryValue.setText(String.valueOf(librarySize));
        healthScoreValue.setText(String.format("%.0f%%", healthPct));

        rebuildTreatmentReminders();

        selectedTypeBar = null;
        selectedDiseaseBar = null;
        typeChartDetail.setText("Hover a bar for info • Click to select a plant type");
        diseaseChartDetail.setText("Hover a bar for info • Click to select a disease");
        healthChartDetail.setText("Click a slice to see health breakdown");

        updateHealthChart(healthy, diseased, total);
        updateTypeChart(DashboardStats.countByType(plants), total);
        updateDiseaseChart(DashboardStats.countByDisease(plants), diseased);
        insightsList.setItems(FXCollections.observableArrayList(DashboardStats.generateInsights(plants)));
    }

    private void rebuildTreatmentReminders() {
        treatmentRemindersBox.getChildren().clear();
        if (treatmentRecords == null) {
            reminderCountLabel.setText("No treatment data");
            return;
        }

        List<TreatmentRecord> active = TreatmentTrackingService.getActiveReminders(treatmentRecords);
        if (active.isEmpty()) {
            reminderCountLabel.setText("All plants healthy — no treatment needed");
            Label empty = new Label("No active treatments. Assign a disease to a plant to start tracking.");
            empty.getStyleClass().add("chart-hint");
            empty.setWrapText(true);
            treatmentRemindersBox.getChildren().add(empty);
            return;
        }

        reminderCountLabel.setText(active.size() + " plant(s) need treatment care");
        for (TreatmentRecord record : active) {
            treatmentRemindersBox.getChildren().add(createReminderCard(record));
        }
    }

    private HBox createReminderCard(TreatmentRecord record) {
        HBox card = new HBox(14);
        card.getStyleClass().add("dashboard-reminder-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(record.isOverdue() ? "⚠" : "💊");
        icon.getStyleClass().add("dashboard-reminder-icon");

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(record.getPlantName() + "  ·  " + record.getDiseaseName());
        title.getStyleClass().add("dashboard-reminder-title");

        String detail = record.getProgressText()
                + "  ·  Applied " + record.getApplicationsCount() + " time(s)"
                + (record.isOverdue() ? "  ·  OVERDUE" : "");
        Label subtitle = new Label(detail);
        subtitle.getStyleClass().add(record.isOverdue() ? "dashboard-reminder-overdue" : "dashboard-reminder-sub");
        subtitle.setWrapText(true);

        info.getChildren().addAll(title, subtitle);

        Button applyBtn = new Button("Apply Treatment");
        applyBtn.getStyleClass().add("primary-button");
        applyBtn.setCursor(Cursor.HAND);
        applyBtn.setOnAction(e -> {
            if (onApplyTreatment != null) {
                onApplyTreatment.accept(record);
            }
        });

        Button detailsBtn = new Button("See Full Details");
        detailsBtn.getStyleClass().add("secondary-button");
        detailsBtn.setCursor(Cursor.HAND);
        detailsBtn.setOnAction(e -> {
            if (onSeeTreatmentDetails != null) {
                onSeeTreatmentDetails.accept(record);
            }
        });

        HBox actions = new HBox(8, applyBtn, detailsBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(icon, info, actions);
        return card;
    }

    private void buildCharts() {
        healthPieChart = createHealthPieChart();
        typeBarChart = createTypeBarChart();
        diseaseBarChart = createDiseaseBarChart();

        healthChartDetail = createDetailLabel();
        typeChartDetail = createDetailLabel();
        diseaseChartDetail = createDetailLabel();

        HBox chartRow = new HBox(16,
                wrapChart("Health Overview", healthPieChart, healthChartDetail, 340),
                wrapChart("Plants by Type", typeBarChart, typeChartDetail, 340));
        chartRow.setFillHeight(true);

        HBox chartRow2 = new HBox(16,
                wrapChart("Plants by Disease", diseaseBarChart, diseaseChartDetail, 700));
        chartRow2.setFillHeight(true);

        chartsContainer.getChildren().addAll(chartRow, chartRow2);
    }

    private Label createDetailLabel() {
        Label label = new Label();
        label.getStyleClass().add("chart-detail-label");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private VBox wrapChart(String title, javafx.scene.chart.Chart chart, Label detailLabel, double prefWidth) {
        Label chartTitle = new Label(title);
        chartTitle.getStyleClass().add("chart-title");
        Label hint = new Label("Names on X-axis • Numbers on bars • Click bar to select");
        hint.getStyleClass().add("chart-hint");

        chart.setPrefWidth(prefWidth);
        chart.setPrefHeight(300);
        chart.setMinHeight(260);
        chart.getStyleClass().add("dashboard-chart");

        VBox box = new VBox(6, chartTitle, hint, chart, detailLabel);
        box.getStyleClass().add("chart-card");
        HBox.setHgrow(box, javafx.scene.layout.Priority.ALWAYS);
        return box;
    }

    private PieChart createHealthPieChart() {
        PieChart chart = new PieChart();
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setStartAngle(90);
        return chart;
    }

    private BarChart<String, Number> createTypeBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Plant Type");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Plants");
        yAxis.setMinorTickVisible(false);
        yAxis.setForceZeroInRange(true);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setCategoryGap(30);
        chart.setBarGap(6);
        return chart;
    }

    private BarChart<String, Number> createDiseaseBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Disease Name");
        xAxis.setTickLabelRotation(-20);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Affected Plants");
        yAxis.setMinorTickVisible(false);
        yAxis.setForceZeroInRange(true);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setCategoryGap(20);
        chart.setBarGap(4);
        return chart;
    }

    private void updateHealthChart(int healthy, int diseased, int total) {
        healthPieChart.getData().clear();
        if (total == 0) {
            PieChart.Data empty = new PieChart.Data("No plants yet (0)", 1);
            healthPieChart.getData().add(empty);
            setupPieSlice(empty, "#ced4da", "No plants in catalog yet.",
                    "No plants in catalog — add plants to see health breakdown.");
            return;
        }

        if (healthy > 0) {
            PieChart.Data healthySlice = new PieChart.Data("Healthy (" + healthy + ")", healthy);
            healthPieChart.getData().add(healthySlice);
            setupPieSlice(healthySlice, "#52b788",
                    "Healthy: " + healthy + " plant(s)",
                    String.format("Healthy: %d plant(s) — %.0f%% of your garden", healthy, healthy * 100.0 / total));
        }
        if (diseased > 0) {
            PieChart.Data diseasedSlice = new PieChart.Data("Diseased (" + diseased + ")", diseased);
            healthPieChart.getData().add(diseasedSlice);
            setupPieSlice(diseasedSlice, "#e76f51",
                    "Diseased: " + diseased + " plant(s)",
                    String.format("Diseased: %d plant(s) need treatment — %.0f%% of your garden",
                            diseased, diseased * 100.0 / total));
        }
    }

    private void setupPieSlice(PieChart.Data data, String color, String tooltipText, String selectedText) {
        data.nodeProperty().addListener((obs, old, node) -> {
            if (node != null) {
                applyPieStyle(node, color, false);
                Tooltip.install(node, new Tooltip(tooltipText + "\nClick to select"));
                node.setOnMouseClicked(e -> {
                    resetAllPieStyles();
                    applyPieStyle(node, color, true);
                    healthChartDetail.setText("Selected: " + selectedText);
                });
            }
        });
    }

    private void applyPieStyle(Node node, String color, boolean selected) {
        node.setCursor(Cursor.HAND);
        if (selected) {
            node.setStyle("-fx-pie-color: " + color + "; -fx-border-color: #1b4332; -fx-border-width: 3;");
        } else {
            node.setStyle("-fx-pie-color: " + color + ";");
        }
    }

    private void resetAllPieStyles() {
        for (PieChart.Data data : healthPieChart.getData()) {
            if (data.getNode() != null) {
                String color = data.getName().startsWith("Healthy") ? "#52b788"
                        : data.getName().startsWith("Diseased") ? "#e76f51" : "#ced4da";
                applyPieStyle(data.getNode(), color, false);
            }
        }
    }

    private void updateTypeChart(Map<String, Long> counts, int total) {
        typeBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        long maxCount = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);

        counts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> {
                    String type = entry.getKey();
                    long count = entry.getValue();
                    String color = colorForType(type);
                    boolean isHighest = count == maxCount && maxCount > 0;

                    XYChart.Data<String, Number> data = new XYChart.Data<>(type, count);
                    series.getData().add(data);

                    String tooltip = type + ": " + count + " plant(s)"
                            + (isHighest ? " (HIGHEST)" : "");
                    String selectedMsg = String.format(
                            "Selected: %s — %d plant(s)%s — %.0f%% of all plants",
                            type, count, isHighest ? " ★ HIGHEST" : "",
                            total > 0 ? count * 100.0 / total : 0);

                    setupSelectableBar(data, color, tooltip, selectedMsg, true);
                });

        typeBarChart.getData().add(series);
        configureYAxis(typeBarChart, maxCount);

        if (maxCount > 0) {
            counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .ifPresent(e -> typeChartDetail.setText(
                            "★ Highest: " + e.getKey() + " with " + e.getValue() + " plant(s) — click a bar to select"));
        }
    }

    private void updateDiseaseChart(Map<String, Long> counts, int diseasedTotal) {
        diseaseBarChart.getData().clear();

        if (counts.isEmpty()) {
            diseaseChartDetail.setText("No diseases assigned — all plants are healthy");
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        long maxCount = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);

        counts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> {
                    String disease = entry.getKey();
                    long count = entry.getValue();
                    boolean isTop = count == maxCount;

                    XYChart.Data<String, Number> data = new XYChart.Data<>(disease, count);
                    series.getData().add(data);

                    String tooltip = disease + ": " + count + " plant(s)"
                            + (isTop ? " (MOST AFFECTED)" : "");
                    String selectedMsg = String.format(
                            "Selected: %s — affects %d plant(s)%s — %.0f%% of diseased plants",
                            disease, count, isTop ? " ★ MOST AFFECTED" : "",
                            diseasedTotal > 0 ? count * 100.0 / diseasedTotal : 0);

                    setupSelectableBar(data, DISEASE_COLOR, tooltip, selectedMsg, false);
                });

        diseaseBarChart.getData().add(series);
        configureYAxis(diseaseBarChart, maxCount);

        counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> diseaseChartDetail.setText(
                        "★ Most affected: " + e.getKey() + " (" + e.getValue() + " plants) — click a bar to select"));
    }

    private String colorForType(String type) {
        return switch (type) {
            case "Fruit" -> TYPE_FRUIT;
            case "Flower" -> TYPE_FLOWER;
            case "Herb" -> TYPE_HERB;
            case "Vegetable" -> TYPE_VEGETABLE;
            case "Tree" -> TYPE_TREE;
            case "Vine" -> TYPE_VINE;
            default -> TYPE_FRUIT;
        };
    }

    private void configureYAxis(BarChart<String, Number> chart, long maxCount) {
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        yAxis.setUpperBound(Math.max(maxCount + 1, 4));
        yAxis.setTickUnit(1);
        yAxis.setMinorTickCount(0);
    }

    private void setupSelectableBar(XYChart.Data<String, Number> data, String color,
                                    String tooltip, String selectedMessage, boolean isTypeChart) {
        data.nodeProperty().addListener((obs, oldNode, barNode) -> {
            if (barNode == null) {
                return;
            }

            barNode.setUserData(color);
            applyBarStyle(barNode, color, false);
            Tooltip.install(barNode, new Tooltip(tooltip + "\nClick to select"));

            barNode.setOnMouseClicked(e -> {
                if (isTypeChart) {
                    deselectBar(selectedTypeBar);
                    selectedTypeBar = barNode;
                } else {
                    deselectBar(selectedDiseaseBar);
                    selectedDiseaseBar = barNode;
                }
                applyBarStyle(barNode, color, true);
                (isTypeChart ? typeChartDetail : diseaseChartDetail).setText(selectedMessage);
                e.consume();
            });

            addValueLabelOnBar(data, barNode);
        });
    }

    private void addValueLabelOnBar(XYChart.Data<String, Number> data, Node barNode) {
        Node parent = barNode.getParent();
        if (parent instanceof StackPane stackPane) {
            Label valueLabel = new Label(String.valueOf(data.getYValue().intValue()));
            valueLabel.getStyleClass().add("bar-value-label");
            stackPane.getChildren().add(valueLabel);
            StackPane.setAlignment(valueLabel, Pos.TOP_CENTER);
            valueLabel.setTranslateY(-20);
        }
    }

    private void applyBarStyle(Node barNode, String color, boolean selected) {
        barNode.setCursor(Cursor.HAND);
        if (selected) {
            barNode.setStyle("-fx-bar-fill: " + color + "; -fx-stroke: #1b4332; -fx-stroke-width: 3;");
        } else {
            barNode.setStyle("-fx-bar-fill: " + color + ";");
        }
    }

    private void deselectBar(Node barNode) {
        if (barNode != null && barNode.getUserData() instanceof String color) {
            applyBarStyle(barNode, color, false);
        }
    }
}
