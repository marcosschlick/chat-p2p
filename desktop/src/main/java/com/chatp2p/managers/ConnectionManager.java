package com.chatp2p.managers;

import com.chatp2p.core.App;
import com.chatp2p.controllers.ChatController;
import com.chatp2p.models.Message;
import javafx.application.Platform;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.*;

public class ConnectionManager {
    private ServerManager serverManager;
    private ExecutorService executorService;
    public final Map<String, Socket> activeConnections = new ConcurrentHashMap<>();
    public final Map<String, ObjectOutputStream> outputStreams = new ConcurrentHashMap<>();

    public void startP2PServer() {
        try {
            serverManager = new ServerManager(this);
            serverManager.startP2PServer(55555);
            executorService = Executors.newCachedThreadPool();
        } catch (IOException e) {
            e.printStackTrace();
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

                Platform.runLater(() -> {
                    if (ChatController.getInstance() != null) {
                        ChatController.getInstance().addSystemMessage(sender + " entrou no chat");
                    }
                });

                startMessageListener(sender, ois);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startMessageListener(String sender, ObjectInputStream ois) {
        executorService.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
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
                            } else if (message.getType() == Message.MessageType.SYSTEM) {
                                ChatController.getInstance().addSystemMessage(message.getContent());
                            }
                        }
                    });
                }
            } catch (SocketException | EOFException e) {
                // Conexão fechada normalmente
            } catch (Exception e) {
                System.err.println("Erro na conexão com " + sender + ": " + e.getMessage());
            } finally {
                try {
                    if (activeConnections.containsKey(sender)) {
                        activeConnections.get(sender).close();
                    }
                } catch (IOException ex) {
                    System.err.println("Erro ao fechar socket: " + ex.getMessage());
                }

                activeConnections.remove(sender);
                outputStreams.remove(sender);
            }
        });
    }

    public void connectToPeer(String username, String ip, int port) {
        executorService.submit(() -> {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 5000);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(new Message(
                        App.getCurrentUser(), username,
                        "Solicitação de conexão",
                        Message.MessageType.CONNECTION_REQUEST
                ));

                activeConnections.put(username, socket);
                outputStreams.put(username, oos);

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                startMessageListener(username, ois);

                Platform.runLater(() -> {
                    if (ChatController.getInstance() != null) {
                        ChatController.getInstance().addSystemMessage("Você entrou no chat com " + username);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (ChatController.getInstance() != null) {
                        ChatController.getInstance().showAlert(
                                "Erro de Conexão",
                                "Não foi possível conectar a " + username + ": " + e.getMessage()
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
        return serverManager != null ? serverManager.getServerPort() : -1;
    }

    public void shutdown() {
        notifyAppClosing();

        if (serverManager != null) {
            serverManager.shutdown();
        }

        for (Socket socket : activeConnections.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar socket: " + e.getMessage());
            }
        }

        if (executorService != null) {
            executorService.shutdownNow();
            try {
                if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService não terminou a tempo");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyUserLeft(String username) {
        if (outputStreams.containsKey(username)) {
            sendMessage(username, new Message(
                    App.getCurrentUser(),
                    username,
                    App.getCurrentUser() + " saiu do chat",
                    Message.MessageType.SYSTEM
            ));
        }
    }

    public void notifyAppClosing() {
        for (String user : activeConnections.keySet()) {
            if (outputStreams.containsKey(user)) {
                try {
                    Message msg = new Message(
                            App.getCurrentUser(),
                            user,
                            App.getCurrentUser() + " fechou o aplicativo chat-p2p",
                            Message.MessageType.SYSTEM
                    );
                    outputStreams.get(user).writeObject(msg);
                    outputStreams.get(user).flush();
                } catch (IOException e) {
                    System.err.println("Erro ao notificar fechamento para " + user + ": " + e.getMessage());
                }
            }
        }
    }
}