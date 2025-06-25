package com.chatp2p.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class SystemMessage extends HBox {
    public SystemMessage(String message) {
        super();
        setAlignment(Pos.CENTER);
        setPadding(new Insets(2));
        Label label = new Label(message);
        label.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px; -fx-font-style: italic;");
        getChildren().add(label);
    }
}