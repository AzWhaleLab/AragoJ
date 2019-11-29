package ui.custom.segline;

import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import session.model.EditorItemPosition;
import session.model.EditorItemSegLine;
import ui.custom.ToolEventHandler;
import ui.custom.base.LineGroup;
import ui.custom.base.PointGroup;
import ui.custom.base.selection.SelectableGroup;
import ui.model.LayerListItem;
import utils.Utility;

public class SegLineGroup extends SelectableGroup implements LayerListItem {
  private static final double WIDTH = 3;

  private String name;

  private ArrayList<LineGroup> lines;
  private ArrayList<PointGroup> points;

  private SegLineChangeEventHandler changeEventHandler;
  private SegLineEventHandler eventHandler;

  private Color color;
  private double opacity = 0.5f;

  private double currentX;
  private double currentY;

  private double currentLength = 0;

  public SegLineGroup(EditorItemSegLine segLine, SegLineEventHandler eventHandler,
      SegLineChangeEventHandler eventChangeHandler, DoubleProperty scalePropery) {
    super(scalePropery);
    this.points = new ArrayList<>();
    this.lines = new ArrayList<>();
    this.changeEventHandler = eventChangeHandler;
    this.eventHandler = eventHandler;
    this.name = segLine.getName();
    this.color = Color.valueOf(segLine.getColor());

    List<EditorItemPosition> verts = segLine.getPoints();
    for (EditorItemPosition pos : verts) {
      addPoint(pos.getX(), pos.getY());
    }
    finish();
    setOnMousePressed(event -> {
      if(eventHandler != null) eventHandler.onSegLineGroupPressed(event, this);
    });
  }

  public SegLineGroup(String name, double startPointX, double startPointY,
      SegLineEventHandler eventHandler, SegLineChangeEventHandler eventChangeHandler, Color color,
      DoubleProperty scalePropery) {
    super(scalePropery);
    this.changeEventHandler = eventChangeHandler;
    this.eventHandler = eventHandler;
    this.lines = new ArrayList<>();
    this.points = new ArrayList<>();
    this.name = name;
    this.color = color;

    addPoint(startPointX, startPointY);
    setOnMousePressed(event -> {
      if(eventHandler != null) eventHandler.onSegLineGroupPressed(event, this);
    });
  }

  public void moveLastVertex(double x, double y) {
    if (points.size() <= 0 || lines.size() <= 0) return;
    setPointPosition(x, y, points.size());
  }

  public void move(double dx, double dy) {
    lines.forEach((lineGroup -> lineGroup.moveLineGroup(dx, dy)));
    points.forEach((pointGroup -> pointGroup.movePoint(dx, dy)));
    if (changeEventHandler != null) changeEventHandler.onSegLineChange(this);
  }

  public double getLength() {
    return currentLength;
  }

  private void calculateLength() {
    double totalLength = 0;
    for (LineGroup lineGroup : lines) {
      totalLength += lineGroup.getLength();
    }
    currentLength = totalLength;
  }

  void setPointPosition(double x, double y, int index) {
    if (index == 0) {
      LineGroup previousLine = lines.get(0);
      previousLine.setStartPoint(x, y);
    } else if (index > 0) {
      LineGroup previousLine = lines.get(index - 1);
      previousLine.setEndPoint(x, y);
      if (index < lines.size()) {
        LineGroup currentLine = lines.get(index);
        currentLine.setStartPoint(x, y);
      }
    }
    if (index < points.size()) {
      PointGroup currentPoint = points.get(index);
      currentPoint.setPoint(x, y);
    }
    calculateLength();
    if (changeEventHandler != null) changeEventHandler.onSegLineChange(this);
  }

  public void setName(String name) {
    this.name = name;
    if (changeEventHandler != null) changeEventHandler.onSegLineChange(this);
  }

  private void addPoint(double x, double y) {
    PointGroup pointGroup = new PointGroup(x, y, WIDTH / 2);
    pointGroup.setScale(getScale());
    pointGroup.setGroupOpacity(opacity);
    pointGroup.setOnMouseDragged(event -> {
      eventHandler.onPointDrag(event, this, points.indexOf(pointGroup));
    });
    pointGroup.setOnMouseReleased(event -> {
      eventHandler.onPointReleased(event, this, points.indexOf(pointGroup));
    });
    points.add(pointGroup);
    if (lines.size() > 0) {
      LineGroup lastLine = lines.get(lines.size() - 1);
      lastLine.setEndPoint(x, y);
    }
    LineGroup lineGroup = new LineGroup(x, y, x, y, WIDTH, color);
    lineGroup.setScale(getScale());
    lineGroup.setGroupOpacity(opacity);
    lineGroup.setOnMousePressed(event -> {
      eventHandler.onLineClicked(event, this, lines.indexOf(lineGroup));
      currentX = event.getX();
      currentY = event.getY();
    });
    lineGroup.setOnMouseDragged(event -> {
      eventHandler.onLineDrag(event, this, lines.indexOf(lineGroup), event.getX() - currentX,
          event.getY() - currentY);
      currentX = event.getX();
      currentY = event.getY();
    });
    lines.add(lineGroup);
    getChildren().addAll(lineGroup, pointGroup);
    if (changeEventHandler != null) changeEventHandler.onSegLineChange(this);
    calculateLength();
  }

