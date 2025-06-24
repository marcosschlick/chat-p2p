package com.chatp2p.core;

import com.chatp2p.managers.AuthManager;
import com.chatp2p.managers.ConnectionManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class App extends Application {
    private static Scene scene;
    private static Stage primaryStage;
    private static ConnectionManager connectionManager;
    private static boolean isShutdown = false;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        scene = new Scene(loadFXML("LoginView"), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/com/chatp2p/styles.css").toExternalForm());
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

        // 1. Logout síncrono primeiro
        if (AuthManager.getAuthToken() != null) {
            AuthManager.logoutSynchronous();
        }

        // 2. Notificar outros usuários
        if (connectionManager != null) {
            connectionManager.notifyAppClosing();
            connectionManager.shutdown();
        }

        // 3. Encerrar aplicação
        Platform.exit();
        System.exit(0);
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        return FXMLLoader.load(App.class.getResource("/com/chatp2p/views/" + fxml + ".fxml"));
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setAuthToken(String token) {
        AuthManager.setAuthToken(token);
    }

    public static String getAuthToken() {
        return AuthManager.getAuthToken();
    }

    public static void setCurrentUser(String user) {
        AuthManager.setCurrentUser(user);
    }

    public static String getCurrentUser() {
        return AuthManager.getCurrentUser();
    }

    public static void main(String[] args) {
        launch();
    }
}