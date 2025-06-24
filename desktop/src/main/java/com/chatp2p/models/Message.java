package com.chatp2p.models;

import java.io.Serializable;

public class Message implements Serializable {
    public enum MessageType {
        TEXT, FILE, CONNECTION_REQUEST, CONNECTION_ACCEPTED
    }

    private String sender;
    private String recipient;
    private String content;
    private String fileName;
    private byte[] fileData;
    private MessageType type;

    public Message(String sender, String recipient, String content, MessageType type) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.type = type;
    }

    public Message(String sender, String recipient, String fileName, byte[] fileData, MessageType type) {
        this.sender = sender;
        this.recipient = recipient;
        this.fileName = fileName;
        this.fileData = fileData;
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public MessageType getType() {
        return type;
    }
}