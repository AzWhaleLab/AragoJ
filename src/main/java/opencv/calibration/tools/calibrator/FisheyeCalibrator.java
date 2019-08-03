package opencv.calibration.tools.calibrator;

import opencv.calibration.model.CalibrationConfig;
import opencv.calibration.model.CalibrationModel;
import opencv.calibration.model.CalibrationResults;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.List;

public class FisheyeCalibrator implements Calibrator {
    private static final CalibrationConfig.Lens LENS_TYPE = CalibrationConfig.Lens.FISHEYE;
    private static final int FLAGS = Calib3d.CALIB_RECOMPUTE_EXTRINSIC | Calib3d.CALIB_CHECK_COND | Calib3d.CALIB_FIX_SKEW;

    @Override
    public CalibrationResults calibrateCamera(List<Mat> objectPoints, List<Mat> imagePoints, Size imageSize) {
        List<Mat> rvecs = new ArrayList<>();
        List<Mat> tvecs = new ArrayList<>();

        Mat intrinsic = new Mat();
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(intrinsic);
        intrinsic.put(0, 0, 1.0);

        Mat distCoeffs = new Mat();
        Mat.zeros(4, 1, CvType.CV_64FC1).copyTo(distCoeffs);
        CalibrationResults calibrationResults = new CalibrationResults();
        calibrationResults.setOverallReprojectionError(Calib3d.calibrate(objectPoints, imagePoints, imageSize, intrinsic, distCoeffs, rvecs, tvecs, FLAGS));
        calibrationResults.setCalibrationModel(new CalibrationModel(intrinsic, distCoeffs, LENS_TYPE));
        return calibrationResults;
    }
}
