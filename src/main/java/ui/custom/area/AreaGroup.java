package ui.custom.area;

import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;
import java.awt.geom.Area;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import session.model.EditorItemArea;
import session.model.EditorItemPosition;
import ui.custom.ToolEventHandler;
import ui.custom.base.LineGroup;
import ui.custom.base.PointGroup;
import ui.custom.base.selection.SelectableGroup;
import ui.model.LayerListItem;
import utils.AreaUtils;
import utils.Utility;

public class AreaGroup extends SelectableGroup implements LayerListItem {

  private AreaEventHandler areaEventHandler;
  private AreaChangeEventHandler areaChangeEventHandler;

  private String name;
  private Color color;

  private ArrayList<LineGroup> lines;
  private ArrayList<PointGroup> vertices;

  private boolean isFinished;

  private Polygon polygon;
  private float opacity;

  private double cachedArea;

  public AreaGroup(EditorItemArea area, AreaEventHandler areaEventHandler,
      AreaChangeEventHandler handler, DoubleProperty scale) {
    super(scale);
    this.vertices = new ArrayList<>();
    this.lines = new ArrayList<>();
    this.areaChangeEventHandler = handler;
    this.name = area.getName();
    this.color = Color.valueOf(area.getColor());
    this.areaEventHandler = areaEventHandler;
    setGroupOpacity(0.5f);

    List<EditorItemPosition> verts = area.getVertices();
    for (int i = 0; i < verts.size(); i++) {
      EditorItemPosition pos = verts.get(i);
      addVertex(pos.getX(), pos.getY(), false);
      if (i + 1 == verts.size()) {
        EditorItemPosition initialVert = verts.get(0);
        addVertex(initialVert.getX(), initialVert.getY(), false);
      }
    }
  }

  public AreaGroup(String name, double startPointX, double startPointY,
      AreaEventHandler areaEventHandler, AreaChangeEventHandler handler, Color color,
      DoubleProperty scale) {
    super(scale);
    this.lines = new ArrayList<>();
    this.vertices = new ArrayList<>();
    this.areaChangeEventHandler = handler;
    this.areaEventHandler = areaEventHandler;
    this.name = name;
    this.color = color;
    this.isFinished = false;

    setGroupOpacity(0.5f);
    addVertex(startPointX, startPointY, false);
  }

  private void setGroupOpacity(float opacity) {
    this.opacity = opacity;
    if (polygon != null) {
      polygon.setOpacity(opacity);
    }
  }

  private boolean isCompletable() {
    return !isFinished && vertices.size() > 2;
  }

  private void completeArea(boolean emitCallback) {
    isFinished = true;
    calculateArea();
    if (emitCallback) {
      setSelected(true);
      areaChangeEventHandler.onAreaComplete(this);
    } else {
      setSelected(false);
    }
  }

  public void calculateArea() {
    if (!isFinished) return;

    java.awt.Polygon poly = new java.awt.Polygon();
    for (PointGroup selectionCross : vertices) {
      poly.addPoint((int) selectionCross.getX(), (int) selectionCross.getY());
    }
    Area area = new Area(poly);
    cachedArea = AreaUtils.approxArea(area, 0, 0);

    double[] points = new double[vertices.size() * 2];
    int i = 0;
    for (PointGroup vert : vertices) {
      points[i++] = vert.getX();
      points[i++] = vert.getY();
    }

    boolean hasPolygon = polygon != null;
    polygon = new Polygon(points);
    polygon.setOnMousePressed(event -> {
      if (areaEventHandler != null) areaEventHandler.onAreaPressed(event, this);
    });
    polygon.setFill(color);
    polygon.setOpacity(opacity);
    if (!hasPolygon) {
      getChildren().add(0, polygon);
    } else {
      getChildren().set(0, polygon);
    }

    if (areaChangeEventHandler != null) areaChangeEventHandler.onAreaChanged(this);
  }

  public double getRoundedArea() {
    return Utility.roundTwoDecimals(cachedArea);
  }

