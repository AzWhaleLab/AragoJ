package ui.custom.base;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import ui.custom.base.selection.SelectionLine;

public class LineGroup extends Group {

  private SelectionLine selectionLine;
  private Line colorLine;

  private double width;
  private double opacity;

  public LineGroup(double startPointX, double startPointY, double endPointX, double endPointY,
      double width, Color color) {
    this.width = width;
    selectionLine = new SelectionLine(startPointX, startPointY, endPointX, endPointY);

    colorLine = new Line(startPointX, startPointY, endPointX, endPointY);
    colorLine.setStrokeWidth(width);
    colorLine.setStrokeType(StrokeType.CENTERED);
    colorLine.setStrokeLineCap(StrokeLineCap.ROUND);
    colorLine.setStroke(color);

    getChildren().addAll(colorLine, selectionLine);
  }

  public void setSelected(boolean isSelected) {
    if (isSelected) {
      selectionLine.show();
    } else {
      selectionLine.hide();
    }
  }

  public void setColorVisibility(boolean visible) {
    colorLine.setOpacity(visible ? opacity : 0);
  }

  public void setEndPoint(double x, double y) {
    selectionLine.setEndX(x);
    selectionLine.setEndY(y);
    colorLine.setEndX(x);
    colorLine.setEndY(y);
  }

  public void setScale(double value) {
    selectionLine.setScale(1 / value);
  }

  public double getEndPointX() {
    return colorLine.getEndX();
  }

  public double getEndPointY() {
    return colorLine.getEndY();
  }

  public double getStartPointX() {
    return colorLine.getStartX();
  }

  public double getStartPointY() {
    return colorLine.getStartY();
  }

  public void setStartPoint(double x, double y) {
    selectionLine.setStartX(x);
    selectionLine.setStartY(y);
    colorLine.setStartX(x);
    colorLine.setStartY(y);
  }

  public double getAngleWith(LineGroup lineGroup) {
    return Math.abs(getDisplayAngleWith(lineGroup));
  }

  public double getDisplayAngleWith(LineGroup lineGroup) {
    double vectorX = selectionLine.getEndX() - selectionLine.getStartX();
    double vectorY = selectionLine.getEndY() - selectionLine.getStartY();
    double lineVectorX = lineGroup.getEndPointX() - lineGroup.getStartPointX();
    double lineVectorY = lineGroup.getEndPointY() - lineGroup.getStartPointY();
    double dotProduct = (vectorX * lineVectorX) + (vectorY * lineVectorY);
    double detProduct = (vectorX * lineVectorY) - (vectorY * lineVectorX);

    double angle = Math.PI - Math.atan2(detProduct, dotProduct);

    if (angle > Math.PI) {
      angle = getPositiveAngleWith(lineGroup) - Math.PI;
    }
    return angle;
  }

  private double getPositiveAngleWith(LineGroup lineGroup) {
    double vectorX = selectionLine.getEndX() - selectionLine.getStartX();
    double vectorY = selectionLine.getEndY() - selectionLine.getStartY();
    double lineVectorX = lineGroup.getEndPointX() - lineGroup.getStartPointX();
    double lineVectorY = lineGroup.getEndPointY() - lineGroup.getStartPointY();
    double dotProduct = (vectorX * lineVectorX) + (vectorY * lineVectorY);
    double magnitude = Math.sqrt((vectorX * vectorX) + (vectorY * vectorY)) * Math.sqrt(
        (lineVectorX * lineVectorX) + (lineVectorY * lineVectorY));

    return Math.acos(dotProduct / magnitude);
  }

  public double getLineAngle() {
    double startX = colorLine.getStartX();
    double startY = colorLine.getStartY();
    double endX = colorLine.getEndX();
    double endY = colorLine.getEndY();

    double x = endX - startX;
    double y = endY - startY;
    double ax = -1.0;
    if (startY < endY) ax = 1.0;
    double ay = 0.0;

    final double delta = (ax * x + ay * y) / Math.sqrt((ax * ax + ay * ay) * (x * x + y * y));

    return Math.acos(delta);
  }

  public double getDisplayAngle() {
    double startX = colorLine.getStartX();
    double startY = colorLine.getStartY();
    double endX = colorLine.getEndX();
    double endY = colorLine.getEndY();

    double x = endX - startX;
    double y = endY - startY;

    return Math.PI - Math.atan2(y, x);
  }

  public void moveLineGroup(double dx, double dy) {
    selectionLine.setStartX(selectionLine.getStartX() + dx);
    selectionLine.setStartY(selectionLine.getStartY() + dy);
    selectionLine.setEndX(selectionLine.getEndX() + dx);
    selectionLine.setEndY(selectionLine.getEndY() + dy);
    colorLine.setStartX(colorLine.getStartX() + dx);
    colorLine.setStartY(colorLine.getStartY() + dy);
    colorLine.setEndX(colorLine.getEndX() + dx);
    colorLine.setEndY(colorLine.getEndY() + dy);
  }

  public void setGroupOpacity(double opacity) {
    this.opacity = opacity;
    colorLine.setOpacity(opacity);
  }

  public double getLength() {
    double dx = colorLine.getEndX() - colorLine.getStartX();
    double dy = colorLine.getEndY() - colorLine.getStartY();
    return Math.sqrt((dx * dx) + (dy * dy));
  }
}
