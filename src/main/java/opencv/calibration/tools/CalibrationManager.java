package opencv.calibration.tools;

import imageprocess.ImageItem;
import opencv.calibration.model.CalibrationConfig;
import opencv.calibration.model.CalibrationImage;
import opencv.calibration.model.CalibrationResults;
import opencv.calibration.tools.calibrator.Calibrator;
import opencv.calibration.tools.calibrator.FisheyeCalibrator;
import opencv.calibration.tools.calibrator.RectilinearCalibrator;
import opencv.calibration.tools.pointfinder.ChessboardCornerFinder;
import opencv.calibration.tools.pointfinder.CircleGridCornerFinder;
import opencv.calibration.tools.pointfinder.PointFinder;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import utils.Translator;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static opencv.calibration.tools.OpenCVUtils.getImageFromMat;

public class CalibrationManager {
    private volatile boolean stoppedCalibration;
    private CalibrationRunListener listener;

    public CalibrationManager(CalibrationRunListener listener){
        this.listener = listener;
    }

    public void runCalibration(List<ImageItem> imageItems, CalibrationConfig config){
        stoppedCalibration = false;

        new Thread(() -> {
            List<CalibrationImage> imagesResults = new ArrayList<>();
            Size boardSize = new Size(config.getHrzPoints(), config.getVertPoints());
            List<Mat> imagePoints = new ArrayList<>();
            List<String> extractedCameras = new ArrayList<>();

            Size imageSize = null;
            int imageCount = imageItems.size();

            Instant start = Instant.now();
            for(int i = 0; i<imageCount && !stoppedCalibration; i++){
                ImageItem imageItem =  imageItems.get(i);
                // Extract camera
                String cameraModel = imageItem.getCameraModel();
                if(!cameraModel.isEmpty() && !extractedCameras.contains(cameraModel)){
                    extractedCameras.add(cameraModel);
                }
                if(listener != null) listener.onProgress(i, imageCount, Translator.getString("progressAnalyzingImage") + " " + imageItem.getName());
                try {
                    // Read from path, skip if not found / empty
                    Mat frame = Imgcodecs.imread(imageItem.getPath());
                    if(frame.empty()){
                        imagesResults.add(new CalibrationImage(imageItem.getImage(), false));
                        continue;
                    }
                    if(imageSize == null) imageSize = frame.size();

                    // Convert to greyscale to improve corner detection
                    Mat grayFrame = new Mat();
                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

                    MatOfPoint2f imageCorners = new MatOfPoint2f();
                    boolean found = findPattern(config.getPattern(), boardSize, grayFrame, imageCorners);
                    if(found){
                        imagePoints.add(imageCorners);
                        Calib3d.drawChessboardCorners(frame, boardSize, imageCorners, found);
                    }
                    imagesResults.add(new CalibrationImage(getImageFromMat(frame), found));
                    frame.release();
                    grayFrame.release();
                } catch (IOException e) {
                    // Ignore & skip to the next one
                }
            }
            if(stoppedCalibration) return;
            List<Mat> objectPoints = getObjectPoints(config.getHrzPoints(), config.getVertPoints(), imagePoints.size(), 1f);
            if(imagePoints.size() > 0){
                if(listener != null && !stoppedCalibration) listener.onProgress(imageCount, imageCount, Translator.getString("progressCalibratingCamera"));
                CalibrationResults calibrationResults = calibrateCamera(objectPoints, imagePoints, imageSize, config.getLens());
                calibrationResults.setImagesResults(imagesResults);
                calibrationResults.setExtractedCameras(extractedCameras);
                calibrationResults.setElapsedTime(Duration.between(start, Instant.now()));
                if(listener != null && !stoppedCalibration) listener.onSuccess(calibrationResults);
            } else {
                if(listener != null && !stoppedCalibration) listener.onFailure();
            }
            stoppedCalibration = true;

        }).start();
    }


    public void stopCalibration() {
        stoppedCalibration = true;
    }


    private boolean findPattern(CalibrationConfig.Pattern pattern, Size boardSize, Mat grayFrame, MatOfPoint2f imageCorners) {
        PointFinder pointFinder = null;
        switch (pattern){
            case CHESSBOARD:
                pointFinder = new ChessboardCornerFinder();
                break;
            case CIRCLE_GRID:
                pointFinder = new CircleGridCornerFinder(CircleGridCornerFinder.Type.SYMMETRIC);
                break;
        }
        return pointFinder.findCorners(grayFrame, boardSize, imageCorners);
    }

    private CalibrationResults calibrateCamera(List<Mat> objectPoints, List<Mat> imagePoints, Size imageSize, CalibrationConfig.Lens lens){
        Calibrator calibrator = null;
        switch (lens){
            case RECTILINEAR:
                calibrator = new RectilinearCalibrator();
                break;
            case FISHEYE:
                calibrator = new FisheyeCalibrator();
                break;
        }
        return calibrator.calibrateCamera(objectPoints, imagePoints, imageSize);
    }

    private List<Mat> getObjectPoints(int horizontalNr, int verticalNr, int listSize, float squareSize){
        List<Mat> objectPoints = new ArrayList<>();
        MatOfPoint3f objectPoint = new MatOfPoint3f();
        for(int y=0; y<verticalNr; y++){
            for(int x=0; x<horizontalNr; x++){
                objectPoint.push_back(new MatOfPoint3f(new Point3(x*squareSize, y*squareSize, 0.0f)));
            }
        }
        for (int i = 0; i < listSize; i++) {
            objectPoints.add(objectPoint);
        }
        return objectPoints;
    }

    public interface CalibrationRunListener{
        void onProgress(int currentImage, int totalImages, String description);
        void onSuccess(CalibrationResults calibrationResults);
        void onFailure();
    }

}
