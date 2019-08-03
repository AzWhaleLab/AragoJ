package ui.custom;

import javafx.event.Event;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import opencv.calibration.ui.CalibrationImageItem;
import equation.model.EquationItem;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import session.model.*;
import ui.controller.LayerTabPageController;

/**
 * ScrollPane responsible for wrapping and handling zoom + panning of the ImageEditorStackGroup.
 * Both zoom + panning only work with the CTRL modifier - blocked on ImageEditorStackGroup class.
 */

public class ZoomableScrollPane extends ScrollPane {
    private double scaleValue = 1;
    private double zoomIntensity = 0.05;
    private Node target;
    private Node zoomNode;

    private ScrollPaneSkin scrollPaneSkin;

    private ImageEditorStackGroup.Mode currentMode;

    public ZoomableScrollPane(Node target) {
        super();
        this.target = target;
        this.zoomNode = new Group(target);
        setContent(outerNode(zoomNode));

        setPannable(true);
        setFitToHeight(true); //center
        setFitToWidth(true); //center

        scrollPaneSkin = new ScrollPaneSkin(this);
        setSkin(scrollPaneSkin);

        updateScale();
    }

    private Node outerNode(Node node) {
        Node outerNode = centeredNode(node);
        outerNode.setOnScroll(e -> {
            if(e.isControlDown()){
                e.consume();
                onScroll(e.getTextDeltaY(), new Point2D(e.getX(), e.getY()));
            }
        });

        return outerNode;
    }

    private Node centeredNode(Node node) {
        VBox vBox = new VBox(node);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    private void updateScale() {
        target.setScaleX(scaleValue);
        target.setScaleY(scaleValue);
    }

    public void onScroll(double wheelDelta, Point2D mousePoint) {
        double zoomFactor = Math.exp(wheelDelta * zoomIntensity);

        Bounds innerBounds = zoomNode.getLayoutBounds();
        Bounds viewportBounds = getViewportBounds();

        // calculate pixel offsets from [0, 1] range
        double valX = this.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
        double valY = this.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

        scaleValue = scaleValue * zoomFactor;
        updateScale();
        this.layout(); // refresh ScrollPane scroll positions & target bounds


        // convert target coordinates to zoomTarget coordinates
        Point2D posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint));

        // calculate adjustment of scroll position (pixels)
        Point2D adjustment = target.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

        // convert back to [0, 1] range
        // (too large/small values are automatically corrected by ScrollPane)
        Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
        this.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
        this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
    }

    public void setDefaultScale(){
        Bounds innerBounds = zoomNode.getLayoutBounds();
        Bounds viewportBounds = getViewportBounds();
        double valX = innerBounds.getWidth() - viewportBounds.getWidth();
        double valY = innerBounds.getHeight() - viewportBounds.getHeight();

        double proposedScale = scaleValue;
        if(valX > 0){
            proposedScale = (viewportBounds.getWidth()*(3.0/5))/innerBounds.getWidth();
        }

        if(valY > 0){
            double yProposedScale = (viewportBounds.getHeight()*(3.0/5))/innerBounds.getHeight();
            if(yProposedScale < proposedScale){
                proposedScale = yProposedScale;
            }
        }

        if(proposedScale < scaleValue){
            scaleValue = proposedScale;
        }
        updateScale();
        this.layout();
    }

    public void loadEditorItem(EditorItem item, ToolEventHandler handler, LayerTabPageController layerTabPageController){
        // Set zoom
        EditorItemZoom zoom = item.getZoom();
        this.scaleValue = zoom.getScale();
        updateScale();
        this.layout();
        this.setHvalue(zoom.gethValue());
        this.setVvalue(zoom.getvValue());

        if(!(target instanceof  ImageEditorStackGroup)) return;
        // Set layers (assumes image is already set)
        ImageEditorStackGroup stackGroup = (ImageEditorStackGroup) target;
        stackGroup.setLineCount(item.getLineCount());
        for(EditorItemLayer layer : item.getLayers()){
            if(layer instanceof EditorItemLine){
                LineGroup lineGroup = new LineGroup((EditorItemLine) layer, (LineGroup.LineEventHandler) handler);
                stackGroup.addElement(lineGroup, true);
            } else if(layer instanceof EditorItemArea){
                AreaGroup areaGroup = new AreaGroup((EditorItemArea) layer, (AreaGroup.AreaEventHandler) handler);
                stackGroup.addElement(areaGroup, true);            }
            else if(layer instanceof EquationItem){
                layerTabPageController.getLayers().add((EquationItem) layer);
            }
        }
        stackGroup.setCurrentScale(item.getScaleRatio());
    }

    public void loadCalibrationItem(CalibrationImageItem item){
        // Set zoom
        EditorItemZoom zoom = item.getZoom();
        this.scaleValue = zoom.getScale();
        updateScale();
        this.layout();
        this.setHvalue(zoom.gethValue());
        this.setVvalue(zoom.getvValue());
    }

    public Node getTarget() {
        return target;
    }

    public double getScaleValue() {
        return scaleValue;
    }

    public void setCurrentMode(ImageEditorStackGroup.Mode mode){
        this.currentMode = mode;
    }
}
