package com.plantmanager.controller;

import com.plantmanager.model.AiConfig;
import com.plantmanager.service.AiService;
import com.plantmanager.service.ConfigPersistence;
import com.plantmanager.service.PlantNetService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.LinkedHashMap;
import java.util.Map;

public class AiSettingsController {

    private static final Map<String, String> PRESETS = new LinkedHashMap<>();
    private static final Map<String, String> PRESET_MODELS = new LinkedHashMap<>();
    static {
        PRESETS.put("Free (built-in, no key)", "https://text.pollinations.ai/openai");
        PRESET_MODELS.put("Free (built-in, no key)", "openai");
        PRESETS.put("OpenRouter (free tier, easy signup)", "https://openrouter.ai/api/v1/chat/completions");
        PRESET_MODELS.put("OpenRouter (free tier, easy signup)", "meta-llama/llama-3.2-3b-instruct:free");
        PRESETS.put("DeepSeek (cheap, needs key)", "https://api.deepseek.com/v1/chat/completions");
        PRESET_MODELS.put("DeepSeek (cheap, needs key)", "deepseek-chat");
        PRESETS.put("Ollama (local, free)", "http://localhost:11434/v1/chat/completions");
        PRESET_MODELS.put("Ollama (local, free)", "llama3.2");
        PRESETS.put("LM Studio (local, free)", "http://localhost:1234/v1/chat/completions");
        PRESET_MODELS.put("LM Studio (local, free)", "");
        PRESETS.put("OpenAI (paid)", "https://api.openai.com/v1/chat/completions");
        PRESET_MODELS.put("OpenAI (paid)", "gpt-4o-mini");
        PRESETS.put("Custom", "");
        PRESET_MODELS.put("Custom", "");
    }

    @FXML private ComboBox<String> providerCombo;
    @FXML private TextField apiUrlField;
    @FXML private PasswordField apiKeyField;
    @FXML private TextField modelField;
    @FXML private Label statusLabel;

    @FXML private TextField plantNetApiKeyField;

    private AiService aiService;
    private PlantNetService plantNetService;
    private Runnable onClose;

    public void initialize(AiService aiService, PlantNetService plantNetService, Runnable onClose) {
        this.aiService = aiService;
        this.plantNetService = plantNetService;
        this.onClose = onClose;

        providerCombo.getItems().addAll(PRESETS.keySet());

        providerCombo.valueProperty().addListener((obs, old, provider) -> {
            String url = PRESETS.get(provider);
            if (url != null && !url.isEmpty()) {
                apiUrlField.setText(url);
            }
            String model = PRESET_MODELS.get(provider);
            if (model != null && !model.isEmpty()) {
                modelField.setText(model);
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

        if (plantNetApiKeyField != null) {
            plantNetApiKeyField.setText(plantNetService.getApiKey());
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

        if (plantNetApiKeyField != null) {
            plantNetService.setApiKey(plantNetApiKeyField.getText().strip());
        }

        try {
            ConfigPersistence.save(config, plantNetService.getApiKey());
            statusLabel.getStyleClass().removeAll("login-success", "login-error");
            statusLabel.getStyleClass().add("login-success");
            statusLabel.setText("Settings saved.");
        } catch (Exception e) {
            statusLabel.getStyleClass().removeAll("login-success", "login-error");
            statusLabel.getStyleClass().add("login-error");
            statusLabel.setText("Failed to save: " + e.getMessage());
        }
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
