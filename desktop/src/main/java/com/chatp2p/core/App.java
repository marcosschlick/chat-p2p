package com.chatp2p.core;

import com.chatp2p.managers.AuthManager;
import com.chatp2p.managers.ConnectionManager;
import com.chatp2p.models.UserProfile;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class App extends Application {
    private static Scene scene;
    private static Stage primaryStage;
    private static ConnectionManager connectionManager;
    private static boolean isShutdown = false;
    private static UserProfile currentUserProfile;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        scene = new Scene(loadFXML("LoginView"), 1200, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/chatp2p/styles.css")).toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Chat P2P");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setOnCloseRequest(event -> shutdown());

        connectionManager = new ConnectionManager();
        connectionManager.startP2PServer();
        stage.show();
    }

    public static void connectToPeer(String username, String ip, int port) {
        connectionManager.connectToPeer(username, ip, port);
    }

    public static void sendMessage(String recipient, String content) {
        connectionManager.sendMessage(recipient, content);
    }

    public static void sendFile(String recipient, File file) {
        connectionManager.sendFile(recipient, file);
    }

    public static int getServerPort() {
        return connectionManager.getServerPort();
    }

    public static void notifyUserLeft(String username) {
        connectionManager.notifyUserLeft(username);
    }

    private void shutdown() {
        if (isShutdown) return;
        isShutdown = true;
        UserProfile user = getUserProfile();
        if (user != null && user.getAuthToken() != null) {
            AuthManager.logoutSynchronous();
        }
        if (connectionManager != null) {
            connectionManager.notifyAppClosing();
            connectionManager.shutdown();
        }
        Platform.exit();
        System.exit(0);
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        return FXMLLoader.load(Objects.requireNonNull(App.class.getResource("/com/chatp2p/views/" + fxml + ".fxml")));
    }

    public static void setUserProfile(Long id, String username, String profileImageUrl, String authToken) {
        UserProfile user = new UserProfile();
        user.setId(id);
        user.setUsername(username);
        user.setProfileImageUrl(profileImageUrl);
        user.setAuthToken(authToken);
        currentUserProfile = user;
    }

    public static UserProfile getUserProfile() {
        return currentUserProfile;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}