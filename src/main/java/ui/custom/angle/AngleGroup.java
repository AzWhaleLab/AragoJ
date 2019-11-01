package ui.custom.angle;

import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import session.model.EditorItemAngle;
import session.model.EditorItemPosition;
import ui.custom.ToolEventHandler;
import ui.custom.base.LineGroup;
import ui.custom.base.PointGroup;
import ui.model.LayerListItem;
import utils.Utility;

import static ui.model.LayerListItem.Type.ANGLE;

public class AngleGroup extends Group implements LayerListItem {
  private final AngleEventHandler eventHandler;
  private final AngleChangeEventHandler eventChangeHandler;

  private String name;

  private LineGroup abLine;
  private LineGroup bcLine;
  private PointGroup aPoint; // 0
  private PointGroup bPoint; // 1
  private PointGroup cPoint; // 2
  private Arc arc;

  private ArrayList<LineGroup> lines;
  private ArrayList<PointGroup> points;

  private double currentAngle;

  public AngleGroup(EditorItemAngle item, AngleEventHandler eventHandler, AngleChangeEventHandler eventChangeHandler){
    this.eventHandler = eventHandler;
    this.eventChangeHandler = eventChangeHandler;
    this.points = new ArrayList<>();
    this.lines = new ArrayList<>();
    this.name = item.getName();
    if(item.getPoints().size() >= 3){
      List<EditorItemPosition> points = item.getPoints();
      addPoint(points.get(0).getX(), points.get(0).getY());
      addPoint(points.get(1).getX(), points.get(1).getY());
      addPoint(points.get(2).getX(), points.get(2).getY());
    }
  }

  public AngleGroup(String name, double startPointX, double startPointY,
      AngleEventHandler eventHandler, AngleChangeEventHandler eventChangeHandler) {
    this.eventHandler = eventHandler;
    this.eventChangeHandler = eventChangeHandler;
    this.points = new ArrayList<>();
    this.lines = new ArrayList<>();
    this.name = name;
    addPoint(startPointX, startPointY);
  }

  public boolean addPoint(double x, double y) {
    if(aPoint == null){
      aPoint = new PointGroup(x, y, 1);
      aPoint.setCircleVisibility(false);
      setListener(aPoint, 0);
      abLine = new LineGroup(x, y, x, y, 0, Color.WHITE);
      abLine.setColorVisibility(false);
      getChildren().addAll(abLine, aPoint);
      return false;
    } else if(bPoint == null){
      bPoint = new PointGroup(x, y, 1);
      bPoint.setCircleVisibility(false);
      setListener(bPoint, 1);
      abLine.setEndPoint(x, y);
      bcLine = new LineGroup(x, y, x, y, 0, Color.WHITE);
      bcLine.setColorVisibility(false);
      arc = new Arc(x, y, 0, 0, abLine.getLineAngle(), getAngle());
      arc.setStroke(Color.WHITE);
      arc.setStrokeWidth(0.05f);
      arc.setBlendMode(BlendMode.DIFFERENCE);
      arc.setFill(Color.TRANSPARENT);
      arc.setType(ArcType.OPEN);
      getChildren().addAll(bcLine, bPoint, arc);
      return false;
    } else if(cPoint == null){
      cPoint = new PointGroup(x, y, 1);
      cPoint.setCircleVisibility(false);
      setListener(cPoint, 2);
      bcLine.setEndPoint(x, y);
      getChildren().addAll(cPoint);
      calculateAngle();
    }
    return true;
  }

  private void calculateArc(){
    if(arc != null && abLine != null && bcLine != null){
      double argRadius = Math.min(abLine.getLength()*0.3, bcLine.getLength()*0.3);
      arc.setCenterX(abLine.getEndPointX());
      arc.setCenterY(abLine.getEndPointY());
      arc.setStartAngle(Math.toDegrees(abLine.getDisplayAngle()));
      double angleEnd = Math.toDegrees(abLine.getDisplayAngleWith(bcLine));
      arc.setLength(angleEnd);
      arc.setRadiusX(argRadius);
      arc.setRadiusY(argRadius);
    }
  }

  public void movePoint(int index, double x, double y){
    if(index == 0){
      aPoint.setPoint(x, y);
      abLine.setStartPoint(x,y);
    } else if(index == 1){
      abLine.setEndPoint(x,y);
      if(bPoint != null){
        bPoint.setPoint(x, y);
        bcLine.setStartPoint(x,y);
      }
    } else if(index == 2){
      if(cPoint != null){
        cPoint.setPoint(x, y);
      }
      bcLine.setEndPoint(x,y);
    }
    calculateAngle();
  }

  public void moveLastPoint(double x, double y){
    if(bPoint == null){
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
  }

  private void calculateAngle() {
    if(bcLine != null){
      currentAngle = abLine.getAngleWith(bcLine);
    } else {
      currentAngle = abLine.getLineAngle();
    }
    calculateArc();
    if(eventChangeHandler != null) eventChangeHandler.onAngleChange(this);
  }

  public double getAngle(){
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
    if(eventChangeHandler != null) eventChangeHandler.onAngleChange(this);
  }

  @Override public String getSecondaryText() {
    return Utility.roundTwoDecimals(Math.toDegrees(getAngle())) + "\u00B0";
  }

  @Override public String getStatus() {
    if(cPoint == null)
      return "angle="+ Utility.roundTwoDecimals(Math.toDegrees(getAngle())) + "\u00B0";
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

  public interface AngleChangeEventHandler extends ToolEventHandler {
    void onAngleChange(AngleGroup angleGroup);
  }

  public interface AngleEventHandler extends ToolEventHandler {
    void onPointDrag(MouseEvent event, AngleGroup angleGroup, int pointIndex);
    void onPointReleased(MouseEvent event, AngleGroup angleGroup, int pointIndex);
  }
}
