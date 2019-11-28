package ui.custom.area;

import javafx.scene.input.MouseEvent;
import ui.custom.ImageEditorStackGroup;
import ui.custom.base.Interactor;

public class AreaInteractor extends Interactor implements AreaGroup.AreaEventHandler {

  public AreaInteractor(ImageEditorStackGroup imageEditorStackGroup) {
    super(imageEditorStackGroup);
  }

  @Override public void onPointDrag(MouseEvent event, AreaGroup areaGroup, int pointIndex) {
    if(getCurrentMode() == ImageEditorStackGroup.Mode.SELECT){
      areaGroup.setVertexPosition(event.getX(), event.getY(), pointIndex);
    }
  }
}
