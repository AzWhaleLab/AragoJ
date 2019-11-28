package ui.custom.base;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import ui.custom.base.selection.SelectionCross;

public class PointGroup extends Group {

  private Circle pointCircle;
  private SelectionCross selectionCross;

  private double x;
  private double y;

  private double opacity;

  public PointGroup(double x, double y, double circleRadius) {
    this.x = x;
    this.y = y;

    selectionCross = new SelectionCross(x, y, 0, 0);
    pointCircle = new Circle(x, y, circleRadius);
    pointCircle.setStrokeWidth(0.1f);
    pointCircle.setStrokeType(StrokeType.INSIDE);
    pointCircle.setStroke(Color.LIGHTGRAY);
    pointCircle.setFill(Color.BLACK);

    getChildren().addAll(pointCircle, selectionCross);
  }

  public void setPoint(double x, double y) {
    this.x = x;
    this.y = y;
    selectionCross.setPoint(x, y);
    pointCircle.setCenterX(x);
    pointCircle.setCenterY(y);
  }

  public void movePoint(double dx, double dy) {
    setPoint(x + dx, y + dy);
  }

  public void setSelected(boolean isSelected) {
    if (isSelected) {
      selectionCross.show();
    } else {
      selectionCross.hide();
    }
  }

  public void setCircleVisibility(boolean visible) {
    pointCircle.setOpacity(visible ? opacity : 0);
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public void setGroupOpacity(double opacity) {
    this.opacity = opacity;
    pointCircle.setOpacity(opacity);
  }

  public void setScale(double value) {
    selectionCross.setScale(value);
  }

  public boolean containsPoint(double x, double y) {
    return selectionCross.containsPoint(x, y);
  }
}
