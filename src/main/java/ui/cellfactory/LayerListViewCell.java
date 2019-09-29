package ui.cellfactory;

import com.jfoenix.controls.JFXListCell;
import com.jfoenix.svg.SVGGlyph;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import ui.controller.LayerTabPageController;
import ui.custom.AreaGroup;
import ui.custom.LineGroup;
import ui.model.LayerListItem;
import ui.model.ScaleRatio;
import utils.Utility;

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
            if(currentScale != null && currentScale.hasScale() && item instanceof LineGroup){
                LineGroup lineGroup = (LineGroup) item;
                secText += " - " + currentScale.getRoundedScaledValue(lineGroup.getLength())  + " " + currentScale.getUnits();
            }
            if(currentScale != null && currentScale.hasScale() && item instanceof AreaGroup){
                AreaGroup areaGroup = (AreaGroup) item;
                secText += " - " + currentScale.getRoundedScaledValue(areaGroup.calculateArea())  + " " + currentScale.getUnits();
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
