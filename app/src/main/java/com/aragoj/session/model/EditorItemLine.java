package com.aragoj.session.model;

import com.aragoj.ui.custom.LineGroup;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "line")
@XmlType(propOrder={"name", "startPointX", "startPointY", "endPointX", "endPointY", "color"})
public class EditorItemLine implements EditorItemLayer{
    private String name;
    private double startPointX;
    private double startPointY;
    private double endPointX;
    private double endPointY;
    private String color;

    public EditorItemLine(LineGroup lineGroup){
        this(lineGroup.getName(), lineGroup.getStartPointX(), lineGroup.getStartPointY(), lineGroup.getEndPointX(), lineGroup.getEndPointY(), lineGroup.getColor());
    }
    public EditorItemLine(String name, double startPointX, double startPointY, double endPointX, double endPointY, String color) {
        this.name = name;
        this.startPointX = startPointX;
        this.startPointY = startPointY;
        this.endPointX = endPointX;
        this.endPointY = endPointY;
        this.color = color;
    }

    public EditorItemLine() {
    }

    @XmlElement(name="name")
    public String getName(){
        return name;
    }

    @XmlElement(name="startPointX")
    public double getStartPointX(){
        return startPointX;
    }

    @XmlElement(name="startPointY")
    public double getStartPointY(){
        return startPointY;
    }

    @XmlElement(name="endPointX")
    public double getEndPointX(){
        return endPointX;
    }

    @XmlElement(name="endPointY")
    public double getEndPointY(){
        return endPointY;
    }

    @XmlElement(name="color")
    public String getColor() {
        return color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartPointX(double startPointX) {
        this.startPointX = startPointX;
    }

    public void setStartPointY(double startPointY) {
        this.startPointY = startPointY;
    }

    public void setEndPointX(double endPointX) {
        this.endPointX = endPointX;
    }

    public void setEndPointY(double endPointY) {
        this.endPointY = endPointY;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
