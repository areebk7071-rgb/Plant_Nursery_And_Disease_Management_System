package com.plantmanager.controller;

import com.plantmanager.model.AiConfig;
import com.plantmanager.model.Plant;
import com.plantmanager.service.AiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AiController {

    @FXML private TextArea chatArea;
    @FXML private TextArea inputField;
    @FXML private Button askButton;
    @FXML private Label headerSubtitle;

    private AiService aiService;
    private List<Plant> plants;
    private Stage stage;

    public void initialize(AiService aiService, List<Plant> plants, Stage stage) {
        this.aiService = aiService;
        this.plants = plants;
        this.stage = stage;

        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && event.isShiftDown()) {
                inputField.appendText("\n");
            } else if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                handleAsk();
            }
        });

        int plantCount = plants.size();
        headerSubtitle.setText("Ask about " + plantCount + " plant(s) in your care");

        chatArea.setText("""
                Welcome to AI Plant Advisor!

                Ask questions like:
                - "What's wrong with my tomato plant?"
                - "How do I treat powdery mildew?"
                - "What are the best companion plants for basil?"
                - "Summarize care tips for all my plants"

                Enter your question below and press Enter or click Ask AI.
                """);
    }

    @FXML
    private void handleAsk() {
        String question = inputField.getText();
        if (question == null || question.isBlank()) return;

        chatArea.appendText("\n\n--- You ---\n" + question + "\n");
        inputField.clear();
        askButton.setDisable(true);

        new Thread(() -> {
            try {
                String response = aiService.ask(question, plants);
                Platform.runLater(() -> {
                    chatArea.appendText("\n--- AI Advisor ---\n" + response + "\n");
                    chatArea.setScrollTop(Double.MAX_VALUE);
                    askButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    chatArea.appendText("\n--- Error ---\n" + e.getMessage() + "\n");
                    askButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleOpenSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/plantmanager/view/ai-settings-dialog.fxml"));
            Parent root = loader.load();
            AiSettingsController controller = loader.getController();
            controller.initialize(aiService, () -> {
                Stage s = (Stage) root.getScene().getWindow();
                s.close();
            });

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("AI Settings");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) {
            chatArea.appendText("\n--- Error ---\nCould not open settings: " + e.getMessage() + "\n");
        }
    }
}
