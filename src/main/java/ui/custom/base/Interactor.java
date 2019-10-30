package ui.custom.base;

import javafx.geometry.Bounds;
import ui.custom.ImageEditorStackGroup;

public class Interactor {

  private ImageEditorStackGroup imageEditorStackGroup;

  public Interactor(ImageEditorStackGroup imageEditorStackGroup){
    this.imageEditorStackGroup = imageEditorStackGroup;
  }

  protected ImageEditorStackGroup.Mode getCurrentMode(){
    return imageEditorStackGroup.getCurrentMode();
  }

  protected Bounds getBounds(){
    return imageEditorStackGroup.getBounds();
  }

  protected void setCurrentMode(ImageEditorStackGroup.Mode mode){
    imageEditorStackGroup.setCurrentMode(mode);
  }
  protected void setHelperLineAngle(double addedLineAngle){
    imageEditorStackGroup.setHelperLineAngle(addedLineAngle);
  }

  protected void setStatus(String s){
    imageEditorStackGroup.setStatus(s);
  }
}
