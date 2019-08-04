package com.aragoj.opencv.calibration.model;

import com.aragoj.opencv.calibration.file.CalibrationData;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Describes the model to be used to undistort images.
 * It's used as a result from calibration and from importing CalibrationData
 *
 * @see CalibrationData
 * @see CalibrationResults
 */

public class CalibrationModel {
    private String name;
    private CalibrationConfig.Lens lens;
    private Mat intrinsic;
    private Mat distortionCoeffs;

    public CalibrationModel(Mat intrinsic, Mat distortionCoeffs, CalibrationConfig.Lens lens) {
        this.intrinsic = intrinsic;
        this.distortionCoeffs = distortionCoeffs;
        this.lens = lens;
    }

    public CalibrationModel(CalibrationData data) {
        double[] distortionCoefficients = data.getDistortionCoefficientsData();
        this.intrinsic = new Mat();
        this.distortionCoeffs = new Mat();
        this.lens = CalibrationConfig.Lens.fromString(data.getLens());

        this.intrinsic.create(3, 3, CvType.CV_64FC1);
        this.intrinsic.put(0, 0, data.getIntrinsicData());

        this.distortionCoeffs.create(1, distortionCoefficients.length, CvType.CV_64FC1);
        this.distortionCoeffs.put(0, 0, distortionCoefficients);

        this.name = data.getName();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Concrete values to be used to export
     */

    public double[] getDistortionCoefficientsValues(){
        double[] distCoefs = new double[distortionCoeffs.cols() * distortionCoeffs.rows()];
        distortionCoeffs.get(0, 0, distCoefs);
        return distCoefs;
    }

    public double[] getIntrinsicValues(){
        double[] intrinsicValues = new double[intrinsic.cols() * intrinsic.rows()];
        intrinsic.get(0, 0, intrinsicValues);
        return intrinsicValues;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CalibrationConfig.Lens getLens() {
        return lens;
    }

    public void setLens(CalibrationConfig.Lens lens) {
        this.lens = lens;
    }

    public Mat getIntrinsic() {
        return intrinsic;
    }

    public void setIntrinsic(Mat intrinsic) {
        this.intrinsic = intrinsic;
    }

    public Mat getDistortionCoeffs() {
        return distortionCoeffs;
    }

    public void setDistortionCoeffs(Mat distortionCoeffs) {
        this.distortionCoeffs = distortionCoeffs;
    }
}
