package utils;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import ui.custom.segline.SegLineGroup;

public class PointUtils {

  public static double getCorrectedPointY(Bounds bounds, double y) {
    double endPointY = y;
    if (endPointY < 0) endPointY = 0;
    if (endPointY > bounds.getHeight()) endPointY = bounds.getHeight();
    return endPointY;
  }

  public static double getCorrectedPointX(Bounds bounds, double x) {
    double endPointX = x;
    if (endPointX < 0) endPointX = 0;
    if (endPointX > bounds.getWidth()) endPointX = bounds.getWidth();
    return endPointX;
  }

  public static Point2D getFinalCorrectedAnglePoint(Bounds parentBounds, double linePointX, double linePointY, double mouseX, double mouseY,
      double lineAngle, double baseLineAngle) {
    double length = PointUtils.getDeltaAngledLineLength(linePointX, linePointY, mouseX, mouseY, lineAngle+baseLineAngle);
    double angledPointX =
        PointUtils.getAngledPointX(lineAngle, linePointX, length, baseLineAngle);
    double angledPointY =
        PointUtils.getAngledPointY(lineAngle, linePointY, length, baseLineAngle);
    return PointUtils.getCorrectedAngledPoint(parentBounds,
        new Point2D(angledPointX, angledPointY), lineAngle, linePointX,
        linePointY, baseLineAngle);
  }

  private static Point2D getCorrectedAngledPoint(Bounds bounds, Point2D point, double lineAngle,
      double startX, double startY, double baseLineAngle) {
    double x = point.getX();
    double y = point.getY();
    if (x < 0) {
      double angY =
          getAngledPointY(lineAngle, startY, getLengthFromAngledX(lineAngle, startX, baseLineAngle), baseLineAngle);
      if (isYithinBounds(bounds, angY)) return new Point2D(0, angY);
    }
    if (x > bounds.getWidth()) {
      double angY = getAngledPointY(lineAngle, startY,
          getLengthFromAngledX(lineAngle, startX, bounds.getWidth()), baseLineAngle);
      if (isYithinBounds(bounds, angY)) return new Point2D(bounds.getWidth(), angY);
    }
    if (y < 0) {
      double angX =
          getAngledPointX(lineAngle, startX, getLengthFromAngledY(lineAngle, startY, baseLineAngle), baseLineAngle);
      if (isXWithinBounds(bounds, angX)) return new Point2D(angX, 0);
    }
    if (y > bounds.getHeight()) {
      double angX = getAngledPointX(lineAngle, startX,
          getLengthFromAngledY(lineAngle, startY, bounds.getHeight()), baseLineAngle);
      if (isXWithinBounds(bounds, angX)) return new Point2D(angX, bounds.getHeight());
    }
    return point;
  }

  private static double getDeltaAngledLineLength(double lineX, double lineY, double refX, double refY,
      double angle) {
    double xLength = refX - lineX;
    double yLength = refY - lineY;

    double length = Math.sqrt((xLength * xLength) + (yLength * yLength));

    if(length == 0){
      length = 0.1;
    }

    double degAngle = Math.toDegrees(angle);
    if (degAngle > 360) degAngle -= 360;
    if ((degAngle < 45 || (degAngle >= 315 && degAngle < 360)) && xLength < 0) {
      length = -length;
    } else if (degAngle >= 135 && degAngle < 225 && xLength > 0) {
      length = -length;
    } else if (degAngle >= 45 && degAngle < 135 && yLength < 0) {
      length = -length;
    } else if (degAngle >= 225 && degAngle < 315 && yLength > 0) length = -length;


    return length;
  }

  private static double getAngledPointX(double angle, double startPointX, double length,
      double baseLineAngle) {
    return startPointX + length * Math.cos(angle + baseLineAngle);
  }

  private static double getAngledPointY(double angle, double startPointY, double length,
      double baseLineAngle) {
    return startPointY + length * Math.sin(angle + baseLineAngle);
  }

  private static double getLengthFromAngledX(double angle, double startPointX,
      double angledPointX) {
    return (angledPointX - startPointX) / Math.cos(angle);
  }

  private static double getLengthFromAngledY(double angle, double startPointY,
      double angledPointY) {
    return (angledPointY - startPointY) / Math.sin(angle);
  }

  public static double getCorrectedDx(Bounds bounds, double startPointX, double endPointX,
      double dx) {
    if (startPointX + dx < 0) return -startPointX;
    if (startPointX + dx > bounds.getWidth()) return bounds.getWidth() - startPointX;
    if (endPointX + dx < 0) return -endPointX;
    if (endPointX + dx > bounds.getWidth()) return bounds.getWidth() - endPointX;
    return dx;
  }

  public static double getCorrectedDy(Bounds bounds, double startPointY, double endPointY,
      double dy) {
    if (startPointY + dy < 0) return -startPointY;
    if (startPointY + dy > bounds.getHeight()) return bounds.getHeight() - startPointY;
    if (endPointY + dy < 0) return -endPointY;
    if (endPointY + dy > bounds.getHeight()) return bounds.getHeight() - endPointY;
    return dy;
  }

  private static boolean isXWithinBounds(Bounds bounds, double x) {
    return (x >= 0 && x < bounds.getWidth());
  }

  private static boolean isYithinBounds(Bounds bounds, double y) {
    return (y >= 0 && y < bounds.getHeight());
  }
}
