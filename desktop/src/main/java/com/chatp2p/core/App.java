package com.chatp2p.core;

import com.chatp2p.managers.ConnectionManager;
import com.chatp2p.managers.HttpManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;

public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;
    private static String authToken;
    private static String currentUser;
    private static ConnectionManager connectionManager;

    public static void setCurrentUser(String user) {
        currentUser = user;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

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
        if (authToken != null) {
            connectionManager.notifyAppClosing();
            logoutOnExit();
        }
        connectionManager.shutdown();
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
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

    private static void logoutOnExit() {
        new Thread(() -> {
            try {
                HttpManager.postWithToken(
                        "http://localhost:8080/api/auth/logout",
                        authToken,
                        ""
                );
            } catch (Exception e) {
                System.err.println("Erro no logout automático: " + e.getMessage());
            }
        }).start();
    }

    public static void main(String[] args) {
        launch();
    }
}