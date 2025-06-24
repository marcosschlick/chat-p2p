package com.chatp2p.controllers;

import com.chatp2p.core.App;
import com.chatp2p.utils.NetworkUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.DatagramSocket;
import java.net.InetAddress;
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

    private String getLocalIP() {
        return NetworkUtils.getLocalIP();
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
                    App.setAuthToken(json.get("token").asText());
                    App.setCurrentUser(username);

                    // Armazenar URL da imagem de perfil
                    if (json.has("profileImageUrl")) {
                        App.setProfileImageUrl(json.get("profileImageUrl").asText());
                    }

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
            }
        }).start();
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll("error-message", "success-message");
    }

    private void showMessage(String message, String type) {
        Platform.runLater(() -> {
            messageLabel.setText(message);
            messageLabel.getStyleClass().add(type + "-message");
        });
    }
}