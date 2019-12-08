package ui.custom.angle;

import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import session.model.EditorItemAngle;
import session.model.EditorItemPosition;
import ui.custom.ToolEventHandler;
import ui.custom.base.LineGroup;
import ui.custom.base.PointGroup;
import ui.custom.base.selection.ArcSelectionLine;
import ui.custom.base.selection.SelectableGroup;
import ui.model.LayerListItem;
import ui.model.ScaleRatio;
import utils.Utility;

import static ui.model.LayerListItem.Type.ANGLE;

public class AngleGroup extends SelectableGroup implements LayerListItem {
  private final AngleEventHandler eventHandler;
  private final AngleChangeEventHandler eventChangeHandler;

  private String name;

  private LineGroup abLine;
  private LineGroup bcLine;
  private PointGroup aPoint;
  private PointGroup bPoint;
  private PointGroup cPoint;
  private ArcSelectionLine angleLine;

  private double currentAngle;

  public AngleGroup(EditorItemAngle item, AngleEventHandler eventHandler,
      AngleChangeEventHandler eventChangeHandler, DoubleProperty scaleProperty) {
    super(scaleProperty);
    this.eventHandler = eventHandler;
    this.eventChangeHandler = eventChangeHandler;
    this.name = item.getName();
    if (item.getPoints()
        .size() >= 3) {
      List<EditorItemPosition> points = item.getPoints();
      addPoint(points.get(0)
          .getX(), points.get(0)
          .getY());
      addPoint(points.get(1)
          .getX(), points.get(1)
          .getY());
      addPoint(points.get(2)
          .getX(), points.get(2)
          .getY());
    }
  }

  public AngleGroup(String name, double startPointX, double startPointY,
      AngleEventHandler eventHandler, AngleChangeEventHandler eventChangeHandler,
      DoubleProperty scaleProperty) {
    super(scaleProperty);
    this.eventHandler = eventHandler;
    this.eventChangeHandler = eventChangeHandler;
    this.name = name;
    addPoint(startPointX, startPointY);
  }

  public boolean addPoint(double x, double y) {
    if (aPoint == null) {
      aPoint = new PointGroup(x, y, 1);
      aPoint.setCircleVisibility(false);
      aPoint.setScale(getScale());
      setListener(aPoint, 0);
      abLine = new LineGroup(x, y, x, y, 0, Color.WHITE);
      abLine.setScale(getScale());
      abLine.setColorVisibility(false);
      getChildren().addAll(abLine, aPoint);
      return false;
    } else if (bPoint == null) {
      bPoint = new PointGroup(x, y, 1);
      bPoint.setCircleVisibility(false);
      bPoint.setScale(getScale());
      setListener(bPoint, 1);
      abLine.setEndPoint(x, y);
      abLine.setScale(getScale());
      bcLine = new LineGroup(x, y, x, y, 0, Color.WHITE);
      bcLine.setColorVisibility(false);
      bcLine.setScale(getScale());

      angleLine = new ArcSelectionLine(x, y, 0, 0, abLine.getLineAngle(), getAngle());
      angleLine.setScale(getScale());
      getChildren().addAll(bcLine, bPoint, angleLine);
      return false;
    } else if (cPoint == null) {
      cPoint = new PointGroup(x, y, 1);
      cPoint.setCircleVisibility(false);
      cPoint.setScale(getScale());
      setListener(cPoint, 2);
      bcLine.setEndPoint(x, y);
      getChildren().addAll(cPoint);
      calculateAngle();
      setSelected(true);
    }
    return true;
  }

  private void calculateArc() {
    if (angleLine != null && abLine != null && bcLine != null) {
      double argRadius = Math.min(abLine.getLength() * 0.3, bcLine.getLength() * 0.3);
      double angleEnd = Math.toDegrees(abLine.getDisplayAngleWith(bcLine));
      angleLine.setArc(abLine.getEndPointX(), abLine.getEndPointY(), argRadius, argRadius,
          Math.toDegrees(abLine.getDisplayAngle()), angleEnd);
    }
  }

