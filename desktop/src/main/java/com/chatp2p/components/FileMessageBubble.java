package com.chatp2p.components;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FileMessageBubble extends HBox {
    public FileMessageBubble(String fileName, boolean sent) {
        render(fileName, sent);
    }

    private void render(String fileName, boolean sent) {
        setPadding(new Insets(5, 10, 5, 10));
        setAlignment(sent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(8, 12, 8, 12));
        bubble.setStyle("-fx-background-color: " + (sent ? "#DCF8C6" : "#FFFFFF") + "; -fx-background-radius: 12;");

        Label fileLabel = new Label("📎 " + fileName);
        fileLabel.setWrapText(true);
        fileLabel.setFont(Font.font("Segoe UI", 14));
        fileLabel.setMaxWidth(300);

        Label timeLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setStyle("-fx-text-fill: #777777; -fx-font-size: 10px;");

        bubble.getChildren().addAll(fileLabel, timeLabel);
        getChildren().add(bubble);
    }
}