package ui.custom.base.selection;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;

public abstract class SelectableGroup extends Group {

  private DoubleProperty scalePropery;

  private boolean isSelected;
  private ChangeListener<Number> scaleListener =
      (observable, oldValue, newValue) -> onChangeScale(newValue.doubleValue());

  public SelectableGroup(DoubleProperty scalePropery) {
    this.scalePropery = scalePropery;
    this.isSelected = false;
    scalePropery.addListener(scaleListener);
  }

  public abstract void onChangeScale(double value);

  protected abstract void onSelected(boolean selected);

  public final void setSelected(boolean selected){
    this.isSelected = selected;
    onSelected(selected);
  }

  public double getScale() {
    return scalePropery.get();
  }

  public boolean isSelected(){
    return isSelected;
  }

  public void setScalePropery(DoubleProperty scalePropery) {
    this.scalePropery = scalePropery;
  }
}
