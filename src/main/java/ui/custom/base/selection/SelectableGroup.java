package ui.custom.base.selection;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;

public abstract class SelectableGroup extends Group {

  private DoubleProperty scalePropery;

  private ChangeListener<Number> scaleListener =
      (observable, oldValue, newValue) -> onChangeScale(newValue.doubleValue());

  public SelectableGroup(DoubleProperty scalePropery) {
    this.scalePropery = scalePropery;
    scalePropery.addListener(scaleListener);
  }

  public abstract void onChangeScale(double value);

  public abstract void setSelected(boolean selected);

  public double getScale() {
    return scalePropery.get();
  }

  public void setScalePropery(DoubleProperty scalePropery) {
    this.scalePropery = scalePropery;
  }
}
