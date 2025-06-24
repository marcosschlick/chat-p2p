package com.chatp2p.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class UserButton extends Button {
    public UserButton(String username, String profileImageUrl) {
        super();
        getStyleClass().add("user-button");
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-radius: 10;");

        ImageView userImage = new ImageView();
        userImage.setFitWidth(60);
        userImage.setFitHeight(60);
        userImage.setPreserveRatio(true);

        try {
            if (profileImageUrl == null || profileImageUrl.isEmpty()) {
                profileImageUrl = getClass().getResource("/com/chatp2p/images/default_user.png").toString();
            }
            userImage.setImage(new Image(profileImageUrl, true));
        } catch (Exception e) {
            userImage.setImage(new Image(getClass().getResource("/com/chatp2p/images/default_user.png").toString()));
        }

        Label userNameLabel = new Label(username);
        userNameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        container.getChildren().addAll(userImage, userNameLabel);
        setGraphic(container);
    }
}