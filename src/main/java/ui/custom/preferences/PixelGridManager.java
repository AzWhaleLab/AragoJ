package ui.custom.preferences;

public class PixelGridManager {

  private double step = -1;

  public void setStep(double step){
    this.step = step;
  }

  public double getStep() {
    return step;
  }

  public double correct(double coordinate){
    return step <= 0 ? coordinate : Math.round(coordinate * (1/step)) / (1/step);
  }
}
