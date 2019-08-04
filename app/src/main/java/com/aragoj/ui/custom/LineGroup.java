package com.aragoj.ui.custom;

import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import com.aragoj.session.model.EditorItemLine;
import com.aragoj.ui.model.LayerListItem;
import com.aragoj.utils.Utility;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.IOException;


//private String name;
//private double startPointX;
//private double startPointY;
//private double endPointX;
//private double endPointY;
//private Color color;

@XmlRootElement(name = "line")
@XmlType(propOrder={"name", "startPointX", "startPointY", "endPointX", "endPointY", "color"})
public class LineGroup extends Group implements LayerListItem {

    private LineEventHandler handler;

    private Circle startPointCircle;
    private Circle endPointCircle;
    private Line line;

    private String name;
    private double width;
    private Color color;

    private double currentX = 0; // Used for whole line movement
    private double currentY = 0; // Used for whole line movement

    // Precision helpers
    private Line helperLine;
    private Cross startPointCross;
    private Cross endPointCross;

    public LineGroup(EditorItemLine layer, LineEventHandler handler){
        this(layer.getName(), layer.getStartPointX(), layer.getStartPointY(), layer.getEndPointX(), layer.getEndPointY(), handler, Color.valueOf(layer.getColor()));
    }

    public LineGroup(String name, double startPointX, double startPointY, LineEventHandler handler, Color color) {
        this(name, startPointX, startPointY, startPointX, startPointY, handler, color);
    }

    public LineGroup(String name, double startPointX, double startPointY, double endPointX, double endPointY, LineEventHandler handler, Color color){
        super();

        this.handler = handler;
        this.name = name;
        line = new Line();
        helperLine = new Line();
        startPointCross = new Cross(startPointX, startPointY, -1, -1);
        endPointCross = new Cross(endPointX, endPointY, -1, -1);

        startPointCircle = new Circle();
        endPointCircle = new Circle();

        // Set start/end point stroke styling
        startPointCircle.setStrokeWidth(0.1f);
        startPointCircle.setStrokeType(StrokeType.INSIDE);
        startPointCircle.setStroke(Color.LIGHTGRAY);
        endPointCircle.setStrokeWidth(0.1f);
        endPointCircle.setStrokeType(StrokeType.INSIDE);
        endPointCircle.setStroke(Color.LIGHTGRAY);
        line.setStrokeType(StrokeType.CENTERED);
        line.setStrokeLineCap(StrokeLineCap.ROUND);


        helperLine.setStrokeWidth(0.05f);
        helperLine.setStroke(Color.WHITE);
        helperLine.setBlendMode(BlendMode.DIFFERENCE);
        helperLine.setMouseTransparent(true);
        setWidth(3);
        setColor(color);
        setGroupOpacity(0.5f);
        setStartPoint(startPointX, startPointY);
        setEndPoint(endPointX, endPointY);

        getChildren().addAll(line, startPointCircle, endPointCircle, helperLine, startPointCross.getShape(), endPointCross.getShape());

        setListeners();
    }

    private void setListeners() {
        line.setOnMousePressed(event -> {
            currentX = event.getX();
            currentY = event.getY();
        });
        line.setOnMouseDragged(event -> {
            if(handler != null) handler.onLineChange(LineGroup.this,event.getX()-currentX, event.getY()-currentY);
            currentX = event.getX();
            currentY = event.getY();
        });
        startPointCircle.setOnMouseDragged(event -> {
            if(!event.isControlDown() && handler != null){
                if(event.isShiftDown()){
                    handler.onAngledStartPointChange(LineGroup.this, event.getX(), event.getY());
                }
                else{
                    handler.onStartPointChange(LineGroup.this, event.getX(), event.getY());
                }
            }

        });
        endPointCircle.setOnMouseDragged(event -> {
            if(!event.isControlDown() && handler != null) {
                if(event.isShiftDown()){
                    handler.onAngledEndPointChange(LineGroup.this, event.getX(), event.getY());
                }
                else{
                    handler.onEndPointChange(LineGroup.this, event.getX(), event.getY());
                }
            }
        });
    }

    private void setGroupOpacity(double value){
        line.setOpacity(value);
        startPointCircle.setOpacity(value);
        endPointCircle.setOpacity(value);
    }
    public void setColor(Color color){
        this.color = color;
        line.setStroke(color);
        startPointCircle.setFill(Color.BLACK);
        endPointCircle.setFill(Color.BLACK);
    }

    public void setWidth(double width){
        line.setStrokeWidth(width);
        startPointCircle.setRadius(width/2);
        endPointCircle.setRadius(width/2);
        this.width = width;
    }

    public void setStartPoint(double startPointX, double startPointY){
        line.setStartX(startPointX);
        line.setStartY(startPointY);
        startPointCircle.setCenterX(startPointX);
        startPointCircle.setCenterY(startPointY);

        helperLine.setStartX(startPointX);
        helperLine.setStartY(startPointY);
        startPointCross.setPoint(startPointX, startPointY);

        if(handler != null) handler.onLineChange();

    }

