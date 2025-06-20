package com.chatp2p.controllers;

import com.chatp2p.App;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    private static ChatController instance;
    private static String selectedUser;

    @FXML private Label userLabel;
    @FXML private VBox messagesContainer;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private TextField messageField;

    public static ChatController getInstance() { return instance; }
    public static void setSelectedUser(String user) { selectedUser = user; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        userLabel.setText(selectedUser != null ? selectedUser : "");

        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            messagesScrollPane.setVvalue(1.0);
        });

        messageField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleSendMessage();
        });
    }

    public void onConnectionEstablished(String sender) {
        if (sender.equals(selectedUser)) {
            addSystemMessage("Conexão estabelecida com " + sender);
        }
    }

    public boolean isChattingWith(String username) {
        return selectedUser != null && selectedUser.equals(username);
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            App.sendMessage(selectedUser, message);
            addSentMessage(message);
            messageField.clear();
        }
    }

    @FXML
    private void handleAttachment() {
        File file = new FileChooser().showOpenDialog(new Stage());
        if (file != null) {
            App.sendFile(selectedUser, file);
            addSentFile(file.getName());
        }
    }

    public void addSentMessage(String message) { addMessage(message, true); }
    public void addReceivedMessage(String message) { addMessage(message, false); }
    public void addSentFile(String fileName) { addFileMessage(fileName, true); }

    public void addReceivedFile(String fileName, byte[] fileData) {
        Platform.runLater(() -> {
            try {
                File file = new File(System.getProperty("user.home") + "/Downloads/" + fileName);
                Files.write(file.toPath(), fileData);
                addFileMessage(fileName, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void addSystemMessage(String message) {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(5));

        Label label = new Label(message);
        label.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px; -fx-font-style: italic;");
        container.getChildren().add(label);

        messagesContainer.getChildren().add(container);
    }

    private void addMessage(String message, boolean sent) {
        Platform.runLater(() -> {
            HBox container = new HBox();
            container.setPadding(new Insets(5, 10, 5, 10));
            container.setAlignment(sent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            VBox bubble = new VBox(5);
            bubble.setPadding(new Insets(8, 12, 8, 12));
            bubble.setStyle("-fx-background-color: " + (sent ? "#DCF8C6" : "#FFFFFF") + "; -fx-background-radius: 12;");

            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setFont(Font.font("Segoe UI", 14));
            messageLabel.setMaxWidth(300);

            Label timeLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            timeLabel.setStyle("-fx-text-fill: #777777; -fx-font-size: 10px;");

            bubble.getChildren().addAll(messageLabel, timeLabel);
            container.getChildren().add(bubble);
            messagesContainer.getChildren().add(container);
        });
    }

    private void addFileMessage(String fileName, boolean sent) {
        Platform.runLater(() -> {
            HBox container = new HBox();
            container.setPadding(new Insets(5, 10, 5, 10));
            container.setAlignment(sent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            VBox bubble = new VBox(5);
            bubble.setPadding(new Insets(8, 12, 8, 12));
            bubble.setStyle("-fx-background-color: " + (sent ? "#DCF8C6" : "#FFFFFF") + "; -fx-background-radius: 12;");

            Label fileLabel = new Label("📎 " + fileName);
            fileLabel.setWrapText(true);
            fileLabel.setFont(Font.font("Segoe UI", 14));
            fileLabel.setMaxWidth(300);

            Label timeLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            timeLabel.setStyle("-fx-text-fill: #777777; -fx-font-size: 10px;");

            bubble.getChildren().addAll(fileLabel, timeLabel);
            container.getChildren().add(bubble);
            messagesContainer.getChildren().add(container);
        });
    }

    @FXML
    private void handleBack() {
        try {
            App.setRoot("OnlineUsers");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}