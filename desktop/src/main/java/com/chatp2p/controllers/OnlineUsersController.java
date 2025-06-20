package com.chatp2p.controllers;

import com.chatp2p.App;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import java.util.*;
import java.util.Timer;

public class OnlineUsersController implements Initializable {

    @FXML private VBox usersContainer;
    @FXML private Button connectButton;
    private String selectedUser;
    private Timer refreshTimer;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        if (token == null) {
            showAlert("Erro", "Token não encontrado");
            return;
        }

        new Thread(() -> {
            try {
                HttpResponse<String> response = HttpClient.newHttpClient().send(
                        HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/api/users/online"))
                                .header("Authorization", "Bearer " + token)
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() == 200) {
                    List<Map<String, String>> users = objectMapper.readValue(
                            response.body(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
                    );
                    Platform.runLater(() -> updateUserList(users));
                } else {
                    showAlert("Erro", "Falha ao carregar usuários");
                }
            } catch (Exception e) {
                showAlert("Erro", "Falha na conexão: " + e.getMessage());
            }
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
            try {
                ChatController.setSelectedUser(selectedUser);
                App.setRoot("ChatView");
            } catch (IOException e) {
                showAlert("Erro", "Erro ao carregar o chat");
            }
        } else {
            showAlert("Erro", "Selecione um usuário para conectar");
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}