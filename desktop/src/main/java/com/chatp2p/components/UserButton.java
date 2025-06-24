package com.chatp2p.components;

import com.chatp2p.core.App;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class UserButton extends Button {
    private final String username;

    public UserButton(String username, String profileImageUrl) {
        super();
        this.username = username;
        render(profileImageUrl);
    }

    private void render(String profileImageUrl) {
        VBox userContainer = new VBox(5);
        userContainer.setAlignment(Pos.CENTER);

        ImageView userImage = new ImageView();
        userImage.setFitWidth(60);
        userImage.setFitHeight(60);
        userImage.setPreserveRatio(true);

        try {
            if (profileImageUrl == null || profileImageUrl.isEmpty()) {
                profileImageUrl = App.class.getResource("/com/chatp2p/images/default_user.jpg").toString();
            }
            userImage.setImage(new Image(profileImageUrl, true));
        } catch (Exception e) {
            userImage.setImage(new Image(App.class.getResource("/com/chatp2p/images/default_user.jpg").toString()));
        }

        Label userNameLabel = new Label(username);
        userNameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        userContainer.getChildren().addAll(userImage, userNameLabel);

        setGraphic(userContainer);
        getStyleClass().add("user-button");
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public String getUsername() {
        return username;
    }
}