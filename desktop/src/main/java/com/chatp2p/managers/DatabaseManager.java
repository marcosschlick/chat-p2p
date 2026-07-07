package com.chatp2p.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:h2:file:./database/chatp2p-db";
    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS messages (" + "id BIGINT AUTO_INCREMENT PRIMARY KEY," + "sender_id BIGINT," + "recipient_id BIGINT," + "type VARCHAR(30)," + "content CLOB," + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" + ")";

    private static final Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(URL);
            connection.createStatement().executeUpdate(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao iniciar banco local", e);
        }
    }

    public static Connection getConnection() {
        return connection;
    }
} 