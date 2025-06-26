package com.chatp2p.components;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.util.Optional;

public class ImageSelectionDialog {
    private final String[] availableImages;
    private Dialog<String> dialog;

    public ImageSelectionDialog(String[] availableImages) {
        this.availableImages = availableImages;
        initializeDialog();
    }

    private void initializeDialog() {
        dialog = new Dialog<>();
        dialog.setTitle("Selecione a foto do perfil");
        dialog.setHeaderText("Escolha uma imagem:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        int col = 0;
        int row = 0;
        for (String imageName : availableImages) {
            try {
                InputStream is = getClass().getResourceAsStream("/com/chatp2p/images/" + imageName);
                if (is == null) {
                    System.err.println("Image not found: " + imageName);
                    continue;
                }
                VBox container = createImageContainer(imageName, is);

                grid.add(container, col, row);
                col++;
                if (col > 2) {
                    col = 0;
                    row++;
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load image: " + imageName, e);
            }
        }

        assert dialog != null;
        dialog.getDialogPane().setContent(grid);
    }

    private VBox createImageContainer(String imageName, InputStream is) {
        Image image = new Image(is);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        VBox container = new VBox(imageView);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(5));
        container.setStyle("-fx-border-color: #555555; -fx-border-radius: 5;");
        container.setOnMouseClicked(e -> {
            dialog.setResult(imageName);
        });

        return container;
    }

    public Optional<String> showAndWait() {
        return dialog.showAndWait();
    }
}