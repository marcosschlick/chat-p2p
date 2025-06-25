package com.chatp2p.controllers;

import com.chatp2p.components.UserButton;
import com.chatp2p.core.App;
import com.chatp2p.managers.HttpManager;
import com.chatp2p.exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.Timer;

public class OnlineUsersController implements Initializable {

    @FXML
    private FlowPane usersContainer;
    @FXML
    private Button connectButton;
    @FXML
    private Label messageLabel;
    @FXML
    private Button profileButton;
    @FXML
    private ImageView profileImageView;
    private String selectedUser;
    private Timer refreshTimer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> userIps = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        connectButton.setDisable(true);
        profileButton.setOnAction(e -> handleProfile());
        setProfileImage();
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
        String token = App.getUserProfile().getAuthToken();
        List<OnlineUser> users = new ArrayList<>();

        if (token == null) {
            showMessage("Token não encontrado", "error");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    updateUserList(users);
                }
            });
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpResponse<String> response = HttpManager.getWithToken("http://localhost:8080/api/users/online", token);
                    if (response.statusCode() == 200) {
                        List<Map<String, Object>> apiUsers = objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                        for (Map<String, Object> user : apiUsers) {
                            String username = user.get("username") != null ? user.get("username").toString() : null;
                            String ip = user.get("ip") != null ? user.get("ip").toString() : null;
                            String profileImageUrl = user.get("profileImageUrl") != null ? user.get("profileImageUrl").toString() : null;
                            users.add(new OnlineUser(username, ip, profileImageUrl));
                        }
                    } else {
                        showMessage("Falha ao carregar usuários: " + response.statusCode(), "error");
                    }
                } catch (Exception e) {
                    showMessage("Falha na conexão: " + e.getMessage(), "error");
                    throw new NetworkException("Failed to refresh online users", e);
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        updateUserList(users);
                        clearMessage();
                    }
                });
            }
        });
        thread.start();
    }

    private void updateUserList(List<OnlineUser> users) {
        usersContainer.getChildren().clear();
        userIps.clear();
        for (int i = 0; i < users.size(); i++) {
            OnlineUser user = users.get(i);
            String username = user.username;
            String ip = user.ip;
            String profileImageUrl = user.profileImageUrl;
            if (username == null) {
                continue;
            }
            if (App.getUserProfile() != null && !username.equals(App.getUserProfile().getUsername())) {
                userIps.put(username, ip != null ? ip : "127.0.0.1");
                addUserButton(username, profileImageUrl);
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
        for (javafx.scene.Node node : usersContainer.getChildren()) {
            if (node instanceof UserButton) {
                node.getStyleClass().remove("selected");
            }
        }
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
            String ip = userIps.get(selectedUser);

            if (ip != null) {
                App.connectToPeer(selectedUser, ip, 55555);

                try {
                    ChatController.setSelectedUser(selectedUser);
                    App.setRoot("ChatView");
                } catch (IOException e) {
                    showMessage("Erro ao carregar o chat", "error");
                    throw new AppException("Failed to load chat for user: " + selectedUser, e);
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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                messageLabel.setText(message);
                messageLabel.getStyleClass().add(type + "-message");
            }
        });
    }

    @FXML
    private void handleProfile() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }

        try {
            App.setRoot("ProfileView");
        } catch (Exception e) {
            showMessage("Erro ao abrir perfil: " + e.getMessage(), "error");
            throw new AppException("Failed to open profile view", e);
        }
    }

    private void setProfileImage() {
        if (profileImageView != null && App.getUserProfile() != null) {
            String imageUrl = App.getUserProfile().getProfileImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (!imageUrl.startsWith("/com/chatp2p/images/")) {
                    imageUrl = "/com/chatp2p/images/" + imageUrl;
                }
                try {
                    Image image = new Image(getClass().getResourceAsStream(imageUrl));
                    profileImageView.setImage(image);
                } catch (Exception e) {
                    profileImageView.setImage(new Image(getClass().getResourceAsStream("/com/chatp2p/images/default_user.png")));
                }
            } else {
                profileImageView.setImage(new Image(getClass().getResourceAsStream("/com/chatp2p/images/default_user.png")));
            }
        }
    }

    // Simple class to represent an online user
    private static class OnlineUser {
        String username;
        String ip;
        String profileImageUrl;
        OnlineUser(String username, String ip, String profileImageUrl) {
            this.username = username;
            this.ip = ip;
            this.profileImageUrl = profileImageUrl;
        }
    }
}