package com.plantmanager.controller;

import com.plantmanager.model.AiConfig;
import com.plantmanager.service.AiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.Map;

public class AiSettingsController {

    private static final Map<String, String> PRESETS = Map.of(
            "Ollama (local)", "http://localhost:11434/v1/chat/completions",
            "LM Studio (local)", "http://localhost:1234/v1/chat/completions",
            "OpenAI", "https://api.openai.com/v1/chat/completions",
            "Custom", ""
    );

    @FXML private ComboBox<String> providerCombo;
    @FXML private TextField apiUrlField;
    @FXML private PasswordField apiKeyField;
    @FXML private TextField modelField;
    @FXML private Label statusLabel;

    private AiService aiService;
    private Runnable onClose;

    public void initialize(AiService aiService, Runnable onClose) {
        this.aiService = aiService;
        this.onClose = onClose;

        providerCombo.getItems().addAll(PRESETS.keySet());

        providerCombo.valueProperty().addListener((obs, old, provider) -> {
            String url = PRESETS.get(provider);
            if (url != null && !url.isEmpty()) {
                apiUrlField.setText(url);
            }
        });

        loadConfig();
    }

    private void loadConfig() {
        AiConfig config = aiService.getConfig();
        apiUrlField.setText(config.getApiUrl());
        apiKeyField.setText(config.getApiKey());
        modelField.setText(config.getModelName());

        String provider = config.getProvider();
        if (PRESETS.containsKey(provider)) {
            providerCombo.setValue(provider);
        } else {
            providerCombo.setValue("Custom");
        }
    }

    @FXML
    private void handleTest() {
        statusLabel.getStyleClass().removeAll("login-success", "login-error");
        statusLabel.setText("Testing connection...");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);

        AiConfig testConfig = buildConfig();
        AiService testService = new AiService(testConfig);

        new Thread(() -> {
            try {
                String result = testService.ask("Say 'Connection successful' if you can read this.");
                Platform.runLater(() -> {
                    statusLabel.getStyleClass().add("login-success");
                    statusLabel.setText("Connected! Response: " + result);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.getStyleClass().add("login-error");
                    statusLabel.setText("Connection failed: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleSave() {
        AiConfig config = buildConfig();
        aiService.setConfig(config);
        statusLabel.getStyleClass().removeAll("login-success", "login-error");
        statusLabel.getStyleClass().add("login-success");
        statusLabel.setText("Settings saved.");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    @FXML
    private void handleCancel() {
        if (onClose != null) onClose.run();
    }

    private AiConfig buildConfig() {
        return new AiConfig(
                providerCombo.getValue() != null ? providerCombo.getValue() : "Custom",
                apiUrlField.getText().strip(),
                apiKeyField.getText().strip(),
                modelField.getText().strip()
        );
    }
}
