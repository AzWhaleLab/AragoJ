package com.aragoj.ui.cellfactory;

import com.jfoenix.controls.JFXListCell;
import com.aragoj.imageprocess.ImageItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Shape;


public class ImageListViewCell extends JFXListCell<ImageItem> {

    private ImageListViewCellController controller = new ImageListViewCellController();

    public ImageListViewCell(){ }
    @Override
    public void updateItem(ImageItem img, boolean empty) {
        super.updateItem(img,empty);
        if(img != null && !empty){
            try {
                Image imge = img.getImageIcon(50);
                controller.setImage(imge);
                controller.setPrimaryLabel(img.getName());
                controller.setSecondaryLabel(img.getPath());
                controller.setStatusPaneColor(img.getStatusColor());
                setGraphic(controller.getHBox());
                layoutChildren();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            setGraphic(null);
        }
    }

    @Override
    protected void makeChildrenTransparent() {
        for (Node child : getChildren()) {
            if (child instanceof Label || child instanceof Shape || child instanceof HBox) {
                child.setMouseTransparent(true);
            }
        }
    }
}
