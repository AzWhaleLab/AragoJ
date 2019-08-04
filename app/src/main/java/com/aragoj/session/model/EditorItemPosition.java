package com.aragoj.session.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "pos")
@XmlType(propOrder = {"x", "y"})
public class EditorItemPosition {

    private double x;
    private double y;

    public EditorItemPosition() {
    }

    public EditorItemPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @XmlElement(name="x")
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    @XmlElement(name="y")
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
