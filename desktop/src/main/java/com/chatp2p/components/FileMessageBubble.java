package com.chatp2p.components;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FileMessageBubble extends MessageBubble {
    public FileMessageBubble(String fileName, boolean sent) {
        super("", sent);

        VBox bubble = (VBox) getChildren().get(0);

        Image clipIcon = new Image(getClass().getResourceAsStream("/com/chatp2p/icons/paperclip.png"));

        HBox container = new HBox();
        container.setSpacing(5);

        ImageView iconView = new ImageView(clipIcon);
        iconView.setFitWidth(16);
        iconView.setFitHeight(16);

        container.getChildren().addAll(iconView, new Label(fileName));

        bubble.getChildren().set(0, container);
    }
}