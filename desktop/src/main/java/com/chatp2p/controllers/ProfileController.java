package com.chatp2p.controllers;

import com.chatp2p.core.App;
import com.chatp2p.managers.HttpManager;
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
    private String selectedImageName = null;  // Armazena apenas o nome do arquivo

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
        usernameField.setText(App.getCurrentUser());
        loadProfileImage();
    }

    private void loadProfileImage() {
        try {
            String profileImageUrl = App.getProfileImageUrl();
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                // Se a URL já está completa, usa diretamente
                Image image = new Image(profileImageUrl);
                profileImageView.setImage(image);
            } else {
                // Caso contrário, carrega a padrão
                Image defaultImage = new Image(getClass().getResourceAsStream("/com/chatp2p/images/default_user.png"));
                profileImageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar imagem padrão: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePhoto() {
        // Criar um diálogo personalizado
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Selecionar Foto do Perfil");
        dialog.setHeaderText("Escolha uma imagem:");

        // Configurar botões
        ButtonType selectButtonType = new ButtonType("Selecionar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        // Layout para as imagens
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        int col = 0;
        int row = 0;
        for (String imageName : availableImages) {
            try {
                // Carregar a imagem do recurso
                InputStream is = getClass().getResourceAsStream("/com/chatp2p/images/" + imageName);
                if (is == null) {
                    System.err.println("Imagem não encontrada: " + imageName);
                    continue;
                }
                Image image = new Image(is);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);

                // Container para a imagem (para borda e clique)
                VBox container = new VBox(imageView);
                container.setAlignment(Pos.CENTER);
                container.setPadding(new Insets(5));
                container.setStyle("-fx-border-color: #555555; -fx-border-radius: 5;");
                container.setOnMouseClicked(e -> {
                    // Define o resultado do diálogo como o nome da imagem
                    dialog.setResult(imageName);
                });

                grid.add(container, col, row);
                col++;
                if (col > 2) { // 3 colunas
                    col = 0;
                    row++;
                }
            } catch (Exception e) {
                System.err.println("Erro ao carregar imagem: " + imageName + ": " + e.getMessage());
            }
        }

        dialog.getDialogPane().setContent(grid);

        // Converter o resultado para o nome da imagem
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return dialog.getResult();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(imageName -> {
            try {
                // Armazena apenas o nome do arquivo
                selectedImageName = imageName;

                // Atualizar a exibição
                Image newImage = new Image(getClass().getResource("/com/chatp2p/images/" + imageName).toString());
                profileImageView.setImage(newImage);

                showMessage("Foto selecionada com sucesso!", "success");
            } catch (Exception e) {
                showMessage("Erro ao carregar a imagem", "error");
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

            // Envia apenas o nome do arquivo, não o Base64
            if (selectedImageName != null) {
                profileData.put("profileImageUrl", selectedImageName);
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

                    // Atualiza a URL da imagem no app
                    if (selectedImageName != null) {
                        App.setProfileImageUrl("/com/chatp2p/images/" + selectedImageName);
                    }

                    newPasswordField.clear();
                    selectedImageName = null; // Resetar após atualização
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