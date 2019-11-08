package opencv.filters.edgedetection;

public class EdgeDetectionFilter {
  private int threshold;

  public EdgeDetectionFilter(int threshold) {
    this.threshold = threshold;
  }

  public int getThreshold() {
    return threshold;
  }

  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }
}
