package com.plantmanager.controller;

import com.plantmanager.model.Plant;
import com.plantmanager.service.AiService;
import com.plantmanager.service.PlantNetService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.plantmanager.model.AiConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AiController {

    @FXML private ScrollPane chatScroll;
    @FXML private VBox chatContainer;
    @FXML private TextField inputField;
    @FXML private Button askButton;
    @FXML private Button uploadButton;
    @FXML private Button cancelButton;
    @FXML private Label headerSubtitle;
    @FXML private Label providerTag;
    @FXML private HBox imagePreviewBox;
    @FXML private ImageView imagePreview;
    @FXML private Label imageFileName;

    private AiService aiService;
    private PlantNetService plantNetService;
    private List<Plant> plants;
    private Stage stage;
    private Path selectedImagePath;
    private Label typingIndicator;
    private volatile Thread currentRequestThread;

    private static final Map<String, String> SWITCHABLE_PROVIDERS = new LinkedHashMap<>();
    private static final Map<String, String> SWITCHABLE_MODELS = new LinkedHashMap<>();
    static {
        SWITCHABLE_PROVIDERS.put("Free (built-in)", "https://text.pollinations.ai/openai");
        SWITCHABLE_MODELS.put("Free (built-in)", "openai");
        SWITCHABLE_PROVIDERS.put("OpenRouter (fast, free tier)", "https://openrouter.ai/api/v1/chat/completions");
        SWITCHABLE_MODELS.put("OpenRouter (fast, free tier)", "meta-llama/llama-3.2-3b-instruct:free");
        SWITCHABLE_PROVIDERS.put("DeepSeek (cheap)", "https://api.deepseek.com/v1/chat/completions");
        SWITCHABLE_MODELS.put("DeepSeek (cheap)", "deepseek-chat");
    }

    private static final String USER_COLOR = "#1b4332";
    private static final String USER_BUBBLE = "#d8f3dc";
    private static final String AI_BUBBLE = "#ffffff";
    private static final String SYSTEM_BUBBLE = "#f0f4f1";
    private static final String ERROR_BUBBLE = "#fce4e4";

    public void initialize(AiService aiService, PlantNetService plantNetService, List<Plant> plants, Stage stage) {
        this.aiService = aiService;
        this.plantNetService = plantNetService;
        this.plants = plants;
        this.stage = stage;

        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                handleAsk();
            }
        });

        int plantCount = plants.size();
        headerSubtitle.setText(plantCount + " plant(s) in your care");
        refreshProviderTag();

        addSystemMessage("🌿 Welcome to AI Plant Advisor!\n\n" +
                "AI chat and plant identification work out of the box. " +
                "Upload a photo or type a question below.\n\n" +
                "For more accurate plant ID, add a free PlantNet API key in Settings.");
    }

    @FXML
    private void handleUploadImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Plant Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            selectedImagePath = file.toPath();
            Image image = new Image(file.toURI().toString(), 60, 60, true, true);
            imagePreview.setImage(image);
            imageFileName.setText(file.getName());
            imagePreviewBox.setVisible(true);
            imagePreviewBox.setManaged(true);
        }
    }

    @FXML
    private void handleClearImage() {
        selectedImagePath = null;
        imagePreview.setImage(null);
        imageFileName.setText("");
        imagePreviewBox.setVisible(false);
        imagePreviewBox.setManaged(false);
    }

    @FXML
    private void handleAsk() {
        String question = inputField.getText().strip();
        boolean hasImage = selectedImagePath != null;
        boolean hasQuestion = !question.isEmpty();

        if (!hasImage && !hasQuestion) return;

        inputField.clear();
        askButton.setDisable(true);
        uploadButton.setDisable(true);
        cancelButton.setVisible(true);
        cancelButton.setManaged(true);

        if (hasImage) {
            String text = "[📷 " + imageFileName.getText() + "]";
            if (hasQuestion) text += "\n" + question;
            addUserMessage(text);
        } else {
            addUserMessage(question);
        }

        showTyping();

        String finalQuestion = question;
        currentRequestThread = new Thread(() -> {
            try {
                if (Thread.currentThread().isInterrupted()) return;
                if (hasImage) {
                    String plantNetKey = plantNetService.getApiKey();
                    boolean hasPlantNet = plantNetKey != null && !plantNetKey.isBlank();

                    String result;
                    if (hasPlantNet) {
                        String raw = plantNetService.identify(selectedImagePath);
                        result = PlantNetService.formatPlantNetResponse(raw);
                    } else {
                        String prompt = "Identify this plant from the image. Give me the scientific name, common name, and notable characteristics. Keep it concise.";
                        if (hasQuestion) prompt += "\n\nThe user also asks: " + finalQuestion;
                        result = aiService.askWithImage(prompt, selectedImagePath, plants);
                        result += "\n\n(For more accurate ID, add a free PlantNet key in Settings)";
                    }

                    String finalResult = result;
                    Platform.runLater(() -> {
                        hideTyping();
                        addAiMessage(finalResult);
                        handleClearImage();
                        reenableButtons();
                    });
                } else {
                    String response = aiService.ask(finalQuestion, plants);
                    Platform.runLater(() -> {
                        hideTyping();
                        addAiMessage(response);
                        reenableButtons();
                    });
                }
            } catch (Exception e) {
                if (Thread.currentThread().isInterrupted()) return;
                Platform.runLater(() -> {
                    hideTyping();
                    addErrorMessage(e.getMessage());
                    reenableButtons();
                });
            }
        });
        currentRequestThread.start();
    }

    @FXML
    private void handleCancelRequest() {
        if (currentRequestThread != null && currentRequestThread.isAlive()) {
            currentRequestThread.interrupt();
            addSystemMessage("⏹ Request cancelled.");
            hideTyping();
            reenableButtons();
        }
    }

    private void addUserMessage(String text) {
        HBox bubble = createBubble(text, USER_BUBBLE, USER_COLOR, false);
        HBox wrapper = new HBox(bubble);
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        wrapper.setPadding(new Insets(1, 0, 1, 40));
        chatContainer.getChildren().add(wrapper);
        scrollToBottom();
    }

    private void addAiMessage(String text) {
        HBox bubble = createBubble(text, AI_BUBBLE, "#2d3436", false);
        HBox wrapper = new HBox(bubble);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setPadding(new Insets(1, 40, 1, 0));
        chatContainer.getChildren().add(wrapper);
        scrollToBottom();
    }

    private void addSystemMessage(String text) {
        HBox bubble = createBubble(text, SYSTEM_BUBBLE, "#5c6f63", true);
        HBox wrapper = new HBox(bubble);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(4, 40, 4, 40));
        chatContainer.getChildren().add(wrapper);
        scrollToBottom();
    }

    private void addErrorMessage(String text) {
        HBox bubble = createBubble("⚠ " + text, ERROR_BUBBLE, "#c0392b", false);
        HBox wrapper = new HBox(bubble);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setPadding(new Insets(1, 40, 1, 0));
        chatContainer.getChildren().add(wrapper);
        scrollToBottom();
    }

    private HBox createBubble(String text, String bgColor, String textColor, boolean center) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(420);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + textColor + "; -fx-line-spacing: 2px;");
        if (center) {
            label.setTextAlignment(TextAlignment.CENTER);
        }

        HBox bubble = new HBox(label);
        bubble.setPadding(new Insets(9, 14, 9, 14));
        bubble.setBackground(new Background(new BackgroundFill(
                Color.web(bgColor), new CornerRadii(14), Insets.EMPTY)));
        return bubble;
    }

    private void showTyping() {
        if (typingIndicator == null) {
            typingIndicator = new Label("  Typing...");
            typingIndicator.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6; -fx-font-style: italic; -fx-padding: 4 0 4 4;");
        }
        HBox wrapper = new HBox(typingIndicator);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setPadding(new Insets(1, 40, 1, 0));
        wrapper.setId("typing-indicator");
        chatContainer.getChildren().add(wrapper);
        scrollToBottom();
    }

    private void hideTyping() {
        chatContainer.getChildren().removeIf(n -> {
            if (n instanceof HBox h) {
                return "typing-indicator".equals(h.getId());
            }
            return false;
        });
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (chatContainer.getHeight() > 0) {
                chatScroll.setVvalue(1.0);
            }
        });
    }

    private void refreshProviderTag() {
        String name = aiService.getCurrentProviderName();
        if (name == null || name.isBlank()) {
            name = "Free (built-in)";
        }
        providerTag.setText("⚡ " + name);
    }

    @FXML
    private void handleSwitchProvider() {
        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(
                aiService.getCurrentProviderName(),
                new ArrayList<>(SWITCHABLE_PROVIDERS.keySet()));
        dialog.setTitle("Switch AI Provider");
        dialog.setHeaderText("Choose an AI provider");
        dialog.setContentText("Provider:");
        dialog.showAndWait().ifPresent(provider -> {
            String url = SWITCHABLE_PROVIDERS.get(provider);
            String model = SWITCHABLE_MODELS.get(provider);
            if (url != null) {
                AiConfig newConfig = new AiConfig(provider, url,
                        aiService.getConfig().getApiKey(),
                        model != null ? model : aiService.getConfig().getModelName());
                aiService.setConfig(newConfig);
                refreshProviderTag();
                addSystemMessage("🔄 Switched to " + provider + ". All future questions will use this provider.");
            }
        });
    }

    private void reenableButtons() {
        askButton.setDisable(false);
        uploadButton.setDisable(false);
        cancelButton.setVisible(false);
        cancelButton.setManaged(false);
        refreshProviderTag();
    }

    @FXML
    private void handleOpenSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/plantmanager/view/ai-settings-dialog.fxml"));
            Parent root = loader.load();
            AiSettingsController controller = loader.getController();
            controller.initialize(aiService, plantNetService, () -> {
                Stage s = (Stage) root.getScene().getWindow();
                s.close();
            });

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("AI Settings");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) {
            addErrorMessage("Could not open settings: " + e.getMessage());
        }
    }
}
