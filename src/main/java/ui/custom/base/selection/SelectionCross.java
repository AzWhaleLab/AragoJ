package ui.custom.base.selection;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;

public class SelectionCross extends Group implements SelectionGroup {

  private static final Color DEFAULT_COLOR = Color.WHITE;
  private static final double THICKNESS = 0.05;
  private static final double LENGTH = 2;
  private double pointX;
  private double pointY;

  private Rectangle rectH;
  private Rectangle rectV;

  private Circle selectionCircle;
  private Shape crossShape;

  public SelectionCross(double x, double y) {
    this(x, y, 0, 0);
  }

  public SelectionCross(double x, double y, double translateX, double translateY) {
    this.pointX = x;
    this.pointY = y;
    this.rectH =
        new Rectangle(pointX - (LENGTH / 2), pointY - (THICKNESS / 2.0), LENGTH, THICKNESS);
    this.rectV =
        new Rectangle(pointX - (THICKNESS / 2.0), pointY - (LENGTH / 2), THICKNESS, LENGTH);
    this.crossShape = Shape.union(rectH, rectV);
    crossShape.setFill(DEFAULT_COLOR);
    crossShape.setStroke(Color.BLACK);
    crossShape.setStrokeType(StrokeType.OUTSIDE);
    crossShape.setStrokeWidth(0.01);

    this.selectionCircle = new Circle(pointX, pointY, LENGTH / 2);
    this.selectionCircle.setOpacity(0);

    getChildren().addAll(crossShape, selectionCircle);
    setTranslateX(translateX);
    setTranslateX(translateY);
  }

  @Override public void setScale(double value) {
    double scale = 1 / value;
    setScaleX(Math.min(scale * 30, 60));
    setScaleY(Math.min(scale * 30, 60));
  }

  @Override public void show() {
    setOpacity(1);
    setMouseTransparent(false);
  }

  @Override public void hide() {
    setOpacity(0);
    setMouseTransparent(true);
  }

  /**
   * This is used instead of the Node's contain because it doesn't seem to take scaling into
   * consideration.
   */
  public boolean containsPoint(double x, double y) {
    return Math.pow(x - selectionCircle.getCenterX(), 2) + Math.pow(
        y - selectionCircle.getCenterY(), 2) < Math.pow(selectionCircle.getRadius() * getScaleX(),
        2);
  }

  public void setColor(Color color) {
    crossShape.setFill(color);
  }

  public void resetColor() {
    setColor(DEFAULT_COLOR);
  }

  public void setPoint(double x, double y) {
    setLayoutX(x - getLayoutBounds().getMinX() - (getLayoutBounds().getWidth() / 2));
    setLayoutY(y - getLayoutBounds().getMinY() - (getLayoutBounds().getHeight() / 2));
    this.pointX = x;
    this.pointY = y;
  }

  public void setPointX(double x) {
    setLayoutX(x - getLayoutBounds().getMinX() - (getLayoutBounds().getWidth() / 2));
    this.pointX = x;
  }

  public void setPointY(double y) {
    setLayoutY(y - getLayoutBounds().getMinY() - (getLayoutBounds().getHeight() / 2));
    this.pointY = y;
  }

  public double getPointX() {
    return pointX;
  }

  public double getPointY() {
    return pointY;
  }
}
