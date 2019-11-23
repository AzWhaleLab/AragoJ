package opencv.filters.edgedetection.canny;

import javafx.scene.image.Image;
import opencv.filters.Filter;
import opencv.filters.FilterArguments;
import opencv.utils.OpenCVUtils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class CannyEdgeFilter implements Filter {

  @Override public Image applyFilter(Image image, FilterArguments arguments, String path)
      throws FilterArguments.NoArgumentFound {
    int threshold = arguments.getInt("threshold");
    try{
      Mat imgMat = OpenCVUtils.getMatFromImage(image);
      Mat result = new Mat();
      Imgproc.Canny(imgMat, result, threshold, threshold*3);
      return OpenCVUtils.getImageFromMat(result);
    } catch (OpenCVUtils.UnsupportedFormatException e) {
      e.printStackTrace();
      return image;
    }
  }

  public static FilterArguments buildArguments(int threshold){
    FilterArguments filterArguments = new FilterArguments();
    filterArguments.putInt("threshold", threshold);
    return filterArguments;
  }
}
