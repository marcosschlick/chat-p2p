package com.chatp2p.controllers;

import com.chatp2p.components.FileMessageBubble;
import com.chatp2p.components.MessageBubble;
import com.chatp2p.components.SystemMessage;
import com.chatp2p.core.App;
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

    @FXML
    private Label userLabel;
    @FXML
    private VBox messagesContainer;
    @FXML
    private ScrollPane messagesScrollPane;
    @FXML
    private TextField messageField;
    @FXML
    private Label connectionStatus;

    public static ChatController getInstance() {
        return instance;
    }

    public static void setSelectedUser(String user) {
        selectedUser = user;
    }

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
        connectionStatus.setText("Não conectado");
        connectionStatus.setStyle("-fx-text-fill: #d32f2f;");
    }

    public void onConnectionEstablished(String sender) {
        if (sender.equals(selectedUser)) {
            connectionStatus.setText("Conectado");
            connectionStatus.setStyle("-fx-text-fill: #388e3c;");
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

    public void addSentMessage(String message) {
        addMessage(message, true);
    }

    public void addReceivedMessage(String message) {
        addMessage(message, false);
    }

    public void addSentFile(String fileName) {
        addFileMessage(fileName, true);
    }

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

    public void addSystemMessage(String message) {
        messagesContainer.getChildren().add(new SystemMessage(message));
    }

    private void addMessage(String message, boolean sent) {
        messagesContainer.getChildren().add(new MessageBubble(message, sent));
    }

    private void addFileMessage(String fileName, boolean sent) {
        messagesContainer.getChildren().add(new FileMessageBubble(fileName, sent));
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