package com.aragoj.opencv.calibration.file;

import com.aragoj.opencv.calibration.model.CalibrationModel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Used to import / export CalibrationModel data.
 *
 */
@XmlRootElement(name = "calibration")
@XmlType(propOrder={"name", "lens", "intrinsic", "distortionCoefficients"})
public class CalibrationData {
    private String name;
    private String lens;
    private String intrinsic;
    private String distortionCoefficients;

    public CalibrationData(){ }

    public CalibrationData(CalibrationModel model) {
        this.name = model.getName();
        this.lens = model.getLens().toString();

        double[] intrinsicValues = model.getIntrinsicValues();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<intrinsicValues.length; i++){
            sb.append(intrinsicValues[i]);
            if(i < intrinsicValues.length - 1){
                sb.append(" ");
            }
        }
        intrinsic = sb.toString();

        double[] distcoeffs = model.getDistortionCoefficientsValues();
        sb = new StringBuilder();
        for(int i = 0; i<distcoeffs.length; i++){
            sb.append(distcoeffs[i]);
            if(i < distcoeffs.length - 1){
                sb.append(" ");
            }
        }
        distortionCoefficients = sb.toString();
    }

    public double[] getIntrinsicData(){
        if(intrinsic != null && !intrinsic.isEmpty()){
            String[] sArray = intrinsic.split(" ");
            double[] intrinsicData = new double[sArray.length];
            for(int i = 0; i<sArray.length; i++){
                intrinsicData[i] = Double.valueOf(sArray[i]);
            }
            return intrinsicData;
        }
        return null;
    }

    public double[] getDistortionCoefficientsData(){
        if(distortionCoefficients != null && !distortionCoefficients.isEmpty()){
            String[] sArray = distortionCoefficients.split(" ");
            double[] distCoeffs = new double[sArray.length];
            for(int i = 0; i<sArray.length; i++){
                distCoeffs[i] = Double.valueOf(sArray[i]);
            }
            return distCoeffs;
        }
        return null;
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLens() {
        return lens;
    }

    public void setLens(String lens) {
        this.lens = lens;
    }

    @XmlElement(name = "intrinsic")
    public String getIntrinsic() {
        return intrinsic;
    }

    public void setIntrinsic(String intrinsic) {
        this.intrinsic = intrinsic;
    }


    @XmlElement(name = "distortion_coefficients")
    public String getDistortionCoefficients() {
        return distortionCoefficients;
    }

    public void setDistortionCoefficients(String distortionCoefficients) {
        this.distortionCoefficients = distortionCoefficients;
    }

}
