package com.aragoj.ui.cellfactory;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.awt.*;
import java.io.IOException;

public class ImageListViewCellController {

    @FXML private Pane statusPane;
    @FXML private ImageView image;
    @FXML private Label primaryLabel;
    @FXML private Label secondaryLabel;
    @FXML private HBox hBox;

    public ImageListViewCellController(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fmxl/ImageListViewCell.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setStatusPaneColor(String color){
        statusPane.setStyle("-fx-background-color: " + color +";");
    }
    public void setImage(Image img){
        image.setImage(img);
    }

    public void setPrimaryLabel(String text){
        primaryLabel.setText(text);
    }

    public void setSecondaryLabel(String text){
        secondaryLabel.setText(text);
    }

    public HBox getHBox(){
        return hBox;
    }

    public void setSecondaryFieldTextColor(String color) {
        this.secondaryLabel.setStyle("-fx-text-fill:"+color+";");
    }
}
