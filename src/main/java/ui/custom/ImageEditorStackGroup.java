package ui.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import session.model.EditorItemAngle;
import session.model.EditorItemArea;
import session.model.EditorItemSegLine;
import ui.MainApplication;
import ui.custom.angle.AngleGroup;
import ui.custom.angle.AngleInteractor;
import ui.custom.area.AreaGroup;
import ui.custom.area.AreaInteractor;
import ui.custom.base.LineGroup;
import ui.custom.base.PointGroup;
import ui.custom.base.selection.SelectableGroup;
import ui.custom.segline.SegLineGroup;
import ui.custom.segline.SegLineInteractor;
import ui.model.LayerListItem;
import ui.model.ScaleRatio;
import utils.Constants;
import utils.PointUtils;

import static ui.custom.ImageEditorStackGroup.Mode.ANGLE_POINT_SELECT;
import static ui.custom.ImageEditorStackGroup.Mode.ANG_POINT_SELECT;
import static ui.custom.ImageEditorStackGroup.Mode.AREA_VERTICE_SELECT;
import static ui.custom.ImageEditorStackGroup.Mode.LINE_ANG;
import static ui.custom.ImageEditorStackGroup.Mode.LINE_POINT_SELECT;
import static ui.custom.ImageEditorStackGroup.Mode.SELECT;

/**
 * Group responsible for layering measuring tools over the image
 * <p>
 * This group is wrapped by ZoomableScrollPane
 */
