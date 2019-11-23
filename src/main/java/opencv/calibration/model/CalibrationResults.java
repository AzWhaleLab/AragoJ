package opencv.calibration.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.rmi.CORBA.Util;
import utils.Utility;

public class CalibrationResults {
    private Duration elapsedTime;
    private List<CalibrationImage> imagesResults;
    private List<String> extractedCameras;
    private CalibrationModel calibrationModel;
    private double overallReprojectionError;

    public CalibrationResults() {
        this.imagesResults = new ArrayList<>();
    }

    public double getOverallReprojectionError(){
        return overallReprojectionError;
//        double meanError = 0;
//        for(int i = 0; i<objectPoints.size(); i++){
//            MatOfPoint2f projectedImagePoints = new MatOfPoint2f();
//            Calib3d.projectPoints((MatOfPoint3f) objectPoints.get(i), rvecs.get(i), tvecs.get(i), intrinsic, (MatOfDouble) distCoeffs, projectedImagePoints);
//            double error = Core.norm(imagesPoints.get(i), projectedImagePoints, Core.NORM_L2) / projectedImagePoints.size().area();
//            meanError += error;
//        }
//        System.out.println("Camera Re-projection Error: " + meanError / objectPoints.size());
//        return meanError / objectPoints.size();
    }

    public void setImagesResults(List<CalibrationImage> imagesResults) {
        this.imagesResults = imagesResults;
    }

    public List<CalibrationImage> getImagesResults() {
        return imagesResults;
    }

    public CalibrationModel getCalibrationModel() {
        return calibrationModel;
    }

    public void setCalibrationModel(CalibrationModel calibrationModel) {
        this.calibrationModel = calibrationModel;
    }

    public void setOverallReprojectionError(double overallReprojectionError) {
        this.overallReprojectionError = overallReprojectionError;
    }

    public void setExtractedCameras(List<String> extractedCameras) {
        this.extractedCameras = extractedCameras;
    }

    public String getExtractedCamera(){
        if(extractedCameras == null || extractedCameras.get(0) == null) return "";
        return extractedCameras.get(0);
    }

    public Duration getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Duration elapsedTime) {
        this.elapsedTime = elapsedTime;
    }


    /**
     * Output functions
     */

    public enum OutputDetail{FULL, SIMPLE}

    public String getFormattedOutput(OutputDetail outputDetail) {
        String date = new SimpleDateFormat("HH:mm:ss").format(new Date());;
        String title = date + " Calibration Results\n";
        String elapsedTime = "Elapsed time: " + getFormattedElapsedTime() + "\n";
        String cameraModel = getCameraModelsString();
        String foundPattern = getFoundPatternString();
        double[] intrinsicVals = calibrationModel.getIntrinsicValues();
        String fxfy = "Fx:" + Utility.roundTwoDecimals(intrinsicVals[0]) + "\nFy:" +  Utility.roundTwoDecimals(intrinsicVals[4]) + "\n";
        String reprojectionError = "Overall Reprojection Error (RMS): "
                + String.valueOf(BigDecimal.valueOf(overallReprojectionError).setScale(3, BigDecimal.ROUND_HALF_UP));
        switch(outputDetail){
            case SIMPLE:

                break;
            case FULL:

                break;
        }
        ;
        return title+elapsedTime+cameraModel+foundPattern+fxfy+reprojectionError;

    }

    private String getFormattedElapsedTime(){
        long seconds = elapsedTime.getSeconds();
        if(seconds < 60) return String.format("%d seconds", seconds);
        long minutes = elapsedTime.toMinutes();
        return String.format("%d minutes %d seconds", minutes, elapsedTime.minusMinutes(minutes).getSeconds());
    }
    private String getFoundPatternString(){
        StringBuilder foundPattern = new StringBuilder();
        if(imagesResults.size() > 0){
            foundPattern.append("Pattern found in ");
            int count = 0;
            for(CalibrationImage item : imagesResults){
                if(item.isFound()) count++;
            }
            foundPattern.append(count).append("/").append(imagesResults.size()).append(" images").append("\n");
        }
        return foundPattern.toString();
    }

    private String getCameraModelsString(){
        StringBuilder cameraModel = new StringBuilder();
        if(extractedCameras.size() > 0){
            if(extractedCameras.size() > 1)
                cameraModel.append("*Warning* More than one camera model detected: ");
            else
                cameraModel.append("Extracted camera model name: ");
            cameraModel.append(getExtractedCameras());
            cameraModel.append("\n");
        }
        return cameraModel.toString();
    }

    private String getExtractedCameras(){
        StringBuilder b = new StringBuilder();
        for(int i = 0; i<extractedCameras.size(); i++){
            b.append(extractedCameras.get(i));
            if(i<extractedCameras.size()-1){
                b.append(", ");
            }
        }
        return b.toString();
    }

}
