package com.aragoj.ui.cellfactory;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class MetadataTagTreeCellController {
    @FXML private ImageView image;
    @FXML private Label primaryLabel;
    @FXML private HBox hBox;

    private static final String DEFAULT_STYLE_CLASS = "metadata-tree-table-cell";

    public MetadataTagTreeCellController(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fmxl/MetadataTreeTableCell.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    private void initialize(){
        hBox.getStyleClass().addAll(DEFAULT_STYLE_CLASS);
        primaryLabel.getStyleClass().addAll(DEFAULT_STYLE_CLASS);
    }

    public void setPrimaryLabelText(String text) {
        this.primaryLabel.setText(text);
    }

    public ImageView getImage() {
        return image;
    }

    public void setExportVisibility(boolean visible){
        image.setVisible(visible);
    }

    public void setExportImageManaged(boolean managed){
        image.setManaged(managed);
    }

    public HBox getHBox(){
        return hBox;
    }
}