  private void setGroupOpacity(double opacity) {
    this.opacity = opacity;
    lines.forEach((lineGroup -> lineGroup.setGroupOpacity(opacity)));
    points.forEach((pointGroup -> pointGroup.setGroupOpacity(opacity)));
    if (changeEventHandler != null) changeEventHandler.onSegLineChange(this);
  }

  public String getColorString() {
    return color.toString();
  }

  public LineGroup getSubLine(int index) {
    return lines.get(index);
  }

  public PointGroup getPoint(int index) {
    return points.get(index);
  }

  public int getLastPointIndex() {
    return points.size() - 1;
  }

  public List<EditorItemPosition> getExportablePoints() {
    ArrayList<EditorItemPosition> verts = new ArrayList<>(points.size());
    for (int i = 0; i < points.size(); i++) {
      PointGroup pointGroup = points.get(i);
      verts.add(new EditorItemPosition(pointGroup.getX(), pointGroup.getY()));
    }
    return verts;
  }

  @Override public SVGGlyph getSVG() throws IOException {
    SVGGlyph glyph = SVGGlyphLoader.loadGlyph(getClass().getClassLoader()
        .getResource("svg/1-layer_vector.svg"));
    glyph.setFill(color);
    glyph.setSize(32, 32);
    return glyph;
  }

  @Override public String getPrimaryText() {
    return name;
  }

  @Override public void setPrimaryText(String primaryText) {
    setName(primaryText);
  }

  @Override public String getSecondaryText() {
    return Utility.roundTwoDecimals(getLength()) + " pixels";
  }

  @Override public String getStatus() {
    return getStatus(points.size() - 1);
  }

  public String getStatus(int pointIndex) {
    StringBuilder status = new StringBuilder();
    if (pointIndex >= 0 && pointIndex < points.size()) {
      if (pointIndex == 0 || (pointIndex == 1 && lines.size() == 1)) {
        double angle = lines.get(0)
            .getLineAngle();
        if (lines.size() > 1) {
          angle = lines.get(0)
              .getAngleWith(lines.get(1));
        }
        status.append("length=")
            .append(Utility.roundTwoDecimals(getSubLine(0).getLength()))
            .append("px, ");
        status.append("angle=")
            .append((Utility.roundTwoDecimals(Math.toDegrees(angle))))
            .append("\u00B0");
      } else if (pointIndex == points.size() - 1) {
        status.append("length=")
            .append(Utility.roundTwoDecimals((getSubLine(lines.size() - 1).getLength())))
            .append("px, ");
        status.append("angle=")
            .append(Utility.roundTwoDecimals((Math.toDegrees(lines.get(lines.size() - 1)
                .getAngleWith(lines.get(lines.size() - 2))))))
            .append("\u00B0");
      } else {
        LineGroup firstLine = lines.get(pointIndex - 1);
        LineGroup secondLine = lines.get(pointIndex);
        status.append("length1=")
            .append(Utility.roundTwoDecimals(getSubLine(pointIndex - 1).getLength()))
            .append("px, ");
        status.append("length2=")
            .append(Utility.roundTwoDecimals(getSubLine(pointIndex).getLength()))
            .append("px, ");
        status.append("angle=")
            .append((Utility.roundTwoDecimals(Math.toDegrees(firstLine.getAngleWith(secondLine)))))
            .append("\u00B0");
      }
    }
    return status.toString();
  }

  @Override public Type getType() {
    return Type.LINE;
  }

  @Override public boolean isVisualElement() {
    return true;
  }

  public String getName() {
    return name;
  }

  @Override public void onChangeScale(double value) {
    for (LineGroup line : lines) {
      line.setScale(value);
    }
    for (PointGroup pointGroup : points) {
      pointGroup.setScale(value);
    }
  }

  @Override public void onSelected(boolean selected) {
    lines.forEach((lineGroup -> lineGroup.setSelected(selected)));
    points.forEach((pointGroup -> pointGroup.setSelected(selected)));
  }

  public void setColorHelpersVisible(boolean visible) {
    lines.forEach((lineGroup -> lineGroup.setColorVisibility(visible)));
    points.forEach((pointGroup -> pointGroup.setCircleVisibility(visible)));
  }

  public void finish() {
    getChildren().remove(lines.remove(lines.size() - 1));
    if (lines.size() == 0) {
      getChildren().clear();
      points.clear();
    } else {
      calculateLength();
      setSelected(true);
    }
    if (changeEventHandler != null) changeEventHandler.onSegLineChange(this);
  }

  public boolean isEmpty() {
    return points.size() <= 0;
  }

  public boolean hasMinimumPoints() {
    return points.size() == 2;
  }

  public void addPointInPosition() {
    if (lines.size() > 0) {
      LineGroup lineGroup = lines.get(lines.size() - 1);
      addPoint(lineGroup.getEndPointX(), lineGroup.getEndPointY());
    }
  }

  public interface SegLineChangeEventHandler extends ToolEventHandler {
    void onSegLineChange(SegLineGroup segLineGroup);
  }

  public interface SegLineEventHandler extends ToolEventHandler {
    void onLineDrag(MouseEvent event, SegLineGroup segLineGroup, int lineIndex, double dx,
        double dy);

    void onPointDrag(MouseEvent event, SegLineGroup segLineGroup, int pointIndex);

    void onLineClicked(MouseEvent event, SegLineGroup segLineGroup, int lineIndex);

    void onSegLineGroupPressed(MouseEvent event, SegLineGroup segLineGroup);

    void onPointReleased(MouseEvent event, SegLineGroup segLineGroup, int pointIndex);
  }
}
