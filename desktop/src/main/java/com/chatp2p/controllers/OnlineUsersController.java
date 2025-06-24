package com.chatp2p.controllers;

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
        List<Map<String, String>> users = new ArrayList<>();

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
        userIps.clear(); // Limpa IPs anteriores

        for (Map<String, String> user : users) {
            String username = user.get("username");
            String ip = user.get("ip"); // Novo: obtém o IP

            if (!username.equals(App.getCurrentUser())) {
                userIps.put(username, ip); // Armazena IP do usuário
                addUserButton(username, user.get("profileImageUrl"));
            }
        }
    }

    private void addUserButton(String username, String profileImageUrl) {
        VBox userContainer = new VBox(5);
        userContainer.setAlignment(Pos.CENTER);

        ImageView userImage = new ImageView();
        userImage.setFitWidth(60);
        userImage.setFitHeight(60);
        userImage.setPreserveRatio(true);

        try {
            if (profileImageUrl == null || profileImageUrl.isEmpty()) {
                profileImageUrl = App.class.getResource("/com/chatp2p/images/default_user.jpg").toString();
            }
            userImage.setImage(new Image(profileImageUrl, true));
        } catch (Exception e) {
            userImage.setImage(new Image(App.class.getResource("/com/chatp2p/images/default_user.jpg").toString()));
        }

        Label userNameLabel = new Label(username);
        userNameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        userContainer.getChildren().addAll(userImage, userNameLabel);

        Button userButton = new Button();
        userButton.setGraphic(userContainer);
        userButton.getStyleClass().add("user-button");
        userButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        userButton.setOnAction(e -> {
            selectUser(username);
            clearSelections();
            userButton.getStyleClass().add("selected");
        });

        usersContainer.getChildren().add(userButton);
    }

    private void clearSelections() {
        usersContainer.getChildren().forEach(node -> {
            if (node instanceof Button) {
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