package ui.model;

import utils.Utility;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "zoom")
public class ScaleRatio {
    private double ratio;
    private String units;

    public ScaleRatio(){
        ratio = -1.0;
        units = "";
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

    public boolean hasScale(){
        return  ratio > 0;
    }

}
