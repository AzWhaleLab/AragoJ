package ui.components;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.skins.JFXSliderSkin;
import javafx.scene.control.Skin;

public class AragoSlider extends JFXSlider {

  @Override protected Skin<?> createDefaultSkin() {
    return new AragoSliderSkin(this);
  }
}
