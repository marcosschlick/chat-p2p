package com.chatp2p.managers;

import com.chatp2p.core.App;
import com.chatp2p.models.Message;
import com.chatp2p.controllers.ChatController;
import javafx.application.Platform;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.*;

public class ConnectionManager {
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    public final Map<String, Socket> activeConnections = new ConcurrentHashMap<>();
    public final Map<String, ObjectOutputStream> outputStreams = new ConcurrentHashMap<>();

    public void startP2PServer() {
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
                        App.getCurrentUser(), sender, "Conexão estabelecida", Message.MessageType.CONNECTION_ACCEPTED
                ));

                startMessageListener(sender, ois);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startMessageListener(String sender, ObjectInputStream ois) {
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

    public void connectToPeer(String username, String ip, int port) {
        executorService.submit(() -> {
            try {
                Socket socket = new Socket(ip, port);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                oos.writeObject(new Message(
                        App.getCurrentUser(), username, "Solicitação de conexão", Message.MessageType.CONNECTION_REQUEST
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

    public void sendMessage(String recipient, String content) {
        sendMessage(recipient, new Message(
                App.getCurrentUser(), recipient, content, Message.MessageType.TEXT
        ));
    }

    public void sendFile(String recipient, File file) {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            sendMessage(recipient, new Message(
                    App.getCurrentUser(), recipient, file.getName(), fileData, Message.MessageType.FILE
            ));
        } catch (IOException e) {
            Platform.runLater(() -> {
                if (ChatController.getInstance() != null) {
                    ChatController.getInstance().showAlert("Erro", "Falha ao enviar arquivo");
                }
            });
        }
    }

    public void sendMessage(String recipient, Message message) {
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

    public int getServerPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : -1;
    }

    public void shutdown() {
        try {
            if (serverSocket != null) serverSocket.close();
            for (Socket socket : activeConnections.values()) socket.close();
            if (executorService != null) executorService.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}