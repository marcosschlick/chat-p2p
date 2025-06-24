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
    private final Map<String, String> userIps = new HashMap<>();

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
        List<Map<String, String>> users = new ArrayList<>(); // Inicia vazia

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

                System.out.println("Response status: " + response.statusCode()); // Log para debug
                System.out.println("Response body: " + response.body()); // Log para debug

                if (response.statusCode() == 200) {
                    // Corrige a desserialização
                    List<Map<String, Object>> apiUsers = objectMapper.readValue(
                            response.body(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
                    );

                    // Converte para Map<String, String>
                    for (Map<String, Object> user : apiUsers) {
                        Map<String, String> stringMap = new HashMap<>();
                        user.forEach((key, value) -> stringMap.put(key, value != null ? value.toString() : ""));
                        users.add(stringMap);
                    }
                } else {
                    showMessage("Falha ao carregar usuários: " + response.statusCode(), "error");
                }
            } catch (Exception e) {
                System.err.println("Erro ao processar resposta: " + e.getMessage());
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
        userIps.clear();

        // DEBUG: Verifique antes de renderizar
        System.out.println("Usuários para renderizar: " + users.size());

        for (Map<String, String> user : users) {
            String username = user.get("username");
            String ip = user.get("ip");

            // Verifique se os campos existem
            if (username == null) {
                System.err.println("Usuário sem username: " + user);
                continue;
            }

            // Filtra usuário atual
            if (!username.equals(App.getCurrentUser())) {
                userIps.put(username, ip != null ? ip : "127.0.0.1");
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
            String ip = userIps.get(selectedUser); // Obtém IP do usuário selecionado

            if (ip != null) {
                // Conecta usando IP e porta fixa
                App.connectToPeer(selectedUser, ip, 55555); // 55555 é a porta padrão

                try {
                    ChatController.setSelectedUser(selectedUser);
                    App.setRoot("ChatView");
                } catch (IOException e) {
                    showMessage("Erro ao carregar o chat", "error");
                }
            } else {
                showMessage("IP do usuário não disponível", "error");
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