    public void setStartPoint(Point2D point){
        setStartPoint(point.getX(), point.getY());
    }

    public void setEndPoint(double endPointX, double endPointY){
        line.setEndX(endPointX);
        line.setEndY(endPointY);
        endPointCircle.setCenterX(endPointX);
        endPointCircle.setCenterY(endPointY);

        helperLine.setEndX(endPointX);
        helperLine.setEndY(endPointY);
        endPointCross.setPoint(endPointX, endPointY);

        if(handler != null) handler.onLineChange();
    }

    public void setEndPoint(Point2D point){
        setEndPoint(point.getX(), point.getY());
    }

    public void moveLineGroup(double dx, double dy){
        line.setStartX(line.getStartX()+dx);
        line.setStartY(line.getStartY()+dy);
        startPointCircle.setCenterX(startPointCircle.getCenterX()+dx);
        startPointCircle.setCenterY(startPointCircle.getCenterY()+dy);
        line.setEndX(line.getEndX()+dx);
        line.setEndY(line.getEndY()+dy);
        endPointCircle.setCenterX(endPointCircle.getCenterX()+dx);
        endPointCircle.setCenterY(endPointCircle.getCenterY()+dy);

        helperLine.setStartX(helperLine.getStartX()+dx);
        helperLine.setStartY(helperLine.getStartY()+dy);
        helperLine.setEndX(helperLine.getEndX()+dx);
        helperLine.setEndY(helperLine.getEndY()+dy);
        startPointCross.setPointX(startPointCross.getPointX()+dx);
        startPointCross.setPointY(startPointCross.getPointY()+dy);
        endPointCross.setPointX(endPointCross.getPointX()+dx);
        endPointCross.setPointY(endPointCross.getPointY()+dy);

    }

    public void setPrecisionHelpersVisible(boolean visible){
        if(visible){
            helperLine.setOpacity(1);
            startPointCross.setOpacity(1);
            endPointCross.setOpacity(1);
        }
        else{
            helperLine.setOpacity(0);
            startPointCross.setOpacity(0);
            endPointCross.setOpacity(0);
        }
    }

    public void setColorHelpersVisible(boolean visible){
        if(visible){
            setGroupOpacity(0.5f);
        }
        else{
            setGroupOpacity(0);
        }
    }

    public void setName(String name){
        this.name = name;
        if(handler != null) handler.onLineChange();
    }

    public double getLength(){
        double dx = line.getEndX()-line.getStartX();
        double dy = line.getEndY()-line.getStartY();
        return Utility.roundTwoDecimals(Math.sqrt((dx*dx) + (dy*dy)));
    }

    public double getLineAngle() {
        double startX = line.getStartX();
        double startY = line.getStartY();
        double endX = line.getEndX();
        double endY = line.getEndY();

        double x = endX-startX;
        double y = endY-startY;
        double ax = -1.0;
        if(startY < endY) ax = 1.0;
        double ay = 0.0;

        final double delta = (ax * x + ay * y) / Math.sqrt(
                (ax * ax + ay * ay) * (x * x + y * y));

        return Math.acos(delta);
    }

    @Override
    public SVGGlyph getSVG() throws IOException {
        SVGGlyph glyph = SVGGlyphLoader.loadGlyph(getClass().getClassLoader().getResource("svg/1-layer_vector.svg"));
        glyph.setFill(color);
        glyph.setSize(32,32);
        return glyph;
    }

    @Override
    public String getPrimaryText() {
        return name;
    }

    @Override
    public void setPrimaryText(String primaryText) {
        setName(primaryText);
    }

    @Override
    public String getSecondaryText() {
        return getLength() + " pixels";
    }

    @Override
    public Type getType() {
        return Type.LINE;
    }

    @Override
    public boolean isVisualElement() {
        return true;
    }

    public interface LineEventHandler extends ToolEventHandler {
        void onStartPointChange(LineGroup line, double x, double y);
        void onEndPointChange(LineGroup line, double x, double y);
        void onAngledStartPointChange(LineGroup line, double x, double y);
        void onAngledEndPointChange(LineGroup line, double x, double y);
        void onLineChange(LineGroup line, double dx, double dy);
        void onLineChange();
    }



    @XmlElement(name="name")
    public String getName(){
        return name;
    }

    @XmlElement(name="startPointX")
    public double getStartPointX(){
        return line.getStartX();
    }

    @XmlElement(name="startPointY")
    public double getStartPointY(){
        return line.getStartY();
    }

    @XmlElement(name="endPointX")
    public double getEndPointX(){
        return line.getEndX();
    }

    @XmlElement(name="endPointY")
    public double getEndPointY(){
        return line.getEndY();
    }

    @XmlElement(name="color")
    public String getColor() {
        return color.toString();
    }

    @XmlTransient
    public double getWidth() {
        return width;
    }

    public Color getColorObj() {
        return color;
    }

    public void setHandler(LineEventHandler lineEventHandler){
        this.handler = lineEventHandler;
    }
}
