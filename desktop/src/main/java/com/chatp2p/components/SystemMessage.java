package com.chatp2p.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SystemMessage extends HBox {
    public SystemMessage(String message) {
        super();
        setAlignment(Pos.CENTER);
        setPadding(new Insets(5));

        Label label = new Label(message);
        label.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px; -fx-font-style: italic; -fx-padding: 5px;");

        Label timeLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 10px; -fx-padding: 0 0 0 5px;");

        HBox container = new HBox(5, label, timeLabel);
        container.setAlignment(Pos.CENTER);

        getChildren().add(container);
    }
}