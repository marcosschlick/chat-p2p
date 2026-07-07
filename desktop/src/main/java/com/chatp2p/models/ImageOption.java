package com.chatp2p.models;

import javafx.scene.image.Image;

public class ImageOption {
    private String name;
    private Image image;

    public ImageOption(String name, Image image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
