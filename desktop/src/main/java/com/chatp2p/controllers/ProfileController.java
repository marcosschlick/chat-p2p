package com.chatp2p.controllers;

import com.chatp2p.core.App;
import com.chatp2p.managers.HttpManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
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
    private String selectedImageBase64 = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usernameField.setText(App.getCurrentUser());
        loadProfileImage();
    }

    private void loadProfileImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/com/chatp2p/images/default_user.png"));
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Erro ao carregar imagem padrão: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Foto do Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(App.getPrimaryStage());
        if (file != null) {
            try {
                byte[] imageBytes = Files.readAllBytes(file.toPath());
                selectedImageBase64 = Base64.getEncoder().encodeToString(imageBytes);

                // Atualiza a imagem exibida
                Image newImage = new Image(file.toURI().toString());
                profileImageView.setImage(newImage);

                showMessage("Foto selecionada com sucesso!", "success");
            } catch (IOException e) {
                showMessage("Erro ao carregar a imagem", "error");
            }
        }
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

        new Thread(() -> {
            try {
                updateProfile(newUsername, newPassword);
            } finally {
                Platform.runLater(() -> saveButton.setDisable(false));
            }
        }).start();
    }

    private void updateProfile(String newUsername, String newPassword) {
        String token = App.getAuthToken();
        if (token == null) {
            showMessage("Token não encontrado", "error");
            return;
        }

        try {
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("username", newUsername);

            if (!newPassword.isEmpty()) {
                profileData.put("password", newPassword);
            }

            if (selectedImageBase64 != null) {
                profileData.put("profileImageUrl", "data:image/png;base64," + selectedImageBase64);
            }

            String jsonBody = objectMapper.writeValueAsString(profileData);

            // Usando o novo endpoint com método PUT
            HttpResponse<String> response = HttpManager.putWithToken(
                    "http://localhost:8080/api/users/me",
                    token,
                    jsonBody
            );

            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    showMessage("Perfil atualizado com sucesso!", "success");
                    if (!newUsername.equals(App.getCurrentUser())) {
                        App.setCurrentUser(newUsername);
                    }
                    newPasswordField.clear();
                    selectedImageBase64 = null; // Resetar após atualização
                } else {
                    showMessage("Erro ao atualizar perfil: " + response.statusCode(), "error");
                }
            });
        } catch (Exception e) {
            Platform.runLater(() ->
                    showMessage("Erro na conexão: " + e.getMessage(), "error")
            );
        }
    }

    @FXML
    private void handleBack() {
        try {
            App.setRoot("OnlineUsers");
        } catch (Exception e) {
            showMessage("Erro ao voltar: " + e.getMessage(), "error");
        }
    }

    private void showMessage(String message, String type) {
        Platform.runLater(() -> {
            messageLabel.setText(message);
            messageLabel.getStyleClass().removeAll("error-message", "success-message");
            messageLabel.getStyleClass().add(type + "-message");
        });
    }
}