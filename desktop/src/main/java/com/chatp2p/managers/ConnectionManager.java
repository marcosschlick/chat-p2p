package com.chatp2p.managers;

import com.chatp2p.core.App;
import com.chatp2p.controllers.ChatController;
import com.chatp2p.models.Message;
import com.chatp2p.exceptions.*;
import com.chatp2p.models.MessageType;
import com.chatp2p.repositories.MessageRepository;
import com.chatp2p.services.UserService;
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
            if (message.getType() == MessageType.CONNECTION_REQUEST) {
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
                            } catch (Exception ignored) {
                            }
                        }

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (ChatController.getInstance() != null && ChatController.getInstance().isChattingWith(sender)) {
                                    if (message.getType() == MessageType.FILE) {
                                        ChatController.getInstance().addReceivedFile(message.getFileName(), message.getFileData());
                                    } else if (message.getType() == MessageType.TEXT) {
                                        ChatController.getInstance().addReceivedMessage(message.getContent());
                                    } else if (message.getType() == MessageType.SYSTEM) {
                                        ChatController.getInstance().addSystemMessage(message.getContent());
                                    }
                                }
                            }
                        });
                    }
                } catch (SocketException | EOFException ignored) {
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
                    oos.writeObject(new Message(App.getUserProfile().getUsername(), username, "Connection request", MessageType.CONNECTION_REQUEST));
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
        if (!outputStreams.containsKey(recipient)) return;

        if (messageRepository != null && App.getUserProfile() != null) {
            try {
                Long recipientId = getUserRemoteId(recipient);
                if (recipientId != null) {
                    messageRepository.save(message, App.getUserProfile().getId(), recipientId);
                }
            } catch (Exception ignored) {
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
                                if (message.getType() == MessageType.FILE) {
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
        return UserService.getRemoteUserId(username);
    }

    public int getServerPort() {
        return serverManager != null ? serverManager.getServerPort() : -1;
    }

    public void shutdown() {
        if (serverManager != null) serverManager.shutdown();
        for (Socket socket : activeConnections.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new NetworkException("Failed to close socket during shutdown", e);
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
        if (outputStreams.containsKey(username)) {
            sendMessage(username, new Message(App.getUserProfile().getUsername(), username, App.getUserProfile().getUsername() + " saiu do chat", MessageType.SYSTEM));
        }
    }

    public void notifyAppClosing() {
        for (String user : activeConnections.keySet()) {
            if (outputStreams.containsKey(user)) {
                try {
                    Message msg = new Message(App.getUserProfile().getUsername(), user, App.getUserProfile().getUsername() + " fechou o chat-p2p", MessageType.SYSTEM);
                    outputStreams.get(user).writeObject(msg);
                    outputStreams.get(user).flush();
                } catch (IOException e) {
                    throw new NetworkException("Failed to notify user about app closing: " + user, e);
                }
            }
        }
    }
}