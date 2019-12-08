package ui.cellfactory;

import com.jfoenix.controls.JFXListCell;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Shape;
import ui.controller.LayerTabPageController;
import ui.custom.area.AreaGroup;
import ui.custom.segline.SegLineGroup;
import ui.model.LayerListItem;
import ui.model.ScaleRatio;

import java.io.IOException;


public class LayerListViewCell extends JFXListCell<LayerListItem> {

    private LayerListViewCellController controller;
    private ScaleRatio currentScale;

    public LayerListViewCell(LayerListener listener, ScaleRatio currentScale){
        this.currentScale = currentScale;
        controller = new LayerListViewCellController(this, listener);
    }

    @Override
    public void updateItem(LayerListItem item, boolean empty) {
        super.updateItem(item, empty);
        if(item != null && !empty){
            try {
                controller.setLabelGraphic(item.getSVG());
            } catch (IOException e) {
                e.printStackTrace();
            }
            controller.setPrimaryText(item.getPrimaryText());
            String secText = item.getSecondaryText();
            if(currentScale != null && currentScale.hasScale() && item instanceof SegLineGroup){
                SegLineGroup lineGroup = (SegLineGroup) item;
                secText += " - " + currentScale.getRoundedScaledValue(lineGroup.getLength())  + " " + currentScale.getUnits();
            }
            if(currentScale != null && currentScale.hasScale() && item instanceof AreaGroup){
                AreaGroup areaGroup = (AreaGroup) item;
                secText += " - " + currentScale.getSquaredRoundedScaledValue(areaGroup.getRoundedArea())  + " " + currentScale.getSquaredUnits();
            }
            controller.setSecondaryLabel(secText);
            setGraphic(controller.getHBox());

        }
        else{
            setGraphic(null);
        }
    }

    @Override
    protected void makeChildrenTransparent() {
        for (Node child : getChildren()) {
            if (child instanceof Label || child instanceof Shape || child instanceof HBox) {
                //child.setMouseTransparent(true);
                child.setPickOnBounds(false);
                if(child instanceof HBox){
                    ((HBox)child).getChildren().get(0).setMouseTransparent(true);
                    ((HBox)child).getChildren().get(1).setPickOnBounds(false);
                    ((VBox)((HBox)child).getChildren().get(1)).getChildren().get(2).setMouseTransparent(true);
                    //((HBox)child).getChildren().get(1).setMouseTransparent(false);
                }
            }
        }
    }

    /**
     * Listener interface to be notified when a layer changes
     */
    public interface LayerListener{
        void onRenameLayer(int index, String name) throws LayerTabPageController.NameAlreadyTaken;
    }
}
