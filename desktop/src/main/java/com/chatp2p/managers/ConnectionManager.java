package com.chatp2p.managers;


import com.chatp2p.controllers.ChatController;
import com.chatp2p.core.App;
import com.chatp2p.exceptions.AppException;
import com.chatp2p.exceptions.ConnectionException;
import com.chatp2p.exceptions.NetworkException;
import com.chatp2p.models.Message;
import com.chatp2p.models.MessageType;
import com.chatp2p.repositories.MessageRepository;
import com.chatp2p.services.UserService;
import javafx.application.Platform;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConnectionManager {
    private ServerManager serverManager;
    private ExecutorService executorService;
    private MessageRepository messageRepository;
    public final Map<String, PeerConnection> connections = new ConcurrentHashMap<>();

    public void startP2PServer() {
        try {
            serverManager = new ServerManager(this);
            serverManager.startP2PServer(55555);
            executorService = Executors.newCachedThreadPool();
            messageRepository = new MessageRepository();
        } catch (IOException e) {
            throw new NetworkException("Failed to start P2P server", e);
        }
    }

    public void handleIncomingConnection(Socket socket) {
        try {
            PeerConnection connection = new PeerConnection(socket);
            Message message = connection.receiveMessage();
            if (message.getType() == MessageType.CONNECTION_REQUEST) {
                String sender = message.getSender();
                connections.put(sender, connection);
                Platform.runLater(() -> {
                    if (ChatController.getInstance() != null) {
                        ChatController.getInstance().addSystemMessage(sender + " entrou no chat");
                    }
                });
                startMessageListener(sender, connection);
            }
        } catch (Exception e) {
            throw new ConnectionException("Failed to handle incoming connection", e);
        }
    }

    private void startMessageListener(String sender, PeerConnection connection) {
        executorService.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Message message = connection.receiveMessage();

                    if (messageRepository != null && App.getUserProfile() != null) {
                        try {
                            Long recipientId = UserService.getRemoteUserId(sender);
                            if (recipientId != null) {
                                messageRepository.save(message, recipientId, App.getUserProfile().getId());
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    Platform.runLater(() -> {
                        if (ChatController.getInstance() != null && ChatController.getInstance().isChattingWith(sender)) {
                            switch (message.getType()) {
                                case FILE:
                                    ChatController.getInstance().addReceivedFile(message.getFileName(), message.getFileData());
                                    break;
                                case TEXT:
                                    ChatController.getInstance().addReceivedMessage(message.getContent());
                                    break;
                                case SYSTEM:
                                    ChatController.getInstance().addSystemMessage(message.getContent());
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
            } catch (Exception e) {
                if (!(e instanceof SocketException || e instanceof EOFException)) {
                    throw new ConnectionException("Error in message listener for user: " + sender, e);
                }
            } finally {
                try {
                    connection.close();
                } catch (Exception ignored) {
                }
                connections.remove(sender);
            }
        });
    }

    public void connectToPeer(String username, String ip, int port) {
        executorService.submit(() -> {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 5000);
                PeerConnection connection = new PeerConnection(socket);
                connection.sendMessage(new Message(App.getUserProfile().getUsername(), username, "Connection request", MessageType.CONNECTION_REQUEST));
                connections.put(username, connection);
                startMessageListener(username, connection);
                Platform.runLater(() -> {
                    if (ChatController.getInstance() != null) {
                        ChatController.getInstance().addSystemMessage("Você entrou no chat com " + username);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (ChatController.getInstance() != null) {
                        ChatController.getInstance().showAlert("Erro de Conexão", "Não foi possível conectar com " + username + ": " + e.getMessage());
                    }
                });
                throw new ConnectionException("Failed to connect to peer: " + username, e);
            }
        });
    }

    public void sendMessage(String recipient, String content) {
        sendMessage(recipient, new Message(App.getUserProfile().getUsername(), recipient, content, MessageType.TEXT));
    }

    public void sendFile(String recipient, File file) {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            sendMessage(recipient, new Message(App.getUserProfile().getUsername(), recipient, file.getName(), fileData, MessageType.FILE));
        } catch (IOException e) {
            Platform.runLater(() -> {
                if (ChatController.getInstance() != null) {
                    ChatController.getInstance().showAlert("Erro", "Falha ao enviar arquivo");
                }
            });
            throw new NetworkException("Failed to send file to " + recipient, e);
        }
    }

    public void sendMessage(String recipient, Message message) {
        PeerConnection connection = connections.get(recipient);
        if (connection == null) return;

        if (messageRepository != null && App.getUserProfile() != null) {
            try {
                Long recipientId = UserService.getRemoteUserId(recipient);
                if (recipientId != null) {
                    messageRepository.save(message, App.getUserProfile().getId(), recipientId);
                }
            } catch (Exception ignored) {
            }
        }

        executorService.submit(() -> {
            try {
                connection.sendMessage(message);
                Platform.runLater(() -> {
                    if (ChatController.getInstance() != null && ChatController.getInstance().isChattingWith(recipient)) {
                        if (message.getType() == MessageType.FILE) {
                            ChatController.getInstance().addSentFile(message.getFileName());
                        } else {
                            ChatController.getInstance().addSentMessage(message.getContent());
                        }
                    }
                });
            } catch (IOException e) {
                throw new NetworkException("Failed to send message to " + recipient, e);
            }
        });
    }

    public int getServerPort() {
        return serverManager != null ? serverManager.getServerPort() : -1;
    }

    public void shutdown() {
        if (serverManager != null) serverManager.shutdown();
        for (PeerConnection connection : connections.values()) {
            try {
                connection.close();
            } catch (Exception e) {
                throw new NetworkException("Failed to close connection during shutdown", e);
            }
        }
        if (executorService != null) {
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new AppException("Shutdown interrupted", e);
            }
        }
    }

    public void notifyUserLeft(String username) {
        PeerConnection connection = connections.get(username);
        if (connection != null) {
            sendMessage(username, new Message(App.getUserProfile().getUsername(), username, App.getUserProfile().getUsername() + " saiu do chat", MessageType.SYSTEM));
        }
    }

    public void notifyAppClosing() {
        for (String user : connections.keySet()) {
            PeerConnection connection = connections.get(user);
            if (connection != null) {
                try {
                    Message msg = new Message(App.getUserProfile().getUsername(), user, App.getUserProfile().getUsername() + " fechou o chat-p2p", MessageType.SYSTEM);
                    connection.sendMessage(msg);
                } catch (IOException e) {
                    throw new NetworkException("Failed to notify user about app closing: " + user, e);
                }
            }
        }
    }
}