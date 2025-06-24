package com.chatp2p.managers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerManager {
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private final ConnectionManager connectionManager;

    public ServerManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void startP2PServer(int port) throws IOException {
        executorService = Executors.newCachedThreadPool();
        serverSocket = new ServerSocket(port);
        System.out.println("Servidor P2P na porta: " + serverSocket.getLocalPort());

        executorService.submit(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executorService.submit(() -> connectionManager.handleIncomingConnection(clientSocket));
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) e.printStackTrace();
                }
            }
        });
    }

    public void shutdown() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar servidor: " + e.getMessage());
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public int getServerPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : -1;
    }
}