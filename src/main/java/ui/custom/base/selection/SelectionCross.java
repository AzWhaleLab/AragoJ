package ui.custom.base.selection;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class SelectionCross extends Group implements SelectionGroup {

  private static final double LENGTH = 2;
  private double pointX;
  private double pointY;

  private Line rectHInner;
  private Line rectHOuter;
  private Line rectVInner;
  private Line rectVOuter;

  private double currentScale;

  private Circle selectionCircle;

  public SelectionCross(double x, double y) {
    this(x, y, 0, 0);
  }

  public SelectionCross(double x, double y, double translateX, double translateY) {
    this.pointX = x;
    this.pointY = y;
    this.rectHInner = new Line(pointX - (LENGTH / 2), pointY, pointX + (LENGTH / 2), pointY);
    rectHInner.setStroke(Color.WHITE);
    this.rectHOuter = new Line(pointX - (LENGTH / 2), pointY, pointX + (LENGTH / 2), pointY);
    rectHOuter.setStroke(Color.rgb(0, 0, 0, 0.5));
    this.rectVInner = new Line(pointX, pointY - (LENGTH / 2.0), pointX, pointY + (LENGTH / 2.0));
    rectVInner.setStroke(Color.WHITE);
    this.rectVOuter = new Line(pointX, pointY - (LENGTH / 2.0), pointX, pointY + (LENGTH / 2.0));
    rectVOuter.setStroke(Color.rgb(0, 0, 0, 0.5));

    this.selectionCircle = new Circle(pointX, pointY, LENGTH / 2);
    this.selectionCircle.setOpacity(0);

    getChildren().addAll(rectHOuter, rectVOuter, rectHInner, rectVInner, selectionCircle);
    setTranslateX(translateX);
    setTranslateY(translateY);
  }

  @Override public void setScale(double value) {
    this.currentScale = 1 / value;
    updatePoint();
  }

  private void updatePoint() {
    rectHInner.setStrokeWidth(currentScale);
    rectVInner.setStrokeWidth(currentScale);
    rectHOuter.setStrokeWidth(currentScale * 2);
    rectVOuter.setStrokeWidth(currentScale * 2);

    double scaledLength = Math.min((LENGTH / 2) * currentScale * 20, 10);
    selectionCircle.setRadius(scaledLength);
    selectionCircle.setCenterX(pointX);
    selectionCircle.setCenterY(pointY);
    rectHInner.setStartX(pointX - scaledLength);
    rectHInner.setStartY(pointY);
    rectHInner.setEndX(pointX + scaledLength);
    rectHInner.setEndY(pointY);

    rectHOuter.setStartX(pointX - scaledLength);
    rectHOuter.setStartY(pointY);
    rectHOuter.setEndX(pointX + scaledLength);
    rectHOuter.setEndY(pointY);

    rectVInner.setStartX(pointX);
    rectVInner.setStartY(pointY - scaledLength);
    rectVInner.setEndX(pointX);
    rectVInner.setEndY(pointY + scaledLength);

    rectVOuter.setStartX(pointX);
    rectVOuter.setStartY(pointY - scaledLength);
    rectVOuter.setEndX(pointX);
    rectVOuter.setEndY(pointY + scaledLength);
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

  public void setPoint(double x, double y) {
    this.pointX = x;
    this.pointY = y;
    updatePoint();
  }

  public double getPointX() {
    return pointX;
  }

  public double getPointY() {
    return pointY;
  }
}
