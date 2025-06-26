package com.chatp2p.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import com.chatp2p.exceptions.*;

import java.net.URL;

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
            if (profileImageUrl != null) {
                URL imageUrl = getClass().getResource(profileImageUrl);
                if (imageUrl != null) {
                    userImage.setImage(new Image(imageUrl.toExternalForm()));
                } else {
                    loadDefaultImage(userImage);
                }
            } else {
                loadDefaultImage(userImage);
            }
        } catch (Exception e) {
            throw new AppException("Failed to load user image for button: " + username, e);
        }

        Label userNameLabel = new Label(username);
        userNameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        container.getChildren().addAll(userImage, userNameLabel);
        setGraphic(container);
    }

    private void loadDefaultImage(ImageView userImage) {
        URL defaultUrl = getClass().getResource("/com/chatp2p/images/default_user.png");
        if (defaultUrl != null) {
            userImage.setImage(new Image(defaultUrl.toExternalForm()));
        }
    }
}