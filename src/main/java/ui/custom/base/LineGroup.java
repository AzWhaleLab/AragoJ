package ui.custom.base;

import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;

public class LineGroup extends Group {

  private Line helperLine;
  private Line colorLine;

  public LineGroup(double startPointX, double startPointY, double endPointX, double endPointY, double width, Color color){
    helperLine = new Line(startPointX, startPointY, endPointX, endPointY);
    helperLine.setStrokeWidth(0.05f);
    helperLine.setStroke(Color.WHITE);
    helperLine.setBlendMode(BlendMode.DIFFERENCE);
    helperLine.setMouseTransparent(true);

    colorLine = new Line(startPointX, startPointY, endPointX, endPointY);
    colorLine.setStrokeWidth(width);
    colorLine.setStrokeType(StrokeType.CENTERED);
    colorLine.setStrokeLineCap(StrokeLineCap.ROUND);
    colorLine.setStroke(color);

    getChildren().addAll(colorLine, helperLine);
  }

  public void setPrecisionHelpersVisibility(boolean visible){
    helperLine.setOpacity(visible ? 1 : 0);
  }

  public void setColorVisibility(boolean visible){
    colorLine.setOpacity(visible ? 1 : 0);
  }

  public void setEndPoint(double x, double y){
    helperLine.setEndX(x);
    helperLine.setEndY(y);
    colorLine.setEndX(x);
    colorLine.setEndY(y);
  }

  public double getEndPointX(){
    return colorLine.getEndX();
  }

  public double getEndPointY(){
    return colorLine.getEndY();
  }

  public double getStartPointX(){
    return colorLine.getStartX();
  }

  public double getStartPointY(){
    return colorLine.getStartY();
  }

  public void setStartPoint(double x, double y){
    helperLine.setStartX(x);
    helperLine.setStartY(y);
    colorLine.setStartX(x);
    colorLine.setStartY(y);
  }

  public double getAngleWith(LineGroup lineGroup){
    return Math.abs(getDisplayAngleWith(lineGroup));
  }

  public double getDisplayAngleWith(LineGroup lineGroup){
    double vectorX = helperLine.getEndX() - helperLine.getStartX();
    double vectorY = helperLine.getEndY() - helperLine.getStartY();
    double lineVectorX = lineGroup.getEndPointX() - lineGroup.getStartPointX();
    double lineVectorY = lineGroup.getEndPointY() - lineGroup.getStartPointY();
    double dotProduct = (vectorX * lineVectorX) + (vectorY * lineVectorY);
    double detProduct = (vectorX * lineVectorY) - (vectorY * lineVectorX);

    double angle = Math.PI - Math.atan2(detProduct, dotProduct);

    if(angle > Math.PI){
      angle = getPositiveAngleWith(lineGroup) - Math.PI;
    }
    return angle;
  }

  private double getPositiveAngleWith(LineGroup lineGroup){
    double vectorX = helperLine.getEndX() - helperLine.getStartX();
    double vectorY = helperLine.getEndY() - helperLine.getStartY();
    double lineVectorX = lineGroup.getEndPointX() - lineGroup.getStartPointX();
    double lineVectorY = lineGroup.getEndPointY() - lineGroup.getStartPointY();
    double dotProduct = (vectorX * lineVectorX) + (vectorY * lineVectorY);
    double magnitude = Math.sqrt((vectorX * vectorX) + (vectorY * vectorY)) *
        Math.sqrt((lineVectorX * lineVectorX) + (lineVectorY * lineVectorY));

    return Math.acos(dotProduct/magnitude);
  }

  public double getLineAngle() {
    double startX = colorLine.getStartX();
    double startY = colorLine.getStartY();
    double endX = colorLine.getEndX();
    double endY = colorLine.getEndY();

    double x = endX-startX;
    double y = endY-startY;
    double ax = -1.0;
    if(startY < endY) ax = 1.0;
    double ay = 0.0;

    final double delta = (ax * x + ay * y) / Math.sqrt(
        (ax * ax + ay * ay) * (x * x + y * y));

    return Math.acos(delta);
  }

  public double getDisplayAngle() {
    double startX = colorLine.getStartX();
    double startY = colorLine.getStartY();
    double endX = colorLine.getEndX();
    double endY = colorLine.getEndY();

    double x = endX-startX;
    double y = endY-startY;

    return Math.PI - Math.atan2(y,x);
  }

  public void moveLineGroup(double dx, double dy){
    helperLine.setStartX(helperLine.getStartX()+dx);
    helperLine.setStartY(helperLine.getStartY()+dy);
    helperLine.setEndX(helperLine.getEndX()+dx);
    helperLine.setEndY(helperLine.getEndY()+dy);
    colorLine.setStartX(colorLine.getStartX()+dx);
    colorLine.setStartY(colorLine.getStartY()+dy);
    colorLine.setEndX(colorLine.getEndX()+dx);
    colorLine.setEndY(colorLine.getEndY()+dy);
  }

  public void setGroupOpacity(double opacity) {
    colorLine.setOpacity(opacity);
  }

  public double getLength(){
    double dx = colorLine.getEndX()-colorLine.getStartX();
    double dy = colorLine.getEndY()-colorLine.getStartY();
    return Math.sqrt((dx*dx) + (dy*dy));
  }
}
