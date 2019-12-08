package opencv.filters.edgedetection.sobel;

import javafx.scene.image.Image;
import opencv.filters.Filter;
import opencv.filters.FilterArguments;
import opencv.utils.OpenCVUtils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.BORDER_DEFAULT;
import static org.opencv.core.Core.magnitude;
import static org.opencv.core.CvType.CV_32F;

public class SobelEdgeFilter implements Filter {

  @Override public Image applyFilter(Image image, FilterArguments arguments, String destPath) {
    try {
      Mat imgMat = OpenCVUtils.getMatFromImage(image);
      imgMat.convertTo(imgMat, CV_32F, 1.f / 255);

      Mat rV = new Mat();
      Mat rH = new Mat();
      Mat r = new Mat();
      Imgproc.Sobel(imgMat, rV, -1, 1, 0, 3, 1, 0, BORDER_DEFAULT);
      Imgproc.Sobel(imgMat, rH, -1, 0, 1, 3, 1, 0, BORDER_DEFAULT);
      magnitude(rV, rH, r);
      r.convertTo(r, OpenCVUtils.getCvType(image.getPixelReader()
          .getPixelFormat()
          .getType()), 255);
      if (!destPath.isEmpty()) {
        Imgcodecs.imwrite(destPath, r);
      }
      return OpenCVUtils.getImageFromMat(r);
    } catch (OpenCVUtils.UnsupportedFormatException e) {
      e.printStackTrace();
      return image;
    }
  }
}
