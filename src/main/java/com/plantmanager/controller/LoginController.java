package com.plantmanager.controller;

import com.plantmanager.model.User;
import com.plantmanager.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private AuthService authService;
    private Runnable onLoginSuccess;

    public void initialize(AuthService authService, Runnable onLoginSuccess) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;

        usernameField.textProperty().addListener((obs, old, text) -> clearError());
        passwordField.textProperty().addListener((obs, old, text) -> clearError());

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });

        usernameField.requestFocus();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank()) {
            showError("Please enter your username.");
            usernameField.requestFocus();
            return;
        }
        if (password == null || password.isBlank()) {
            showError("Please enter your password.");
            passwordField.requestFocus();
            return;
        }

        loginButton.setDisable(true);
        try {
            Optional<User> user = authService.authenticate(username, password);
            if (user.isPresent()) {
                onLoginSuccess.run();
            } else {
                showError("Invalid username or password.");
                passwordField.clear();
                passwordField.requestFocus();
            }
        } catch (IOException e) {
            showError("Could not verify credentials: " + e.getMessage());
        } finally {
            loginButton.setDisable(false);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
