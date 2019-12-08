package opencv.filters.sharpen;

import javafx.scene.image.Image;
import opencv.filters.Filter;
import opencv.filters.FilterArguments;
import opencv.utils.OpenCVUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class SharpenFilter implements Filter {

  // We could also do with a kernel, e.g:
  //
  // Mat kernel = new Mat(3, 3, CV_16SC1);
  // kernel.put(0, 0, -1, -1, -1, -1, 9, -1, -1, -1, -1);
  // Imgproc.filter2D(imgMat, imgMat, CV_8UC1, kernel);

  @Override public Image applyFilter(Image image, FilterArguments arguments, String path)
      throws FilterArguments.NoArgumentFound {
    try {
      Mat imgMat = OpenCVUtils.getMatFromImage(image);
      Mat a = new Mat();

      Imgproc.GaussianBlur(imgMat, a, new Size(0, 0), 3);
      Core.addWeighted(imgMat, 1.5, a, -0.5, 0, a);
      if (!path.isEmpty()) {
        Imgcodecs.imwrite(path, a);
      }
      return OpenCVUtils.getImageFromMat(a);
    } catch (Exception e) {
      e.printStackTrace();
      return image;
    }
  }
}
