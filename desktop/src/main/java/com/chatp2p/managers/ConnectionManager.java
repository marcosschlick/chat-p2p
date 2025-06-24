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
            serverSocket = new ServerSocket(55555);
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

                // Apenas notificar o receptor
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
                while (true) {
                    Message message = (Message) ois.readObject();

                    // Tratar mensagem de conexão
                    if (message.getType() == Message.MessageType.CONNECTION_REQUEST) {
                        Platform.runLater(() -> {
                            if (ChatController.getInstance() != null &&
                                    ChatController.getInstance().isChattingWith(sender)) {
                                ChatController.getInstance().addSystemMessage(sender + " entrou no chat");
                            }
                        });
                        continue;
                    }

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
            } catch (SocketException e) {
                // Conexão fechada normalmente
            } catch (Exception e) {
                System.err.println("Erro na conexão com " + sender + ": " + e.getMessage());
            } finally {
                Platform.runLater(() -> {
                    if (ChatController.getInstance() != null &&
                            ChatController.getInstance().isChattingWith(sender)) {
                        ChatController.getInstance().addSystemMessage(sender + " desconectou");
                    }
                });

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

                // Apenas notificar o iniciador
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
        return serverSocket != null ? serverSocket.getLocalPort() : -1;
    }

    public void shutdown() {
        notifyAppClosing();

        try {
            if (serverSocket != null) {
                serverSocket.close();
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
                if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService não terminou a tempo");
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
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
                sendMessage(user, new Message(
                        App.getCurrentUser(),
                        user,
                        App.getCurrentUser() + " fechou o aplicativo chat-p2p",
                        Message.MessageType.SYSTEM
                ));
            }
        }
    }

}