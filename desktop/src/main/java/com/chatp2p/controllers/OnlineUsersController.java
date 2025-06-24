package com.chatp2p.controllers;

import com.chatp2p.components.UserButton;
import com.chatp2p.core.App;
import com.chatp2p.managers.HttpManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.Timer;

public class OnlineUsersController implements Initializable {

    @FXML
    private VBox usersContainer;
    @FXML
    private Button connectButton;
    @FXML
    private Label messageLabel;

    private String selectedUser;
    private Timer refreshTimer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Mock user para testes
    private static final Map<String, String> MOCK_USER = Map.of(
            "username", "mock_user",
            "profileImageUrl", ""
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        connectButton.setDisable(true);
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        refreshOnlineUsers();
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refreshOnlineUsers();
            }
        }, 5000, 5000);
    }

    private void refreshOnlineUsers() {
        String token = App.getAuthToken();
        List<Map<String, String>> users = new ArrayList<>();

        // Sempre adiciona o usuário mockado
        users.add(MOCK_USER);

        if (token == null) {
            showMessage("Token não encontrado", "error");
            Platform.runLater(() -> updateUserList(users));
            return;
        }

        new Thread(() -> {
            try {
                HttpResponse<String> response = HttpManager.getWithToken(
                        "http://localhost:8080/api/users/online",
                        token
                );

                if (response.statusCode() == 200) {
                    List<Map<String, String>> apiUsers = objectMapper.readValue(
                            response.body(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
                    );

                    // Adiciona usuários reais da API
                    users.addAll(apiUsers);
                } else {
                    showMessage("Falha ao carregar usuários", "error");
                }
            } catch (Exception e) {
                showMessage("Falha na conexão: " + e.getMessage(), "error");
            }

            Platform.runLater(() -> {
                updateUserList(users);
                clearMessage();
            });
        }).start();
    }

    private void updateUserList(List<Map<String, String>> users) {
        usersContainer.getChildren().clear();
        for (Map<String, String> user : users) {
            String username = user.get("username");
            if (!username.equals(App.getCurrentUser())) {
                addUserButton(username, user.get("profileImageUrl"));
            }
        }
    }

    private void addUserButton(String username, String profileImageUrl) {
        UserButton userButton = new UserButton(username, profileImageUrl);

        userButton.setOnAction(e -> {
            selectUser(username);
            clearSelections();
            userButton.getStyleClass().add("selected");
        });

        usersContainer.getChildren().add(userButton);
    }

    private void clearSelections() {
        usersContainer.getChildren().forEach(node -> {
            if (node instanceof UserButton) {
                node.getStyleClass().remove("selected");
            }
        });
    }

    private void selectUser(String username) {
        selectedUser = username;
        connectButton.setDisable(false);
    }

    @FXML
    private void handleConnect() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }

        if (selectedUser != null) {
            try {
                ChatController.setSelectedUser(selectedUser);
                App.setRoot("ChatView");
            } catch (IOException e) {
                showMessage("Erro ao carregar o chat", "error");
            }
        } else {
            showMessage("Selecione um usuário para conectar", "error");
        }
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