package ui.cellfactory;

import com.jfoenix.controls.JFXListCell;
import imageprocess.ImageItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Shape;
import session.model.EditorItem;
import ui.model.UIEditorItem;

public class ImageListViewCell extends JFXListCell<UIEditorItem> {

    private ImageListViewCellController controller = new ImageListViewCellController();

    public ImageListViewCell(){ }

    @Override
    public void updateItem(UIEditorItem item, boolean empty) {
        super.updateItem(item,empty);
        if(!isEmpty() && item != null && item.getImageItem() != null){
            ImageItem img = item.getImageItem();
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
