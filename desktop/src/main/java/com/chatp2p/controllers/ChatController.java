package com.chatp2p.controllers;

import com.chatp2p.components.FileMessageBubble;
import com.chatp2p.components.MessageBubble;
import com.chatp2p.components.SystemMessage;
import com.chatp2p.core.App;
import com.chatp2p.exceptions.*;
import com.chatp2p.repositories.MessageRepository;
import com.chatp2p.models.Message;
import com.chatp2p.models.MessageType;
import com.chatp2p.services.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    private static ChatController instance;
    private static String selectedUser;
    private MessageRepository messageRepository;

    @FXML
    private Label userLabel;
    @FXML
    private VBox messagesContainer;
    @FXML
    private ScrollPane messagesScrollPane;
    @FXML
    private TextField messageField;

    public static ChatController getInstance() {
        return instance;
    }

    public static void setSelectedUser(String user) {
        selectedUser = user;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        messageRepository = new MessageRepository();

        if (selectedUser != null) {
            userLabel.setText(selectedUser);
            loadMessageHistory();
        } else {
            userLabel.setText("");
        }

        messagesContainer.heightProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
            @Override
            public void changed(javafx.beans.value.ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
                messagesScrollPane.setVvalue(1.0);
            }
        });
        messageField.setOnKeyPressed(new javafx.event.EventHandler<javafx.scene.input.KeyEvent>() {
            @Override
            public void handle(javafx.scene.input.KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    handleSendMessage();
                }
            }
        });
    }

    private void loadMessageHistory() {
        if (App.getUserProfile() == null || selectedUser == null) {
            return;
        }

        try {
            Long selectedUserId = getUserRemoteId(selectedUser);
            if (selectedUserId != null) {
                List<Message> history = messageRepository.findHistory(App.getUserProfile().getId(), selectedUserId);

                Platform.runLater(() -> {
                    for (Message msg : history) {
                        if (msg.getType() == MessageType.TEXT) {
                            boolean isSent = "Você".equals(msg.getSender());
                            addMessage(msg.getContent(), isSent);
                        } else if (msg.getType() == MessageType.FILE) {
                            boolean isSent = "Você".equals(msg.getSender());
                            addFileMessage(msg.getFileName(), isSent);
                        }
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }

    private Long getUserRemoteId(String username) {
        return UserService.getRemoteUserId(username);
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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(System.getProperty("user.home") + "/Downloads/" + fileName);
                    Files.write(file.toPath(), fileData);
                    addFileMessage(fileName, false);
                } catch (IOException e) {
                    throw new AppException("Failed to save received file: " + fileName, e);
                }
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
        if (App.getUserProfile() != null) {
            App.notifyUserLeft(selectedUser);
        }

        try {
            App.setRoot("OnlineUsers");
        } catch (IOException e) {
            throw new AppException("Failed to go back to OnlineUsers", e);
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