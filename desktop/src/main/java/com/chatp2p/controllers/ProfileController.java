package com.chatp2p.controllers;

import com.chatp2p.core.App;
import com.chatp2p.managers.HttpManager;
import com.chatp2p.models.UserProfile;
import com.chatp2p.exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML
    private ImageView profileImageView;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private Label messageLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button backButton;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String selectedImageName = null;  // Stores only the file name

    private final String[] availableImages = {
            "default_user.png",
            "bob_esponja.jpg",
            "chaves.png",
            "groot.jpg",
            "sasuke.png",
            "squirtle.png",
            "suarez.png",
            "vegeta.png",
            "capitao_america.png"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        UserProfile profile = App.getUserProfile();
        if (profile != null) {
            usernameField.setText(profile.getUsername());
            setProfileImageView(profile.getProfileImageUrl());
        }
    }

    private void setProfileImageView(String imageUrl) {
        if (profileImageView != null && imageUrl != null && !imageUrl.isEmpty()) {
            try {
                if (!imageUrl.startsWith("/com/chatp2p/images/")) {
                    imageUrl = "/com/chatp2p/images/" + imageUrl;
                }
                Image image = new Image(getClass().getResourceAsStream(imageUrl));
                profileImageView.setImage(image);
            } catch (Exception e) {
                profileImageView.setImage(new Image(getClass().getResourceAsStream("/com/chatp2p/images/default_user.png")));
            }
        } else {
            profileImageView.setImage(new Image(getClass().getResourceAsStream("/com/chatp2p/images/default_user.png")));
        }
    }

    @FXML
    private void handleChangePhoto() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select Profile Photo");
        dialog.setHeaderText("Choose an image:");

        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        int col = 0;
        int row = 0;
        for (String imageName : availableImages) {
            try {
                InputStream is = getClass().getResourceAsStream("/com/chatp2p/images/" + imageName);
                if (is == null) {
                    System.err.println("Image not found: " + imageName);
                    continue;
                }
                Image image = new Image(is);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);

                VBox container = new VBox(imageView);
                container.setAlignment(Pos.CENTER);
                container.setPadding(new Insets(5));
                container.setStyle("-fx-border-color: #555555; -fx-border-radius: 5;");
                container.setOnMouseClicked(e -> {
                    dialog.setResult(imageName);
                });

                grid.add(container, col, row);
                col++;
                if (col > 2) {
                    col = 0;
                    row++;
                }
            } catch (Exception e) {
                throw new AppException("Failed to load image: " + imageName, e);
            }
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return dialog.getResult();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(imageName -> {
            try {
                selectedImageName = imageName;
                Image newImage = new Image(getClass().getResource("/com/chatp2p/images/" + imageName).toString());
                profileImageView.setImage(newImage);
                showMessage("Foto selecionada com sucesso!", "success");
            } catch (Exception e) {
                showMessage("Erro ao carregar a imagem", "error");
                throw new AppException("Failed to load selected image: " + imageName, e);
            }
        });
    }

    @FXML
    private void handleSave() {
        String newUsername = usernameField.getText().trim();
        String newPassword = newPasswordField.getText();

        if (newUsername.isEmpty()) {
            showMessage("Nome de usuário não pode estar vazio", "error");
            return;
        }

        saveButton.setDisable(true);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    updateProfile(newUsername, newPassword);
                    App.getUserProfile().setUsername(newUsername);
                    if (selectedImageName != null) {
                        App.getUserProfile().setProfileImageUrl(selectedImageName);
                    }
                } finally {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            saveButton.setDisable(false);
                        }
                    });
                }
            }
        });
        thread.start();
    }

    private void updateProfile(String newUsername, String newPassword) {
        String token = App.getUserProfile().getAuthToken();
        if (token == null) {
            showMessage("Token não encontrado", "error");
            return;
        }
        try {
            String usernameToSend = newUsername;
            String passwordToSend = newPassword;
            String imageToSend = selectedImageName;
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"username\":\"").append(usernameToSend).append("\"");
            if (passwordToSend != null && !passwordToSend.isEmpty()) {
                jsonBuilder.append(",\"password\":\"").append(passwordToSend).append("\"");
            }
            if (imageToSend != null) {
                jsonBuilder.append(",\"profileImageUrl\":\"").append(imageToSend).append("\"");
            }
            jsonBuilder.append("}");
            String jsonBody = jsonBuilder.toString();
            HttpResponse<String> response = HttpManager.putWithToken(
                    "http://localhost:8080/api/users/me",
                    token,
                    jsonBody
            );
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (response.statusCode() == 200) {
                        showMessage("Perfil atualizado com sucesso!", "success");
                        if (App.getUserProfile() != null) {
                            App.getUserProfile().setUsername(newUsername);
                        }
                        if (selectedImageName != null) {
                            App.getUserProfile().setProfileImageUrl(selectedImageName);
                        }
                        newPasswordField.clear();
                        selectedImageName = null;
                    } else {
                        showMessage("Erro ao atualizar perfil: " + response.statusCode(), "error");
                    }
                }
            });
        } catch (Exception e) {
            showMessage("Erro na conexão: " + e.getMessage(), "error");
            throw new NetworkException("Failed to update profile for user: " + newUsername, e);
        }
    }

    @FXML
    private void handleBack() {
        try {
            App.setRoot("OnlineUsers");
        } catch (Exception e) {
            showMessage("Erro ao voltar: " + e.getMessage(), "error");
            throw new AppException("Failed to go back to OnlineUsers", e);
        }
    }

    private void showMessage(String message, String type) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                messageLabel.setText(message);
                messageLabel.getStyleClass().removeAll("error-message", "success-message");
                messageLabel.getStyleClass().add(type + "-message");
            }
        });
    }
}