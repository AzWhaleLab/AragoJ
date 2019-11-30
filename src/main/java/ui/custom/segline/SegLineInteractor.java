package ui.custom.segline;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import ui.custom.ImageEditorStackGroup;
import ui.custom.base.Interactor;
import ui.custom.base.LineGroup;
import utils.PointUtils;

import static ui.custom.ImageEditorStackGroup.Mode.LINE_ANG_SEL;

public class SegLineInteractor extends Interactor implements SegLineGroup.SegLineEventHandler {

  public SegLineInteractor(ImageEditorStackGroup imageEditorStackGroup) {
    super(imageEditorStackGroup);
  }

  public void onLineDrag(MouseEvent event, SegLineGroup segLineGroup, int lineIndex, double dx,
      double dy) {
    if (getCurrentMode() == ImageEditorStackGroup.Mode.SELECT) {
      //segLineGroup.move(dx, dy);
    }
  }

  public void onPointDrag(MouseEvent event, SegLineGroup segLineGroup, int pointIndex) {
    if (event.isControlDown() || event.getButton() == MouseButton.SECONDARY) return;

    double x = correct(event.getX());
    double y = correct(event.getY());

    if (segLineGroup.isSelected() && getCurrentMode() == ImageEditorStackGroup.Mode.SELECT) {
      if (event.isShiftDown() && (pointIndex == 0
          || pointIndex == segLineGroup.getLastPointIndex())) {
        if (pointIndex == 0) {
          LineGroup line = segLineGroup.getSubLine(pointIndex);
          double lineAngle = line.getLineAngle();
          Point2D point = PointUtils.getFinalCorrectedAnglePoint(getBounds(), line.getEndPointX(),
              line.getEndPointY(), x, y, lineAngle, 0);
          segLineGroup.setPointPosition(point.getX(), point.getY(), pointIndex);
        } else {
          LineGroup line = segLineGroup.getSubLine(pointIndex - 1);
          double lineAngle = line.getLineAngle();
          Point2D point = PointUtils.getFinalCorrectedAnglePoint(getBounds(), line.getStartPointX(),
              line.getStartPointY(), x, y, lineAngle, 0);
          segLineGroup.setPointPosition(point.getX(), point.getY(), pointIndex);
        }
      } else {
        double correctedX = PointUtils.getCorrectedPointX(getBounds(), x);
        double correctedY = PointUtils.getCorrectedPointY(getBounds(), y);
        segLineGroup.setPointPosition(correctedX, correctedY, pointIndex);
      }
      setStatus(segLineGroup.getStatus(pointIndex));
    }
  }

  @Override public void onLineClicked(MouseEvent event, SegLineGroup segLineGroup, int lineIndex) {
    if (getCurrentMode() == LINE_ANG_SEL && event.getButton() == MouseButton.PRIMARY) {
      event.consume();
      LineGroup line = segLineGroup.getSubLine(lineIndex);
      setHelperLineAngle(line.getLineAngle());
      setCurrentMode(ImageEditorStackGroup.Mode.LINE_ANG);
    }
  }

  @Override public void onSegLineGroupPressed(MouseEvent event, SegLineGroup segLineGroup) {
    if (getCurrentMode() == ImageEditorStackGroup.Mode.SELECT
        && event.getButton() == MouseButton.PRIMARY) {
      event.consume();
      setSelected(segLineGroup);
    }
  }

  @Override
  public void onPointReleased(MouseEvent event, SegLineGroup segLineGroup, int pointIndex) {
    setStatus("");
  }
}
