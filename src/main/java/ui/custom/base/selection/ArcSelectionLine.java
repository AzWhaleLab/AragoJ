package ui.custom.base.selection;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

public class ArcSelectionLine extends Group implements SelectionGroup {

  private Arc innerArc;
  private Arc outerArc;

  public ArcSelectionLine(double x, double y, double radiusX, double radiusY, double startAngle,
      double length) {

    innerArc = new Arc(x, y, radiusX, radiusY, startAngle, length);
    innerArc.setStroke(Color.WHITE);
    innerArc.setFill(Color.TRANSPARENT);
    innerArc.setType(ArcType.OPEN);
    outerArc = new Arc(x, y, radiusX, radiusY, startAngle, length);
    outerArc.setStroke(Color.rgb(0, 0, 0, 0.5));
    outerArc.setFill(Color.TRANSPARENT);
    outerArc.setType(ArcType.OPEN);

    getChildren().addAll(outerArc, innerArc);
  }

  public void setArc(double centerX, double centerY, double radiusX, double radiusY,
      double startAngle, double length) {
    innerArc.setCenterX(centerX);
    innerArc.setCenterY(centerY);
    innerArc.setRadiusX(radiusX);
    innerArc.setRadiusY(radiusY);
    innerArc.setStartAngle(startAngle);
    innerArc.setLength(length);

    outerArc.setCenterX(centerX);
    outerArc.setCenterY(centerY);
    outerArc.setRadiusX(radiusX);
    outerArc.setRadiusY(radiusY);
    outerArc.setStartAngle(startAngle);
    outerArc.setLength(length);
  }

  @Override public void setScale(double scale) {
    double s = 1 / scale;
    innerArc.setStrokeWidth(s);
    outerArc.setStrokeWidth(s * 2);
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
