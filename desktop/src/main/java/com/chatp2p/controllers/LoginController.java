package com.chatp2p.controllers;

import com.chatp2p.core.App;
import com.chatp2p.utils.NetworkUtils;
import com.chatp2p.exceptions.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginController {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;
    @FXML
    private ImageView userIconView;

    private String getLocalIP() {
        return NetworkUtils.getLocalIP();
    }

    @FXML
    public void initialize() {
        usernameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateUserIcon(newValue.trim());
            }
        });
    }

    private void updateUserIcon(String username) {
        if (username.isEmpty()) {
            loadDefaultUserIcon();
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpResponse<String> response = HttpClient.newHttpClient().send(
                            HttpRequest.newBuilder()
                                    .uri(URI.create("http://localhost:8080/api/users/profile-image/" + username))
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofString()
                    );
                    if (response.statusCode() == 200) {
                        JsonNode json = objectMapper.readTree(response.body());
                        String imageUrl = json.get("profileImageUrl").asText();
                        String fullImageUrl;
                        if (imageUrl.startsWith("/com/chatp2p/images/")) {
                            fullImageUrl = imageUrl;
                        } else {
                            fullImageUrl = "/com/chatp2p/images/" + imageUrl;
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Image image = new Image(fullImageUrl);
                                    userIconView.setImage(image);
                                } catch (Exception e) {
                                    loadDefaultUserIcon();
                                }
                            }
                        });
                    } else {
                        loadDefaultUserIcon();
                    }
                } catch (Exception e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            loadDefaultUserIcon();
                        }
                    });
                    throw new NetworkException("Failed to load user icon for username: " + username, e);
                }
            }
        });
        thread.start();
    }

    private void loadDefaultUserIcon() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/com/chatp2p/images/default_user.png"));
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    userIconView.setImage(defaultImage);
                }
            });
        } catch (Exception e) {
            throw new AppException("Failed to load default user icon", e);
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        clearMessage();
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Preencha todos os campos", "error");
            return;
        }
        String ip = getLocalIP();
        new Thread(() -> {
            try {
                String jsonBody = String.format(
                        "{\"username\":\"%s\",\"password\":\"%s\",\"ip\":\"%s\"}",
                        username, password, ip
                );
                HttpResponse<String> response = HttpClient.newHttpClient().send(
                        HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/api/auth/login"))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );
                if (response.statusCode() == 200) {
                    JsonNode json = objectMapper.readTree(response.body());
                    Long userId = json.get("id").asLong();
                    String token = json.get("token").asText();
                    String profileImageUrl = json.get("profileImageUrl").asText();
                    App.setUserProfile(userId, username, profileImageUrl, token);
                    Platform.runLater(() -> {
                        try {
                            App.setRoot("OnlineUsers");
                        } catch (Exception e) {
                            showMessage("Erro ao carregar tela", "error");
                        }
                    });
                } else {
                    showMessage("Credenciais inválidas", "error");
                }
            } catch (Exception e) {
                showMessage("Falha na conexão: " + e.getMessage(), "error");
                throw new NetworkException("Login failed for user: " + username, e);
            }
        }).start();
    }

    @FXML
    private void handleCreateAccount() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        clearMessage();
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Preencha todos os campos", "error");
            return;
        }
        new Thread(() -> {
            try {
                HttpResponse<String> response = HttpClient.newHttpClient().send(
                        HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/api/auth/register"))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(
                                        "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );
                if (response.statusCode() == 200) {
                    showMessage("Conta criada com sucesso!", "success");
                } else {
                    showMessage("Erro ao criar conta", "error");
                }
            } catch (Exception e) {
                showMessage("Falha na conexão: " + e.getMessage(), "error");
                throw new NetworkException("Account creation failed for user: " + username, e);
            }
        }).start();
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll("error-message", "success-message");
    }

    private void showMessage(String message, String type) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                messageLabel.setText(message);
                messageLabel.getStyleClass().add(type + "-message");
            }
        });
    }
}