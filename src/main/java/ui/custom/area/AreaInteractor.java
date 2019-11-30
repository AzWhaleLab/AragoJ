package ui.custom.area;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import ui.custom.ImageEditorStackGroup;
import ui.custom.base.Interactor;

public class AreaInteractor extends Interactor implements AreaGroup.AreaEventHandler {

  public AreaInteractor(ImageEditorStackGroup imageEditorStackGroup) {
    super(imageEditorStackGroup);
  }

  @Override public void onPointDrag(MouseEvent event, AreaGroup areaGroup, int pointIndex) {
    double x = correct(event.getX());
    double y = correct(event.getY());
    if (areaGroup.isSelected()
        && getCurrentMode() == ImageEditorStackGroup.Mode.SELECT
        && event.getButton() == MouseButton.PRIMARY) {
      event.consume();
      areaGroup.setVertexPosition(x, y, pointIndex);
    }
  }

  @Override public void onAreaPressed(MouseEvent event, AreaGroup areaGroup) {
    if (getCurrentMode() == ImageEditorStackGroup.Mode.SELECT
        && event.getButton() == MouseButton.PRIMARY) {
      event.consume();
      setSelected(areaGroup);
    }
  }
}
