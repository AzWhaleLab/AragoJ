package ui.custom.area;

import javafx.scene.input.MouseEvent;
import ui.custom.ImageEditorStackGroup;
import ui.custom.base.Interactor;

public class AreaInteractor extends Interactor implements AreaGroup.AreaEventHandler {

  public AreaInteractor(ImageEditorStackGroup imageEditorStackGroup) {
    super(imageEditorStackGroup);
  }

  @Override public void onPointDrag(MouseEvent event, AreaGroup areaGroup, int pointIndex) {
    if(areaGroup.isSelected() && getCurrentMode() == ImageEditorStackGroup.Mode.SELECT){
      event.consume();
      areaGroup.setVertexPosition(event.getX(), event.getY(), pointIndex);
    }
  }

  @Override public void onAreaPressed(MouseEvent event, AreaGroup areaGroup) {
    if(getCurrentMode() == ImageEditorStackGroup.Mode.SELECT){
      event.consume();
      setSelected(areaGroup);
    }
  }
}
