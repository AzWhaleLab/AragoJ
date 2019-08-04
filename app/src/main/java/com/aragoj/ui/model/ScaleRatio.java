package com.aragoj.ui.model;

import com.aragoj.utils.Utility;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "zoom")
public class ScaleRatio {
    private double ratio;
    private String units;

    public ScaleRatio(){

    }

    public ScaleRatio(double ratio, String units) {
        this.ratio = ratio;
        this.units = units;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public double getScaledValue(double length){
        return length*ratio;
    }

    public double getRoundedScaledValue(double length){
        return Utility.roundTwoDecimals(getScaledValue(length));
    }

}
