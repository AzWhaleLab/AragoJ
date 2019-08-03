package opencv.calibration.tools.pointfinder;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;

public interface PointFinder {
    boolean findCorners(Mat inputFrame, Size boardSize, MatOfPoint2f imageCorners);
}
