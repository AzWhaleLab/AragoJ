package com.aragoj.ui.custom;

import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Cross {

    private static final Color DEFAULT_COLOR = Color.WHITE;
    private static final double THICKNESS = 0.05;
    private double pointX;
    private double pointY;

    private Rectangle rectH;
    private Rectangle rectV;

    private Shape shape;

    public Cross(double x, double y){
        this(x, y, 0, 0);
    }

    public Cross(double x, double y, double translateX, double translateY){
        this.pointX = x;
        this.pointY = y;
        this.rectH = new Rectangle(pointX-1, pointY-(THICKNESS/2.0), 2,THICKNESS);
        this.rectV = new Rectangle(pointX-(THICKNESS/2.0), pointY-1, THICKNESS,2);
        this.shape = Shape.union(rectH,rectV);
        shape.setFill(DEFAULT_COLOR);
        shape.setTranslateX(translateX);
        shape.setTranslateY(translateY);
        shape.setBlendMode(BlendMode.DIFFERENCE);
        shape.setMouseTransparent(true);
    }

    public void setColor(Color color){
        shape.setFill(color);
    }

    public void resetColor(){
        shape.setFill(DEFAULT_COLOR);
    }


    public void setOpacity(double opacity){
        shape.setOpacity(opacity);
    }

    public void setPoint(double x, double y){
        this.shape.setLayoutX(x - this.shape.getLayoutBounds().getMinX());
        this.shape.setLayoutY(y - this.shape.getLayoutBounds().getMinY());
        this.pointX = x;
        this.pointY = y;
    }

    public void setPointX(double x){
        this.shape.setLayoutX(x - this.shape.getLayoutBounds().getMinX());
        this.pointX = x;
    }

    public void setPointY(double y){
        this.shape.setLayoutY(y - this.shape.getLayoutBounds().getMinY());
        this.pointY = y;
    }

    public double getPointX() {
        return pointX;
    }

    public double getPointY() {
        return pointY;
    }

    public Shape getShape(){
        return shape;
    }
}
