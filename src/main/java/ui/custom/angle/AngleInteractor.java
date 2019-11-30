package ui.custom.angle;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import ui.custom.ImageEditorStackGroup;
import ui.custom.base.Interactor;

public class AngleInteractor extends Interactor implements AngleGroup.AngleEventHandler {

  public AngleInteractor(ImageEditorStackGroup imageEditorStackGroup) {
    super(imageEditorStackGroup);
  }

  @Override public void onPointDrag(MouseEvent event, AngleGroup angleGroup, int pointIndex) {
    double x = correct(event.getX());
    double y = correct(event.getY());
    if (angleGroup.isSelected()
        && getCurrentMode() == ImageEditorStackGroup.Mode.SELECT
        && event.getButton() == MouseButton.PRIMARY) {
      event.consume();
      angleGroup.movePoint(pointIndex, x, y);
      setStatus(angleGroup.getStatus());
    }
  }

  @Override public void onMousePressed(MouseEvent event, AngleGroup angleGroup, int pointIndex) {
    if (getCurrentMode() == ImageEditorStackGroup.Mode.SELECT
        && event.getButton() == MouseButton.PRIMARY) {
      event.consume();
      setSelected(angleGroup);
    }
  }

  @Override public void onPointReleased(MouseEvent event, AngleGroup angleGroup, int pointIndex) {
    setStatus("");
  }
}
