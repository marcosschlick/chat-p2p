package com.chatp2p;

import com.chatp2p.controllers.ChatController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;
    private static String authToken;
    private static String currentUser;
    private static ServerSocket serverSocket;
    private static ExecutorService executorService;
    public static final Map<String, Socket> activeConnections = new ConcurrentHashMap<>();
    public static final Map<String, ObjectOutputStream> outputStreams = new ConcurrentHashMap<>();

    public static void setCurrentUser(String user) { currentUser = user; }
    public static String getCurrentUser() { return currentUser; }

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

        startP2PServer();
        stage.show();
    }

    private void startP2PServer() {
        executorService = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(0);
            System.out.println("Servidor P2P na porta: " + serverSocket.getLocalPort());

            executorService.submit(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        executorService.submit(() -> handleIncomingConnection(clientSocket));
                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleIncomingConnection(Socket socket) {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Message message = (Message) ois.readObject();

            if (message.getType() == Message.MessageType.CONNECTION_REQUEST) {
                String sender = message.getSender();
                activeConnections.put(sender, socket);
                outputStreams.put(sender, new ObjectOutputStream(socket.getOutputStream()));

                if (ChatController.getInstance() != null) {
                    Platform.runLater(() ->
                            ChatController.getInstance().onConnectionEstablished(sender)
                    );
                }

                sendMessage(sender, new Message(
                        currentUser, sender, "Conexão estabelecida", Message.MessageType.CONNECTION_ACCEPTED
                ));

                startMessageListener(sender, ois);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startMessageListener(String sender, ObjectInputStream ois) {
        executorService.submit(() -> {
            try {
                while (true) {
                    Message message = (Message) ois.readObject();
                    Platform.runLater(() -> {
                        if (ChatController.getInstance() != null &&
                                ChatController.getInstance().isChattingWith(sender)) {

                            if (message.getType() == Message.MessageType.FILE) {
                                ChatController.getInstance().addReceivedFile(
                                        message.getFileName(), message.getFileData()
                                );
                            } else if (message.getType() == Message.MessageType.TEXT) {
                                ChatController.getInstance().addReceivedMessage(message.getContent());
                            }
                        }
                    });
                }
            } catch (Exception e) {
                activeConnections.remove(sender);
                outputStreams.remove(sender);
            }
        });
    }

    public static void connectToPeer(String username, String ip, int port) {
        executorService.submit(() -> {
            try {
                Socket socket = new Socket(ip, port);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                oos.writeObject(new Message(
                        currentUser, username, "Solicitação de conexão", Message.MessageType.CONNECTION_REQUEST
                ));

                activeConnections.put(username, socket);
                outputStreams.put(username, oos);

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                startMessageListener(username, ois);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (ChatController.getInstance() != null) {
                        ChatController.getInstance().showAlert(
                                "Erro de Conexão", "Não foi possível conectar a " + username
                        );
                    }
                });
            }
        });
    }

    public static void sendMessage(String recipient, String content) {
        sendMessage(recipient, new Message(
                currentUser, recipient, content, Message.MessageType.TEXT
        ));
    }

    public static void sendFile(String recipient, File file) {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            sendMessage(recipient, new Message(
                    currentUser, recipient, file.getName(), fileData, Message.MessageType.FILE
            ));
        } catch (IOException e) {
            Platform.runLater(() -> {
                if (ChatController.getInstance() != null) {
                    ChatController.getInstance().showAlert("Erro", "Falha ao enviar arquivo");
                }
            });
        }
    }

    public static void sendMessage(String recipient, Message message) {
        if (!outputStreams.containsKey(recipient)) return;

        executorService.submit(() -> {
            try {
                outputStreams.get(recipient).writeObject(message);
                Platform.runLater(() -> {
                    if (ChatController.getInstance() != null &&
                            ChatController.getInstance().isChattingWith(recipient)) {

                        if (message.getType() == Message.MessageType.FILE) {
                            ChatController.getInstance().addSentFile(message.getFileName());
                        } else {
                            ChatController.getInstance().addSentMessage(message.getContent());
                        }
                    }
                });
            } catch (IOException e) {
                System.err.println("Erro ao enviar mensagem: " + e.getMessage());
            }
        });
    }

    public static int getServerPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : -1;
    }

    public static String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    private void shutdown() {
        if (authToken != null) logoutOnExit();

        try {
            if (serverSocket != null) serverSocket.close();
            for (Socket socket : activeConnections.values()) socket.close();
            if (executorService != null) executorService.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        return FXMLLoader.load(App.class.getResource("/com/chatp2p/views/" + fxml + ".fxml"));
    }

    public static Stage getPrimaryStage() { return primaryStage; }
    public static void setAuthToken(String token) { authToken = token; }
    public static String getAuthToken() { return authToken; }

    private static void logoutOnExit() {
        new Thread(() -> {
            try {
                HttpClient.newHttpClient().send(
                        HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/api/auth/logout"))
                                .header("Authorization", "Bearer " + authToken)
                                .POST(HttpRequest.BodyPublishers.noBody())
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
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