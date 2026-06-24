package com.plantmanager.controller;

import com.plantmanager.model.User;
import com.plantmanager.service.AuthService;
import com.plantmanager.service.PasswordHasher;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML private VBox loginFormBox;
    @FXML private VBox registerFormBox;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Label subtitleLabel;

    @FXML private TextField regUsernameField;
    @FXML private TextField regDisplayNameField;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regConfirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Label regErrorLabel;

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

        regUsernameField.textProperty().addListener((obs, old, text) -> clearRegError());
        regPasswordField.textProperty().addListener((obs, old, text) -> clearRegError());
        regConfirmPasswordField.textProperty().addListener((obs, old, text) -> clearRegError());

        showLoginForm();
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

    @FXML
    private void handleRegister() {
        String username = regUsernameField.getText();
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();
        String displayName = regDisplayNameField.getText();

        if (username == null || username.isBlank()) {
            showRegError("Please enter a username.");
            regUsernameField.requestFocus();
            return;
        }
        if (password == null || password.isBlank()) {
            showRegError("Please enter a password.");
            regPasswordField.requestFocus();
            return;
        }
        if (confirmPassword == null || confirmPassword.isBlank()) {
            showRegError("Please confirm your password.");
            regConfirmPasswordField.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            showRegError("Passwords do not match.");
            regConfirmPasswordField.clear();
            regConfirmPasswordField.requestFocus();
            return;
        }
        if (displayName == null || displayName.isBlank()) {
            showRegError("Please enter a display name.");
            regDisplayNameField.requestFocus();
            return;
        }

        registerButton.setDisable(true);
        try {
            if (authService.getUserRepository().findByUsername(username).isPresent()) {
                showRegError("Username already taken.");
                regUsernameField.clear();
                regUsernameField.requestFocus();
                return;
            }

            User user = new User(username, PasswordHasher.hash(password), displayName);
            authService.getUserRepository().createUser(user);

            showRegSuccess("Account created successfully! You can now sign in.");
            showLoginForm();
        } catch (IOException e) {
            showRegError("Registration failed: " + e.getMessage());
        } finally {
            registerButton.setDisable(false);
        }
    }

    @FXML
    private void showLoginForm() {
        loginFormBox.setVisible(true);
        loginFormBox.setManaged(true);
        registerFormBox.setVisible(false);
        registerFormBox.setManaged(false);

        subtitleLabel.setText("Sign in to continue");

        usernameField.clear();
        passwordField.clear();
        clearError();
        usernameField.requestFocus();
    }

    @FXML
    private void showRegisterForm() {
        loginFormBox.setVisible(false);
        loginFormBox.setManaged(false);
        registerFormBox.setVisible(true);
        registerFormBox.setManaged(true);

        subtitleLabel.setText("Create a new account");

        regUsernameField.clear();
        regDisplayNameField.clear();
        regPasswordField.clear();
        regConfirmPasswordField.clear();
        clearRegError();
        regUsernameField.requestFocus();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().removeAll("login-success");
        errorLabel.getStyleClass().add("login-error");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showRegError(String message) {
        regErrorLabel.setText(message);
        regErrorLabel.getStyleClass().removeAll("login-success");
        regErrorLabel.getStyleClass().add("login-error");
        regErrorLabel.setVisible(true);
        regErrorLabel.setManaged(true);
    }

    private void showRegSuccess(String message) {
        regErrorLabel.setText(message);
        regErrorLabel.getStyleClass().removeAll("login-error");
        regErrorLabel.getStyleClass().add("login-success");
        regErrorLabel.setVisible(true);
        regErrorLabel.setManaged(true);
    }

    private void clearRegError() {
        regErrorLabel.setText("");
        regErrorLabel.setVisible(false);
        regErrorLabel.setManaged(false);
    }
}
