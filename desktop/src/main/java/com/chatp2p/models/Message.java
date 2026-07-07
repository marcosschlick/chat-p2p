package com.chatp2p.models;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String sender;
    private String recipient;
    private MessageType type;
    private String content;
    private String fileName;
    private byte[] fileData;

    public Message() {
    }

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

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}