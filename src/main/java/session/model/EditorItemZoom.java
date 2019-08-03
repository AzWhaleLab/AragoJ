package session.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "zoom")
public class EditorItemZoom {
    private double hValue;
    private double vValue;
    private double scale;

    public EditorItemZoom(){ }

    public EditorItemZoom(double hValue, double vValue, double scale) {
        this.hValue = hValue;
        this.vValue = vValue;
        this.scale = scale;
    }

    @XmlElement
    public double gethValue() {
        return hValue;
    }

    public void sethValue(double hValue) {
        this.hValue = hValue;
    }

    @XmlElement
    public double getvValue() {
        return vValue;
    }

    public void setvValue(double vValue) {
        this.vValue = vValue;
    }

    @XmlElement
    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

}