public class ImageEditorStackGroup extends Group
    implements SegLineGroup.SegLineChangeEventHandler, AreaGroup.AreaChangeEventHandler,
    AngleGroup.AngleChangeEventHandler {
  public static Color DEFAULT_COLOR = Color.RED;
  private Preferences prefs;

  public enum Mode {PAN, ZOOM, SELECT, LINE_CREATION, LINE_POINT_SELECT, LINE_ANG_SEL, LINE_ANG, ANG_POINT_SELECT, AREA_CREATION, AREA_VERTICE_SELECT, ANGLE_CREATION, ANGLE_POINT_SELECT}

  private ModeListener modeListener;
  private ElementListener elementListener;

  private ArrayList<LayerListItem> elements;

  private int currentSelectedItemIndex = -1;
  private double helperLineAngle = -1;
  private Color currentPickedColor;
  private double addedLineAngle;

  private int lineCount;
  private int areaCount;
  private int angleCount;

  private Mode currentMode;
  private ScaleRatio currentScale;

  private Bounds bounds;

  // Interactors
  private SegLineInteractor segLineInteractor = new SegLineInteractor(this);
  private AreaInteractor areaInteractor = new AreaInteractor(this);
  private AngleInteractor angleInteractor = new AngleInteractor(this);

  private StringProperty status;

  public ImageEditorStackGroup(ModeListener modeListener, ElementListener elementListener,
      Color color, double angle, StringProperty statusProperty) {
    super();
    lineCount = 1;
    areaCount = 1;
    angleCount = 1;
    this.status = statusProperty;
    prefs = Preferences.userNodeForPackage(MainApplication.class);
    addedLineAngle = angle;
    currentPickedColor = color;
    this.modeListener = modeListener;
    this.elementListener = elementListener;
    elements = new ArrayList<>();
    setListeners();
  }

  public void setBounds(Bounds bounds) {
    this.bounds = bounds;
  }

  public void setImage(ImageView image) {
    if (getChildren().size() == 0) {
      getChildren().setAll(image);
    } else {
      getChildren().set(0, image);
    }
  }

  private void setListeners() {
    setOnMousePressed(mousePressedHandler);
    setOnMouseDragged(mouseDraggedHandler);
    setOnMouseMoved(mouseMovedHandler);

    setOnKeyPressed(keyPressedHandler);

    // Block any event that is not CTRL modified or PAN
    //        addEventHandler(MouseEvent.ANY, event -> {
    //            if (!event.isControlDown() && currentMode != Mode.PAN) event.consume();
    //        });
  }

  private EventHandler<KeyEvent> keyPressedHandler = e -> {

    if (e.getCode() == KeyCode.ESCAPE) {
      cancelOrFinishModes();
    }
  };

  private EventHandler<MouseEvent> mouseMovedHandler = e -> {
    if (e.isControlDown() || currentMode == Mode.PAN) return;

    if (currentMode == Mode.AREA_VERTICE_SELECT) {
      ((AreaGroup) elements.get(elements.size() - 1)).moveLastVertex(e.getX(), e.getY());
    } else if (currentMode == LINE_POINT_SELECT) {
      SegLineGroup segLineGroup = ((SegLineGroup) elements.get(elements.size() - 1));
      if (e.isShiftDown()) {
        LineGroup line = segLineGroup.getSubLine(segLineGroup.getLastPointIndex());
        double lineAngle = line.getLineAngle();
        Point2D point = PointUtils.getFinalCorrectedAnglePoint(getBounds(), line.getStartPointX(),
            line.getStartPointY(), e.getX(), e.getY(), lineAngle, 0);
        segLineGroup.moveLastVertex(point.getX(), point.getY());
      } else {
        segLineGroup.moveLastVertex(e.getX(), e.getY());
      }
      status.setValue(segLineGroup.getStatus());
    } else if (currentMode == ANG_POINT_SELECT) {
      SegLineGroup segLineGroup = ((SegLineGroup) elements.get(elements.size() - 1));
      PointGroup pointGroup = segLineGroup.getPoint(0);
      Point2D point =
          PointUtils.getFinalCorrectedAnglePoint(bounds, pointGroup.getX(), pointGroup.getY(),
              e.getX(), e.getY(), helperLineAngle, addedLineAngle);
      segLineGroup.moveLastVertex(point.getX(), point.getY());
    } else if (currentMode == ANGLE_POINT_SELECT) {
      AngleGroup angleGroup = ((AngleGroup) elements.get(elements.size() - 1));
      angleGroup.moveLastPoint(e.getX(), e.getY());
      status.setValue(angleGroup.getStatus());
    }
  };

  private EventHandler<MouseEvent> mousePressedHandler = e -> {
    requestFocus();
    if (e.isControlDown() || currentMode == Mode.PAN) {
      return;
    }

    if (e.getButton() == MouseButton.SECONDARY) {
      if (currentMode == SELECT) {

      } else {
        cancelOrFinishModes();
      }
    } else if (e.getButton() == MouseButton.PRIMARY) {
      if (currentMode == ImageEditorStackGroup.Mode.ZOOM) {
        e.consume();
        VBox vBox = (VBox) ((ImageEditorStackGroup) e.getSource()).getParent()
            .getParent();
        ZoomableScrollPane scrollPane = (ZoomableScrollPane) vBox.getParent()
            .getParent()
            .getParent();
        Point2D point = vBox.sceneToLocal(new Point2D(e.getSceneX(), e.getSceneY()));
        if (e.isPrimaryButtonDown()) {
          scrollPane.onScroll(3, point);
        } else if (e.isSecondaryButtonDown()) {
          scrollPane.onScroll(-3, point);
        }
      }

      if (currentMode == Mode.LINE_POINT_SELECT || currentMode == ANG_POINT_SELECT) {
        e.consume();
        SegLineGroup segLineGroup = ((SegLineGroup) elements.get(elements.size() - 1));
        if (currentMode == ANG_POINT_SELECT) {
          segLineGroup.addPointInPosition();
          segLineGroup.finish();
          currentMode = Mode.LINE_ANG_SEL;
        } else {
          segLineGroup.addPointInPosition();
        }
        if (segLineGroup.hasMinimumPoints()) {
          reportLayerAdd(segLineGroup, false);
        }
      }

      if (currentMode == Mode.LINE_CREATION || currentMode == LINE_ANG) {
        e.consume();
        deselect();
        // Create a new line
        SegLineGroup line =
            new SegLineGroup("Line_" + lineCount++, e.getX(), e.getY(), segLineInteractor, this,
                currentPickedColor, scaleXProperty());
        addInternalElement(line);
        currentSelectedItemIndex = elements.size() - 1;
        if (currentMode == LINE_ANG) {
          currentMode = ANG_POINT_SELECT;
        } else {
          currentMode = LINE_POINT_SELECT;
        }
      }

      if (currentMode == Mode.ANGLE_POINT_SELECT) {
        e.consume();
        AngleGroup angleGroup = ((AngleGroup) elements.get(elements.size() - 1));
        boolean finished = angleGroup.addPoint(e.getX(), e.getY());
        if (finished) {
          reportLayerAdd(angleGroup, false);
          setStatus("");
          currentMode = Mode.ANGLE_CREATION;
        }
      } else if (currentMode == Mode.ANGLE_CREATION) {
        e.consume();
        deselect();
        AngleGroup angle =
            new AngleGroup("Angle_" + angleCount++, e.getX(), e.getY(), angleInteractor, this,
                scaleXProperty());
        addInternalElement(angle);
        currentSelectedItemIndex = elements.size() - 1;
        currentMode = Mode.ANGLE_POINT_SELECT;
      }

      if (currentMode == Mode.AREA_VERTICE_SELECT) {
        e.consume();
        ((AreaGroup) elements.get(elements.size() - 1)).addVertex(e.getX(), e.getY(), true);
      } else if (currentMode == Mode.AREA_CREATION) {
        e.consume();
        deselect();
        currentMode = AREA_VERTICE_SELECT;
        // Create a new line
        AreaGroup areaGroup =
            new AreaGroup("Area_" + areaCount++, e.getX(), e.getY(), areaInteractor, this,
                currentPickedColor, scaleXProperty());
        addInternalElement(areaGroup);
        currentSelectedItemIndex = elements.size() - 1;
      }
    }
  };

  public Bounds getBounds() {
    return bounds;
  }

  /**
   * Handles on mouse dragged event: - Updates either start or end X and Y position of a line.
   */
  private EventHandler<MouseEvent> mouseDraggedHandler = event -> {
    // TODO
    if (event.isControlDown() || currentMode == Mode.PAN || currentSelectedItemIndex == -1) return;
    //LayerListItem item = elements.get(currentSelectedItemIndex);
    //if (item.getType() == LayerListItem.Type.LINE) {
    //    SegLineGroup line = (SegLineGroup) item;
    //    if (currentMode == Mode.LINE && currentSelectedItemIndex != -1) {
    //        line.setEndPoint(getCorrectedPointX(bounds, event.getX()), getCorrectedPointY(bounds, event.getY()));
    //    }
    //    if (currentMode == Mode.ANG && currentSelectedItemIndex != -1) {
    //        event.consume();
    //        double length = getDeltaAngledLineLength(line, event.getX(), event.getY(), helperLineAngle + addedLineAngle, true);
    //        double x = getAngledPointX(helperLineAngle, line.getStartPointX(), length, true);
    //        double y = getAngledPointY(helperLineAngle, line.getStartPointY(), length, true);
    //        line.setEndPoint(getCorrectedPointX(bounds, x), getCorrectedPointY(bounds, y));
    //    }
    //}

  };

  public Color getCurrentPickedColor() {
    return currentPickedColor;
  }

  public void setCurrentPickedColor(Color currentPickedColor) {
    this.currentPickedColor = currentPickedColor;
    prefs.put(Constants.STTGS_COLOR_PICKER, currentPickedColor.toString());
  }

  public double getAddedLineAngle() {
    return addedLineAngle;
  }

  public void setAddedLineAngle(double addedLineAngle) {
    this.addedLineAngle = Math.toRadians(addedLineAngle);
    prefs.putDouble(Constants.STTGS_ANGLE_PICKER, addedLineAngle);
  }

  public void setHelperLineAngle(double helperLineAngle) {
    this.helperLineAngle = helperLineAngle;
  }

  public void addAreaGroup(EditorItemArea layer) {
    AreaGroup areaGroup = new AreaGroup(layer, areaInteractor, this, scaleXProperty());
    addElement(areaGroup, true);
    deselect();
  }

  public void addSegLineGroup(EditorItemSegLine segLine) {
    SegLineGroup lineGroup = new SegLineGroup(segLine, segLineInteractor, this, scaleXProperty());
    addElement(lineGroup, true);
    deselect();
  }

  public void addAngle(EditorItemAngle angle) {
    AngleGroup angleGroup = new AngleGroup(angle, angleInteractor, this, scaleXProperty());
    addElement(angleGroup, true);
    deselect();
  }

  public void addElement(LayerListItem item, boolean editorItemLoad) {
    addInternalElement(item);
    reportLayerAdd(item, editorItemLoad);
  }

  private void addInternalElement(LayerListItem item) {
    if (item.isVisualElement()) getChildren().add((Node) item);
    elements.add(item);
  }

  private void reportLayerAdd(LayerListItem item, boolean editorItemLoad) {
    if (elementListener != null) {
      if (item.getType() == LayerListItem.Type.LINE) {
        elementListener.onLineAdd((SegLineGroup) item, editorItemLoad);
      } else if (item.getType() == LayerListItem.Type.ANGLE) {
        elementListener.onAngleAdd((AngleGroup) item, editorItemLoad);
      } else if (item.getType() == LayerListItem.Type.AREA && editorItemLoad) {
        elementListener.onAreaAdd((AreaGroup) item, editorItemLoad);
      }
    }
  }

  public void addAreaGroup(AreaGroup areaGroup, boolean editorItemLoad) {
    getChildren().add(areaGroup);
    elements.add(areaGroup);
    if (elementListener != null) elementListener.onAreaAdd(areaGroup, editorItemLoad);
  }

  public ScaleRatio getCurrentScale() {
    return currentScale;
  }

  public void setCurrentScale(ScaleRatio currentScale) {
    this.currentScale = currentScale;
  }

  /**
   * Removes a layer (line) from the ImageEditorStackGroup
   */
  public void removeLayerGroup(String name) {
    for (int i = 0; i < elements.size(); i++) {
      LayerListItem item = elements.get(i);
      if (item.getPrimaryText()
          .equals(name)) {
        elements.remove(i);
        getChildren().remove(i + 1); // + 1 because the first layer is the base image
      }
    }
  }

  public void renameLineGroup(String oldName, String name) {
    for (int i = 0; i < elements.size(); i++) {
      LayerListItem item = elements.get(i);
      if (item.getPrimaryText()
          .equals(oldName)) {
        elements.get(i)
            .setPrimaryText(name);
      }
    }
  }

  public void clearList() {
    cancelOrFinishModes();
    getChildren().clear();
    elements.clear();
    areaCount = 1;
    lineCount = 1;
    angleCount = 1;
  }

  private void cancelOrFinishModes() {
    if (currentMode == Mode.AREA_VERTICE_SELECT) {
      int index = elements.size() - 1;
      ((AreaGroup) elements.get(index)).cancel();
      elements.remove(index);
      getChildren().remove(index + 1);
      currentMode = Mode.AREA_CREATION;
    }
    if (currentMode == LINE_POINT_SELECT) {
      int index = elements.size() - 1;
      SegLineGroup segLineGroup = ((SegLineGroup) elements.get(index));
      segLineGroup.finish();
      if (segLineGroup.isEmpty()) {
        elements.remove(segLineGroup);
        getChildren().remove(index + 1);
      }
      status.setValue("");
      currentMode = Mode.LINE_CREATION;
    }
    if (currentMode == ANGLE_POINT_SELECT) {
      int index = elements.size() - 1;
      AngleGroup angleGroup = ((AngleGroup) elements.get(index));
      elements.remove(angleGroup);
      getChildren().remove(index + 1);
      status.setValue("");
      currentMode = Mode.ANGLE_CREATION;
    }
  }

  public ArrayList<SegLineGroup> getLines() {
    ArrayList<SegLineGroup> lines = new ArrayList<>();
    for (LayerListItem item : elements) {
      if (item.getType() == LayerListItem.Type.LINE) {
        lines.add((SegLineGroup) item);
      }
    }
    return lines;
  }

  @Override public void onSegLineChange(SegLineGroup segLineGroup) {
    if (elementListener != null) elementListener.onLineChange(segLineGroup);
  }

  @Override public void onAngleChange(AngleGroup angleGroup) {
    if (elementListener != null) elementListener.onAngleChange(angleGroup);
  }

  public void setSelectedLayers(List<LayerListItem> items) {
    for (LayerListItem item : elements) {
      if (items.contains(item)) {
        if (item instanceof SelectableGroup) {
          ((SelectableGroup) item).setSelected(true);
        }
      } else {
        if (item instanceof SelectableGroup) {
          ((SelectableGroup) item).setSelected(false);
        }
      }
    }
  }

  public void setSelectedLayer(LayerListItem layer) {
    for (LayerListItem item : elements) {
      if (item.equals(layer)) {
        if (item instanceof SelectableGroup) {
          ((SelectableGroup) item).setSelected(true);
          if (elementListener != null) elementListener.onLayerSelected(item);
        }
      } else {
        if (item instanceof SelectableGroup) {
          ((SelectableGroup) item).setSelected(false);
        }
      }
    }
  }

  public void deselect() {
    for (LayerListItem item : elements) {
      if (item instanceof SelectableGroup) {
        ((SelectableGroup) item).setSelected(false);
      }
    }
    if (elementListener != null) elementListener.deselect();
  }


  /**
   * Area
   **/
  @Override public void onVertexChange(AreaGroup area, int vertexIndex, double x, double y) {
    //TODO
  }

  @Override public void onAreaChanged(AreaGroup areaGroup) {
    if (elementListener != null) elementListener.onAreaChange(areaGroup);
  }

  @Override public void onAreaComplete(AreaGroup area) {
    currentMode = Mode.AREA_CREATION;
    elementListener.onAreaAdd(area, false);
  }

  public void setCurrentMode(Mode mode) {
    currentSelectedItemIndex = -1;
    this.currentMode = mode;
    status.setValue("");
    if (modeListener != null) modeListener.onModeChange(currentMode);
  }

  public void setStatus(String s) {
    status.setValue(s);
  }

  public Mode getCurrentMode() {
    return currentMode;
  }

  public void setColorHelperLinesVisible(boolean visible) {
    for (LayerListItem item : elements) {
      if (item.getType() == LayerListItem.Type.LINE) {
        ((SegLineGroup) item).setColorHelpersVisible(visible);
      }
    }
  }

  public void setLineCount(int count) {
    this.lineCount = count;
  }

  public interface ModeListener {
    void onModeChange(Mode mode);
  }

  public interface ElementListener {
    void onLayerSelected(LayerListItem item);

    void deselect();

    void onLineAdd(SegLineGroup line, boolean sessionLoad);

    void onAngleAdd(AngleGroup angle, boolean sessionLoad);

    void onAreaAdd(AreaGroup area, boolean sessionLoad);

    void onLineChange(SegLineGroup lineGroup);

    void onAreaChange(AreaGroup areaGroup);

    void onAngleChange(AngleGroup angleGroup);
  }
}