  public void addVertex(double x, double y, boolean emitCompleteCallback) {
    if (isFinished) return;
    double actualX = x;
    double actualY = y;
    // If it's completable and the point is near the last point
    if (isCompletable() && vertices.get(0)
        .containsPoint(x, y)) {
      actualX = vertices.get(0)
          .getX();
      actualY = vertices.get(0)
          .getY();
      // Move the previous line end point to the corrected coordinates
      if (lines.size() > 0) {
        LineGroup lastLine = lines.get(lines.size() - 1);
        lastLine.setEndPoint(actualX, actualY);
      }
      completeArea(emitCompleteCallback);
    } else {
      // Add the point
      PointGroup pointGroup = new PointGroup(actualX, actualY, 1);
      pointGroup.setCircleVisibility(false);
      pointGroup.setScale(getScale());
      pointGroup.setOnMouseDragged(event -> {
        areaEventHandler.onPointDrag(event, this, vertices.indexOf(pointGroup));
      });
      getChildren().add(pointGroup);
      vertices.add(pointGroup);
      // Move the previous line end point
      if (lines.size() > 0) {
        LineGroup lastLine = lines.get(lines.size() - 1);
        lastLine.setEndPoint(actualX, actualY);
      }
      // Add a new line for the next point
      LineGroup line = new LineGroup(actualX, actualY, actualX, actualY, 0, Color.WHITE);
      line.setColorVisibility(false);
      line.setScale(getScale());
      getChildren().add(line);
      lines.add(line);
    }
  }

  public void moveLastVertex(double x, double y) {
    if (vertices.size() <= 0 || lines.size() <= 0) return;
    double actualX = x;
    double actualY = y;
    if (!isFinished && isCompletable() && vertices.get(0)
        .containsPoint(x, y)) {
      actualX = vertices.get(0)
          .getX();
      actualY = vertices.get(0)
          .getY();
    }
    setVertexPosition(actualX, actualY, vertices.size());
  }

  public void setVertexPosition(double x, double y, int index) {
    if (vertices.size() == 0 || lines.size() == 0) return;
    if (index == 0) {
      LineGroup previousLine = lines.get(0);
      previousLine.setStartPoint(x, y);
      if (isFinished) {
        LineGroup currentLine = lines.get(lines.size() - 1);
        currentLine.setEndPoint(x, y);
      }
      PointGroup currentVertex = vertices.get(0);
      currentVertex.setPoint(x, y);
    } else {
      LineGroup previousLine = lines.get(index - 1);
      previousLine.setEndPoint(x, y);
      if (index < lines.size()) {
        LineGroup currentLine = lines.get(index);
        currentLine.setStartPoint(x, y);
      }
      if (index < vertices.size()) {
        PointGroup currentVertex = vertices.get(index);
        currentVertex.setPoint(x, y);
      }
    }
    calculateArea();
  }

  public void setName(String name) {
    this.name = name;
    if (areaChangeEventHandler != null) areaChangeEventHandler.onAreaChanged(AreaGroup.this);
  }

  public String getColorString() {
    return color.toString();
  }

  public List<EditorItemPosition> getExportableVertices() {
    ArrayList<EditorItemPosition> verts = new ArrayList<>(vertices.size());
    for (int i = 0; i < vertices.size(); i++) {
      PointGroup selectionCross = vertices.get(i);
      verts.add(new EditorItemPosition(selectionCross.getX(), selectionCross.getY()));
    }
    return verts;
  }

  @Override public SVGGlyph getSVG() throws IOException {
    SVGGlyph glyph = SVGGlyphLoader.loadGlyph(getClass().getClassLoader()
        .getResource("svg/1-area_vector.svg"));
    glyph.setFill(color);
    glyph.setSize(32, 32);
    return glyph;
  }

  @Override public String getPrimaryText() {
    return name;
  }

  @Override public void setPrimaryText(String primaryText) {
    name = primaryText;
  }

  @Override public String getSecondaryText() {
    return getRoundedArea() + " pixels\u00B2";
  }

  @Override public String getStatus() {
    return "";
  }

  @Override public Type getType() {
    return Type.AREA;
  }

  @Override public boolean isVisualElement() {
    return true;
  }

  public void cancel() {
    vertices.clear();
    lines.clear();
    getChildren().clear();
  }

  @Override public void onChangeScale(double value) {
    for (LineGroup line : lines) {
      line.setScale(value);
    }
    for (PointGroup point : vertices) {
      point.setScale(value);
    }
  }

  @Override public void onSelected(boolean selected) {
    for (LineGroup line : lines) {
      line.setSelected(selected);
    }
    for (PointGroup point : vertices) {
      point.setSelected(selected);
    }
  }

  public void setColorHelpersVisible(boolean visible) {
    polygon.setOpacity(visible ? 1 : 0);
  }

  public interface AreaEventHandler extends ToolEventHandler {
    void onPointDrag(MouseEvent event, AreaGroup areaGroup, int pointIndex);

    void onAreaPressed(MouseEvent event, AreaGroup areaGroup);
  }

  public interface AreaChangeEventHandler extends ToolEventHandler {
    void onVertexChange(AreaGroup line, int vertexIndex, double x, double y);

    void onAreaChanged(AreaGroup areaGroup);

    void onAreaComplete(AreaGroup area);
  }
}
