package ui.custom;

import equation.model.EquationItem;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import opencv.calibration.ui.CalibrationImageItem;
import session.model.EditorItem;
import session.model.EditorItemAngle;
import session.model.EditorItemArea;
import session.model.EditorItemLayer;
import session.model.EditorItemSegLine;
import session.model.EditorItemZoom;
import ui.controller.LayerTabPageController;
import ui.custom.area.AreaGroup;

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

  private ZoomChangeListener listener;

  public ZoomableScrollPane(Node target, ZoomChangeListener zoomChangeListener) {
    super();
    this.target = target;
    this.zoomNode = new Group(target);
    this.listener = zoomChangeListener;
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
      if (e.isControlDown()) {
        e.consume();
        onScroll(e.getTextDeltaY(), new Point2D(e.getX(), e.getY()));
      }
    });

    return outerNode;
  }

  private Node centeredNode(Node node) {
    StackPane stackPane = new StackPane(node);
    stackPane.setAlignment(Pos.CENTER);
    return stackPane;
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

    double scale = scaleValue * zoomFactor;
    if (scale > 32) {
      return;
      //scale = 32;
      //zoomFactor = 1;
    }
    scaleValue = scale;
    updateScale();
    this.layout(); // refresh ScrollPane scroll positions & target bounds

    // convert target coordinates to zoomTarget coordinates
    Point2D posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint));

    // calculate adjustment of scroll position (pixels)
    Point2D adjustment = target.getLocalToParentTransform()
        .deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

    // convert back to [0, 1] range
    // (too large/small values are automatically corrected by ScrollPane)
    Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
    this.setHvalue(
        (valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
    this.setVvalue(
        (valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
    if (listener != null) listener.onZoomChange(getHvalue(), getVvalue());
  }

  public void updateZoom() {
    if (listener != null) listener.onZoomChange(getHvalue(), getVvalue());
  }

  public void setDefaultScale() {
    Bounds innerBounds = zoomNode.getLayoutBounds();
    Bounds viewportBounds = getViewportBounds();
    double valX = innerBounds.getWidth() - viewportBounds.getWidth();
    double valY = innerBounds.getHeight() - viewportBounds.getHeight();

    double proposedScale = scaleValue;
    if (valX > 0) {
      proposedScale = (viewportBounds.getWidth() * (3.0 / 5)) / innerBounds.getWidth();
    }

    if (valY > 0) {
      double yProposedScale = (viewportBounds.getHeight() * (3.0 / 5)) / innerBounds.getHeight();
      if (yProposedScale < proposedScale) {
        proposedScale = yProposedScale;
      }
    }

    if (proposedScale < scaleValue) {
      scaleValue = proposedScale;
    }
    updateScale();
    this.layout();
  }

  public void loadEditorItem(EditorItem item, LayerTabPageController layerTabPageController) {
    // Set zoom
    EditorItemZoom zoom = item.getZoom();
    if (zoom != null && zoom.getScale() >= 0) {
      this.scaleValue = zoom.getScale();
      updateScale();
      this.layout();
      this.setHvalue(zoom.gethValue());
      this.setVvalue(zoom.getvValue());
    } else {
      setDefaultScale();
    }

    if (!(target instanceof ImageEditorStackGroup)) return;
    // Set layers (assumes image is already set)
    ImageEditorStackGroup stackGroup = (ImageEditorStackGroup) target;
    stackGroup.setLineCount(item.getLineCount());
    for (EditorItemLayer layer : item.getLayers()) {
      if (layer instanceof EditorItemSegLine) {
        stackGroup.addSegLineGroup((EditorItemSegLine) layer);
      } else if (layer instanceof EditorItemArea) {
        stackGroup.addAreaGroup((EditorItemArea) layer);
      } else if (layer instanceof EditorItemAngle) {
        stackGroup.addAngle((EditorItemAngle) layer);
      } else if (layer instanceof EquationItem) {
        layerTabPageController.getLayers()
            .add((EquationItem) layer);
      }
    }
    if (item.getScaleRatio()
        .getRatio() > 0) {
      stackGroup.setCurrentScale(item.getScaleRatio());
    }
  }

  public void loadCalibrationItem(CalibrationImageItem item) {
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

  public void setCurrentMode(ImageEditorStackGroup.Mode mode) {
    this.currentMode = mode;
  }

  public ImageEditorStackGroup.Mode getCurrentMode() {
    return currentMode;
  }

  public interface ZoomChangeListener {
    void onZoomChange(double hValue, double vValue);
  }
}
