package ui.custom.base;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import ui.custom.Cross;

public class PointGroup extends Group {

  private Circle pointCircle;
  private Cross helperCross;

  private double x;
  private double y;

  public PointGroup(double x, double y, double circleRadius) {
    this.x = x;
    this.y = y;

    helperCross = new Cross(x, y, 0, 0);
    pointCircle = new Circle(x, y, circleRadius);
    pointCircle.setStrokeWidth(0.1f);
    pointCircle.setStrokeType(StrokeType.INSIDE);
    pointCircle.setStroke(Color.LIGHTGRAY);
    pointCircle.setFill(Color.BLACK);

    getChildren().addAll(pointCircle, helperCross.getShape());
  }

  public void setPoint(double x, double y){
    this.x = x;
    this.y = y;
    helperCross.setPoint(x, y);
    pointCircle.setCenterX(x);
    pointCircle.setCenterY(y);
  }

  public void movePoint(double dx, double dy){
    setPoint(x+dx, y+dy);
  }

  public void setPrecisionHelpersVisibility(boolean visible){
    helperCross.setOpacity(visible ? 1 : 0);
  }

  public void setCircleVisibility(boolean visible){
    pointCircle.setOpacity(visible ? 1 : 0);
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public void setGroupOpacity(double opacity) {
    pointCircle.setOpacity(opacity);
  }
}
