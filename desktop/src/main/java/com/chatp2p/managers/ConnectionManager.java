package com.chatp2p.managers;

import com.chatp2p.core.App;
import com.chatp2p.controllers.ChatController;
import com.chatp2p.models.Message;
import com.chatp2p.exceptions.*;
import javafx.application.Platform;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.*;

public class ConnectionManager {
    private ServerManager serverManager;
    private ExecutorService executorService;
    private MessageRepository messageRepository;
    public final Map<String, Socket> activeConnections = new ConcurrentHashMap<>();
    public final Map<String, ObjectOutputStream> outputStreams = new ConcurrentHashMap<>();

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
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Message message = (Message) ois.readObject();
            if (message.getType() == Message.MessageType.CONNECTION_REQUEST) {
                String sender = message.getSender();
                activeConnections.put(sender, socket);
                outputStreams.put(sender, new ObjectOutputStream(socket.getOutputStream()));
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ChatController.getInstance() != null) {
                            ChatController.getInstance().addSystemMessage(sender + " entrou no chat");
                        }
                    }
                });
                startMessageListener(sender, ois);
            }
        } catch (Exception e) {
            throw new ConnectionException("Failed to handle incoming connection", e);
        }
    }

    private void startMessageListener(String sender, ObjectInputStream ois) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Message message = (Message) ois.readObject();
                        
                        if (messageRepository != null && App.getUserProfile() != null) {
                            try {
                                Long recipientId = getUserRemoteId(sender);
                                if (recipientId != null) {
                                    messageRepository.save(message, recipientId, App.getUserProfile().getId());
                                }
                            } catch (Exception e) {
                                // Ignorar erros de salvamento
                            }
                        }
                        
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (ChatController.getInstance() != null && ChatController.getInstance().isChattingWith(sender)) {
                                    if (message.getType() == Message.MessageType.FILE) {
                                        ChatController.getInstance().addReceivedFile(message.getFileName(), message.getFileData());
                                    } else if (message.getType() == Message.MessageType.TEXT) {
                                        ChatController.getInstance().addReceivedMessage(message.getContent());
                                    } else if (message.getType() == Message.MessageType.SYSTEM) {
                                        ChatController.getInstance().addSystemMessage(message.getContent());
                                    }
                                }
                            }
                        });
                    }
                } catch (SocketException | EOFException e) {
                    // Connection closed, do nothing
                } catch (Exception e) {
                    throw new ConnectionException("Error in message listener for user: " + sender, e);
                } finally {
                    try {
                        if (activeConnections.containsKey(sender)) {
                            activeConnections.get(sender).close();
                        }
                    } catch (IOException e) {
                        throw new NetworkException("Failed to close socket for user: " + sender, e);
                    }
                    activeConnections.remove(sender);
                    outputStreams.remove(sender);
                }
            }
        });
    }

    public void connectToPeer(String username, String ip, int port) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, port), 5000);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(new Message(App.getUserProfile().getUsername(), username, "Connection request", Message.MessageType.CONNECTION_REQUEST));
                    activeConnections.put(username, socket);
                    outputStreams.put(username, oos);
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    startMessageListener(username, ois);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ChatController.getInstance() != null) {
                                ChatController.getInstance().addSystemMessage("Você entrou no chat com " + username);
                            }
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ChatController.getInstance() != null) {
                                ChatController.getInstance().showAlert("Erro de Conexão", "Não foi possível conectar com " + username + ": " + e.getMessage());
                            }
                        }
                    });
                    throw new ConnectionException("Failed to connect to peer: " + username, e);
                }
            }
        });
    }

    public void sendMessage(String recipient, String content) {
        sendMessage(recipient, new Message(App.getUserProfile().getUsername(), recipient, content, Message.MessageType.TEXT));
    }

    public void sendFile(String recipient, File file) {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            sendMessage(recipient, new Message(App.getUserProfile().getUsername(), recipient, file.getName(), fileData, Message.MessageType.FILE));
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
        if (!outputStreams.containsKey(recipient)) return;
        
        if (messageRepository != null && App.getUserProfile() != null) {
            try {
                Long recipientId = getUserRemoteId(recipient);
                if (recipientId != null) {
                    messageRepository.save(message, App.getUserProfile().getId(), recipientId);
                }
            } catch (Exception e) {
                // Ignorar erros de salvamento
            }
        }
        
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    outputStreams.get(recipient).writeObject(message);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ChatController.getInstance() != null && ChatController.getInstance().isChattingWith(recipient)) {
                                if (message.getType() == Message.MessageType.FILE) {
                                    ChatController.getInstance().addSentFile(message.getFileName());
                                } else {
                                    ChatController.getInstance().addSentMessage(message.getContent());
                                }
                            }
                        }
                    });
                } catch (IOException e) {
                    throw new NetworkException("Failed to send message to " + recipient, e);
                }
            }
        });
    }

    private Long getUserRemoteId(String username) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:8080/api/users/by-username/" + username))
                .GET()
                .build();
            
            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode json = mapper.readTree(response.body());
                return json.get("id").asLong();
            }
        } catch (Exception e) {
            // Ignorar erros de busca de ID
        }
        return null;
    }

    public int getServerPort() {
        return serverManager != null ? serverManager.getServerPort() : -1;
    }

    public void shutdown() {
        if (serverManager != null) serverManager.shutdown();
        for (Socket socket : activeConnections.values()) {
            try { socket.close(); } catch (IOException e) {
                throw new NetworkException("Failed to close socket during shutdown", e);
            }
        }
        if (executorService != null) {
            executorService.shutdownNow();
            try { executorService.awaitTermination(2, TimeUnit.SECONDS); } catch (InterruptedException e) {
                throw new AppException("Shutdown interrupted", e);
            }
        }
    }

    public void notifyUserLeft(String username) {
        if (outputStreams.containsKey(username)) {
            sendMessage(username, new Message(App.getUserProfile().getUsername(), username, App.getUserProfile().getUsername() + " saiu do chat", Message.MessageType.SYSTEM));
        }
    }

    public void notifyAppClosing() {
        for (String user : activeConnections.keySet()) {
            if (outputStreams.containsKey(user)) {
                try {
                    Message msg = new Message(App.getUserProfile().getUsername(), user, App.getUserProfile().getUsername() + " fechou o chat-p2p", Message.MessageType.SYSTEM);
                    outputStreams.get(user).writeObject(msg);
                    outputStreams.get(user).flush();
                } catch (IOException e) {
                    throw new NetworkException("Failed to notify user about app closing: " + user, e);
                }
            }
        }
    }
}