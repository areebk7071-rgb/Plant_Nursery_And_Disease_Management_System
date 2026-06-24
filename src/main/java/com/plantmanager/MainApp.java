package com.plantmanager;

import com.plantmanager.controller.LoginController;
import com.plantmanager.controller.MainController;
import com.plantmanager.repository.DiseaseRepository;
import com.plantmanager.repository.PlantRepository;
import com.plantmanager.repository.TreatmentRecordRepository;
import com.plantmanager.repository.UserRepository;
import com.plantmanager.service.AuthService;
import com.plantmanager.view.SplashScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private Stage primaryStage;
    private final AuthService authService = new AuthService(new UserRepository());

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Botanical Treatment Advisor");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        SplashScreen.show(this::showLoginScreen);
    }

    private void showLoginScreen() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/plantmanager/view/login-view.fxml"));
                Parent root = loader.load();

                LoginController controller = loader.getController();
                controller.initialize(authService, this::showMainWindow);

                Scene scene = new Scene(root, 480, 560);
                scene.getStylesheets().add(
                        getClass().getResource("/com/plantmanager/view/styles.css").toExternalForm());

                primaryStage.setScene(scene);
                primaryStage.setResizable(false);
                primaryStage.centerOnScreen();
                primaryStage.show();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load login screen", e);
            }
        });
    }

    private void showMainWindow() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/plantmanager/view/main-view.fxml"));
                Parent root = loader.load();

                MainController controller = loader.getController();
                PlantRepository repository = new PlantRepository();
                DiseaseRepository diseaseRepository = new DiseaseRepository();
                TreatmentRecordRepository treatmentRecordRepository = new TreatmentRecordRepository();
                try {
                    diseaseRepository.load();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load diseases", e);
                }
                controller.initializeData(repository, diseaseRepository, treatmentRecordRepository,
                        authService.getCurrentUser().orElseThrow(), this::handleLogout);

                Scene scene = new Scene(root, 1200, 780);
                scene.getStylesheets().add(
                        getClass().getResource("/com/plantmanager/view/styles.css").toExternalForm());

                primaryStage.setMinWidth(900);
                primaryStage.setMinHeight(600);
                primaryStage.setResizable(true);
                primaryStage.setScene(scene);
                primaryStage.show();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load main window", e);
            }
        });
    }

    private void handleLogout() {
        authService.logout();
        showLoginScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
