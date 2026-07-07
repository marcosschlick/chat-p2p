package com.chatp2p.components;


import com.chatp2p.models.ImageOption;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class ImageSelector extends Dialog<String> {

    public ImageSelector(List<ImageOption> options) {
        setTitle("Selecione a foto do perfil");
        setHeaderText("Escolha uma imagem:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        int col = 0;
        int row = 0;
        for (ImageOption option : options) {
            VBox container = createImageContainer(option);
            grid.add(container, col, row);
            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResultConverter(button -> {
            if (button == ButtonType.OK && getResult() != null) {
                return getResult();
            }
            return null;
        });
    }

    private VBox createImageContainer(ImageOption option) {
        ImageView imageView = new ImageView(option.getImage());
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(false);

        VBox container = new VBox(imageView);
        container.setAlignment(javafx.geometry.Pos.CENTER);
        container.setPadding(new Insets(5));
        container.setStyle("-fx-border-color: #555555; -fx-border-radius: 5;");
        container.setOnMouseClicked(e -> {
            setResult(option.getName());
            close();
        });
        return container;
    }

}