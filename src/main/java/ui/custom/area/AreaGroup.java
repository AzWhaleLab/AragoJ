package ui.custom.area;

import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;
import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;
import session.model.EditorItemArea;
import session.model.EditorItemPosition;
import ui.custom.Cross;
import ui.custom.ToolEventHandler;
import ui.model.LayerListItem;
import utils.AreaUtils;
import utils.Utility;

import java.awt.geom.Area;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AreaGroup extends Group implements LayerListItem {

    private AreaEventHandler areaEventHandler;

    private String name;
    private Color color;
    private ArrayList<Line> lines;
    private ArrayList<Cross> vertices;

    private Circle startPointCircle; // To end the area
    private boolean isFinished;

    private Polygon polygon;
    private float opacity;


    public AreaGroup(EditorItemArea area, AreaEventHandler handler){
        this.vertices = new ArrayList<>();
        this.lines = new ArrayList<>();
        this.areaEventHandler = handler;
        this.name = area.getName();
        this.color = Color.valueOf(area.getColor());
        setGroupOpacity(0.5f);

        List<EditorItemPosition> verts = area.getVertices();
        for(int i = 0; i<verts.size(); i++){
            EditorItemPosition pos = verts.get(i);
            addVertex(pos.getX(), pos.getY(), false);
            if(i+1 == verts.size()){
                EditorItemPosition initialVert = verts.get(0);
                createInitialPointCircle(initialVert.getX(), initialVert.getY());
                addVertex(initialVert.getX(), initialVert.getY(), true);
                completeArea(false);
            }
        }
    }

    public AreaGroup(String name, double startPointX, double startPointY, AreaEventHandler handler, Color color) {
        this.lines = new ArrayList<>();
        this.vertices = new ArrayList<>();
        this.areaEventHandler = handler;
        this.name = name;
        this.color = color;
        this.isFinished = false;

        setGroupOpacity(0.5f);
        createInitialPointCircle(startPointX, startPointY);
        addVertex(startPointX, startPointY, false);
    }

    private void setGroupOpacity(float opacity) {
        this.opacity = opacity;
        if (polygon != null) {
            polygon.setOpacity(opacity);
        }
    }

    private void createInitialPointCircle(double startPointX, double startPointY) {
        startPointCircle = new Circle();
        startPointCircle.setStrokeWidth(0.1f);
        startPointCircle.setStrokeType(StrokeType.INSIDE);
        startPointCircle.setStroke(Color.TRANSPARENT);
        startPointCircle.setOpacity(0);
        startPointCircle.setCenterX(startPointX);
        startPointCircle.setCenterY(startPointY);
        startPointCircle.setRadius(1);

        startPointCircle.setOnMouseEntered(e -> {
            if (isCompletable()) {
                Cross cross = vertices.get(0);
                cross.setColor(color);
                // Set final cursor
            }
        });
        startPointCircle.setOnMouseExited(e -> {
            // Unset final cursor
            if (isCompletable()) {
                Cross cross = vertices.get(0);
                cross.resetColor();
            }
        });
        startPointCircle.setOnMousePressed(e -> {
            if (isCompletable()) {
                e.consume();
                addVertex(startPointX, startPointY, true);
                Cross cross = vertices.get(0);
                cross.resetColor();
                completeArea(true);
            }
        });
        getChildren().addAll(startPointCircle);
    }

    private boolean isCompletable() {
        return !isFinished && vertices.size() > 2;
    }

    private void completeArea(boolean emitCallback) {
        double[] points = new double[vertices.size() * 2];
        int i = 0;
        for (Cross vert : vertices) {
            points[i++] = vert.getPointX();
            points[i++] = vert.getPointY();
        }
        Polygon polygon = new Polygon(points);
        polygon.setFill(color);
        polygon.setOpacity(opacity);
        getChildren().addAll(polygon);

        isFinished = true;
        if(emitCallback) areaEventHandler.onAreaComplete(this);
        startPointCircle.setOnMouseExited(null);
        startPointCircle.setOnMouseEntered(null);
        startPointCircle.setOnMousePressed(null);
    }

    public double calculateArea() {
        java.awt.Polygon polygon = new java.awt.Polygon();
        for (Cross cross : vertices) {
            polygon.addPoint((int) cross.getPointX(), (int) cross.getPointY());
        }
        Area area = new Area(polygon);
        return Utility.roundTwoDecimals(AreaUtils.approxArea(area, 0, 0));
    }

    public void addVertex(double x, double y, boolean skipVertexShape) {
        if (!skipVertexShape) vertices.add(createCross(x, y));
        if (lines.size() > 0) {
            Line lastLine = lines.get(lines.size() - 1);
            lastLine.setEndX(x);
            lastLine.setEndY(y);
        }

        lines.add(createLine(x, y, x, y));
    }

    public void moveLastVertex(double x, double y) {
        if (vertices.size() <= 0 || lines.size() <= 0) return;
        setVertexPosition(x, y, vertices.size());
    }

    private void setVertexPosition(double x, double y, int index) {
        if (index == 0) {
            Line previousLine = lines.get(0);
            previousLine.setStartX(x);
            previousLine.setStartY(y);
        } else if (index > 0) {
            Line previousLine = lines.get(index - 1);
            previousLine.setEndX(x);
            previousLine.setEndY(y);
            if (index < lines.size()) {
                Line currentLine = lines.get(index);
                previousLine.setStartX(x);
                previousLine.setStartY(y);
            }
            if (index < vertices.size()) {
                Cross currentVertex = vertices.get(index);
                currentVertex.setPointX(x);
                currentVertex.setPointY(y);
            }
        }
    }

    private Cross createCross(double startPointX, double startPointY) {
        Cross cross = new Cross(startPointX, startPointY);
        getChildren().add(cross.getShape());
        return cross;
    }

    private Line createLine(double startPointX, double startPointY, double endPointX, double endPointY) {
        Line line = new Line();
        line.setStrokeWidth(0.05f);
        line.setStroke(Color.WHITE);
        line.setBlendMode(BlendMode.DIFFERENCE);
        line.setMouseTransparent(true);

        line.setStartX(startPointX);
        line.setStartY(startPointY);
        line.setEndX(endPointX);
        line.setEndY(endPointY);

        getChildren().add(line);

        return line;
    }

    public void setName(String name) {
        this.name = name;
        if (areaEventHandler != null) areaEventHandler.onAreaChanged(AreaGroup.this);
    }

    public String getColorString(){
        return color.toString();
    }

    public List<EditorItemPosition> getExportableVertices(){
        ArrayList<EditorItemPosition> verts = new ArrayList<>(vertices.size());
        for(int i = 0; i<vertices.size(); i++){
            Cross cross = vertices.get(i);
            verts.add(new EditorItemPosition(cross.getPointX(), cross.getPointY()));
        }
        return verts;
    }

    @Override
    public SVGGlyph getSVG() throws IOException {
        SVGGlyph glyph = SVGGlyphLoader.loadGlyph(getClass().getClassLoader().getResource("svg/1-area_vector.svg"));
        glyph.setFill(color);
        glyph.setSize(32, 32);
        return glyph;
    }

    @Override
    public String getPrimaryText() {
        return name;
    }

    @Override
    public void setPrimaryText(String primaryText) {
        name = primaryText;
    }

    @Override
    public String getSecondaryText() {
        return calculateArea() + " pixels";
    }

    @Override public String getStatus() {
        return "";
    }

    @Override
    public Type getType() {
        return Type.AREA;
    }

    @Override
    public boolean isVisualElement() {
        return true;
    }

    public void cancel() {
        vertices.clear();
        lines.clear();
        startPointCircle.setOnMouseEntered(null);
        startPointCircle.setOnMouseExited(null);
        startPointCircle.setOnMousePressed(null);
    }

    public interface AreaEventHandler extends ToolEventHandler {
        void onVertexChange(AreaGroup line, int vertexIndex, double x, double y);

        void onAreaChanged(AreaGroup areaGroup);

        void onAreaComplete(AreaGroup area);

    }

}
