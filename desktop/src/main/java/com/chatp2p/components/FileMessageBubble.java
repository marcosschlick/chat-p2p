package com.chatp2p.components;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class FileMessageBubble extends MessageBubble {
    public FileMessageBubble(String fileName, boolean sent) {
        super("", sent);

        VBox bubble = (VBox) getChildren().get(0);
        bubble.getChildren().set(0, new Label("📎 " + fileName));
    }
}