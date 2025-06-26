package com.chatp2p.managers;

import com.chatp2p.models.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PeerConnection {
    private final Socket socket;
    private final ObjectOutputStream output;
    private final ObjectInputStream input;

    public PeerConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.output = new ObjectOutputStream(socket.getOutputStream());
        this.input = new ObjectInputStream(socket.getInputStream());
    }

    public void sendMessage(Message message) throws IOException {
        output.writeObject(message);
    }

    public Message receiveMessage() throws IOException, ClassNotFoundException {
        return (Message) input.readObject();
    }

    public void close() {
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException ignored) {
        }
    }
}