  public void movePoint(int index, double x, double y) {
    if (index == 0) {
      aPoint.setPoint(x, y);
      abLine.setStartPoint(x, y);
    } else if (index == 1) {
      abLine.setEndPoint(x, y);
      if (bPoint != null) {
        bPoint.setPoint(x, y);
        bcLine.setStartPoint(x, y);
      }
    } else if (index == 2) {
      if (cPoint != null) {
        cPoint.setPoint(x, y);
      }
      bcLine.setEndPoint(x, y);
    }
    calculateAngle();
  }

  public void moveLastPoint(double x, double y) {
    if (bPoint == null) {
      movePoint(1, x, y);
    } else {
      movePoint(2, x, y);
    }
  }

  private void setListener(PointGroup pointGroup, int i) {
    pointGroup.setOnMouseDragged(event -> {
      eventHandler.onPointDrag(event, this, i);
    });
    pointGroup.setOnMouseReleased(event -> {
      eventHandler.onPointReleased(event, this, i);
    });
    pointGroup.setOnMousePressed(event -> {
      eventHandler.onMousePressed(event, this, i);
    });
  }

  private void calculateAngle() {
    if (bcLine != null) {
      currentAngle = abLine.getAngleWith(bcLine);
    } else {
      currentAngle = abLine.getLineAngle();
    }
    calculateArc();
    if (eventChangeHandler != null) eventChangeHandler.onAngleChange(this);
  }

  public double getAngle() {
    return currentAngle;
  }

  @Override public SVGGlyph getSVG() throws IOException {
    SVGGlyph glyph = SVGGlyphLoader.loadGlyph(getClass().getClassLoader()
        .getResource("svg/1-angle_vector.svg"));
    glyph.setFill(Color.gray(0.2));
    glyph.setSize(32, 25);
    return glyph;
  }

  @Override public String getPrimaryText() {
    return name;
  }

  @Override public void setPrimaryText(String primaryText) {
    name = primaryText;
    if (eventChangeHandler != null) eventChangeHandler.onAngleChange(this);
  }

  @Override public String getSecondaryText() {
    return Utility.roundTwoDecimals(Math.toDegrees(getAngle())) + "\u00B0";
  }

  @Override public String getStatus(ScaleRatio scaleRatio) {
    if (cPoint == null) {
      return "angle=" + Utility.roundTwoDecimals(Math.toDegrees(getAngle())) + "\u00B0";
    }
    return "";
  }

  @Override public Type getType() {
    return ANGLE;
  }

  @Override public boolean isVisualElement() {
    return true;
  }

  public List<EditorItemPosition> getExportablePoints() {
    EditorItemPosition aPointItem = new EditorItemPosition(aPoint.getX(), aPoint.getY());
    EditorItemPosition bPointItem = new EditorItemPosition(bPoint.getX(), bPoint.getY());
    EditorItemPosition cPointItem = new EditorItemPosition(cPoint.getX(), cPoint.getY());
    return Arrays.asList(aPointItem, bPointItem, cPointItem);
  }

  public String getName() {
    return name;
  }

  @Override public void onChangeScale(double value) {
    if (abLine != null) abLine.setScale(value);
    if (bcLine != null) bcLine.setScale(value);
    if (aPoint != null) aPoint.setScale(value);
    if (bPoint != null) bPoint.setScale(value);
    if (cPoint != null) cPoint.setScale(value);
    if (angleLine != null) angleLine.setScale(value);
  }

  @Override public void onSelected(boolean selected) {
    if (abLine != null) abLine.setSelected(selected);
    if (bcLine != null) bcLine.setSelected(selected);
    if (aPoint != null) aPoint.setSelected(selected);
    if (bPoint != null) bPoint.setSelected(selected);
    if (cPoint != null) cPoint.setSelected(selected);
    if (angleLine != null) {
      if (selected) {
        angleLine.show();
      } else {
        angleLine.hide();
      }
    }
  }

  public interface AngleChangeEventHandler extends ToolEventHandler {
    void onAngleChange(AngleGroup angleGroup);
  }

  public interface AngleEventHandler extends ToolEventHandler {
    void onPointDrag(MouseEvent event, AngleGroup angleGroup, int pointIndex);

    void onMousePressed(MouseEvent event, AngleGroup angleGroup, int pointIndex);

    void onPointReleased(MouseEvent event, AngleGroup angleGroup, int pointIndex);
  }
}
