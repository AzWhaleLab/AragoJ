package ui.custom.angle;

import javafx.scene.input.MouseEvent;
import ui.custom.ImageEditorStackGroup;
import ui.custom.base.Interactor;

public class AngleInteractor extends Interactor implements AngleGroup.AngleEventHandler {

  public AngleInteractor(ImageEditorStackGroup imageEditorStackGroup) {
    super(imageEditorStackGroup);
  }

  @Override public void onPointDrag(MouseEvent event, AngleGroup angleGroup, int pointIndex) {
    if(angleGroup.isSelected() && getCurrentMode() == ImageEditorStackGroup.Mode.SELECT){
      event.consume();
      angleGroup.movePoint(pointIndex, event.getX(), event.getY());
      setStatus(angleGroup.getStatus());
    }
  }

  @Override public void onMousePressed(MouseEvent event, AngleGroup angleGroup, int pointIndex) {
    if(getCurrentMode() == ImageEditorStackGroup.Mode.SELECT){
      event.consume();
      setSelected(angleGroup);
    }
  }

  @Override public void onPointReleased(MouseEvent event, AngleGroup angleGroup, int pointIndex) {
    setStatus("");
  }
}
