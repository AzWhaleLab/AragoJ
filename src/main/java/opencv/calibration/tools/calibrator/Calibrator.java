package opencv.calibration.tools.calibrator;

import opencv.calibration.model.CalibrationResults;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.List;

public interface Calibrator {
    CalibrationResults calibrateCamera(List<Mat> objectPoints, List<Mat> imagePoints, Size imageSize);
}
