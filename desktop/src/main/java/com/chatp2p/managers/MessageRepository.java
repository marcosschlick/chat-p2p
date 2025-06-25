package com.chatp2p.managers;

import com.chatp2p.models.Message;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {

    public void save(Message msg, Long senderId, Long recipientId) {
        if (msg.getType() != Message.MessageType.TEXT && msg.getType() != Message.MessageType.FILE) {
            return;
        }

        String sql = "INSERT INTO messages (sender_id, recipient_id, type, content) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setLong(1, senderId);
            ps.setLong(2, recipientId);
            ps.setString(3, msg.getType().name());
            ps.setString(4, msg.getType() == Message.MessageType.FILE ? msg.getFileName() : msg.getContent());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao gravar histórico", e);
        }
    }

    public List<Message> findHistory(Long userA, Long userB) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT sender_id, type, content, created_at FROM messages " +
                    "WHERE (sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?) " +
                    "ORDER BY created_at ASC";
        
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setLong(1, userA);
            ps.setLong(2, userB);
            ps.setLong(3, userB);
            ps.setLong(4, userA);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long senderId = rs.getLong("sender_id");
                    String type = rs.getString("type");
                    String content = rs.getString("content");
                    Timestamp timestamp = rs.getTimestamp("created_at");
                    
                    Message.MessageType messageType = Message.MessageType.valueOf(type);
                    Message message = new Message();
                    message.setType(messageType);
                    message.setContent(content);
                    
                    boolean isSent = senderId.equals(userA);
                    message.setSender(isSent ? "Você" : "Outro");
                    message.setRecipient(isSent ? "Outro" : "Você");
                    
                    if (messageType == Message.MessageType.FILE) {
                        message.setFileName(content);
                    }
                    
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao carregar histórico", e);
        }
        
        return messages;
    }
} 