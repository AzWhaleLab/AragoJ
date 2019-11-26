package ui.custom.base.selection;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class SelectionLine extends Group implements SelectionGroup {

  private Line innerLine;
  private Line outerLine;

  /**
   * Here we're just setting two lines to replicate a stroke. This is not ideal but it's ok for
   * now.
   */
  public SelectionLine(double startPointX, double startPointY, double endPointX, double endPointY) {
    innerLine = new Line(startPointX, startPointY, endPointX, endPointY);
    innerLine.setStroke(Color.WHITE);
    outerLine = new Line(startPointX, startPointY, endPointX, endPointY);
    outerLine.setStroke(Color.rgb(0, 0, 0, 0.5));

    getChildren().addAll(outerLine, innerLine);

    setMouseTransparent(true);
  }

  public void setScale(double scale) {
    innerLine.setStrokeWidth(scale);
    outerLine.setStrokeWidth(scale * 2);
  }

  public void setStartX(double startX) {
    innerLine.setStartX(startX);
    outerLine.setStartX(startX);
  }

  public void setStartY(double startY) {
    innerLine.setStartY(startY);
    outerLine.setStartY(startY);
  }

  public void setEndX(double endX) {
    innerLine.setEndX(endX);
    outerLine.setEndX(endX);
  }

  public void setEndY(double endY) {
    innerLine.setEndY(endY);
    outerLine.setEndY(endY);
  }

  public double getStartX() {
    return innerLine.getStartX();
  }

  public double getStartY() {
    return innerLine.getStartY();
  }

  public double getEndX() {
    return innerLine.getEndX();
  }

  public double getEndY() {
    return innerLine.getEndY();
  }

  @Override public void show() {
    setOpacity(1);
    setMouseTransparent(false);
  }

  @Override public void hide() {
    setOpacity(0);
    setMouseTransparent(true);
  }
}
