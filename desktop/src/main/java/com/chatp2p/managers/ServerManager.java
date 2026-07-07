package com.chatp2p.managers;

import com.chatp2p.exceptions.*;

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
        executorService.submit(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executorService.submit(() -> connectionManager.handleIncomingConnection(clientSocket));
                } catch (IOException e) {
                    throw new NetworkException("Failed to accept incoming connection", e);
                }
            }
        });
    }

    public void shutdown() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            throw new NetworkException("Failed to close server socket", e);
        }
        if (executorService != null) executorService.shutdownNow();
    }

    public int getServerPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : -1;
    }
}