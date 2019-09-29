package ui.custom;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import ui.MainApplication;
import ui.model.LayerListItem;
import ui.model.ScaleRatio;
import utils.Constants;

import java.security.Key;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import static ui.custom.ImageEditorStackGroup.Mode.ANG;
import static ui.custom.ImageEditorStackGroup.Mode.ANG_SEL;
import static ui.custom.ImageEditorStackGroup.Mode.AREA_VERTICE_SELECT;


/**
 * Group responsible for layering measuring tools over the image
 * <p>
 * This group is wrapped by ZoomableScrollPane
 */
public class ImageEditorStackGroup extends Group implements LineGroup.LineEventHandler, AreaGroup.AreaEventHandler {
    public static Color DEFAULT_COLOR = Color.RED;
    private Preferences prefs;


    public enum Mode {PAN, ZOOM, SELECT, LINE, ANG_SEL, ANG, AREA_CREATION, AREA_VERTICE_SELECT}

    private ModeListener modeListener;
    private ElementListener elementListener;

    private ArrayList<LayerListItem> elements;

    private int currentSelectedItemIndex = -1;
    private double helperLineAngle = -1;
    private Color currentPickedColor;
    private double addedLineAngle;

    private int lineCount;
    private int areaCount;

    private Mode currentMode;
    private ScaleRatio currentScale;

    private Bounds bounds;


    public ImageEditorStackGroup(ModeListener modeListener, ElementListener elementListener, Color color, double angle) {
        super();
        lineCount = 1;
        areaCount = 1;
        prefs = Preferences.userNodeForPackage(MainApplication.class);
        addedLineAngle = angle;
        currentPickedColor = color;
        this.modeListener = modeListener;
        this.elementListener = elementListener;
        elements = new ArrayList<>();
        setListeners();
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    private void setListeners() {
        setOnMousePressed(mousePressedHandler);
        setOnMouseDragged(mouseDraggedHandler);
        setOnMouseMoved(mouseMovedHandler);

        setOnKeyPressed(keyPressedHandler);

        // Block any event that is not CTRL modified or PAN
//        addEventHandler(MouseEvent.ANY, event -> {
//            if (!event.isControlDown() && currentMode != Mode.PAN) event.consume();
//        });
    }

    private EventHandler<KeyEvent> keyPressedHandler = e -> {

        if(e.getCode() == KeyCode.ESCAPE){
            if (currentMode == Mode.AREA_VERTICE_SELECT) {
                int index = elements.size() - 1;
                ((AreaGroup) elements.get(index)).cancel();
                elements.remove(index);
                getChildren().remove(index+1);
                currentMode = Mode.AREA_CREATION;
            }
        }

    };

    private EventHandler<MouseEvent> mouseMovedHandler = e -> {
        if (e.isControlDown() || currentMode == Mode.PAN) return;

        if (currentMode == Mode.AREA_VERTICE_SELECT) {
            ((AreaGroup) elements.get(elements.size() - 1)).moveLastVertex(e.getX(), e.getY());
        }
    };
    /**
     * Handles two options (on mouse press):
     * - Creation of a new measuring tool (free, perpendicular or paralell line)
     * - Selecting a line (for different uses).
     */
    private EventHandler<MouseEvent> mousePressedHandler = e -> {
        requestFocus();
        if (e.isControlDown() || currentMode == Mode.PAN) return;

        if (currentMode == ImageEditorStackGroup.Mode.ZOOM) {
            e.consume();
            VBox vBox = (VBox) ((ImageEditorStackGroup) e.getSource()).getParent().getParent();
            ZoomableScrollPane scrollPane = (ZoomableScrollPane) vBox.getParent().getParent().getParent();
            Point2D point = vBox.sceneToLocal(new Point2D(e.getSceneX(), e.getSceneY()));
            if (e.isPrimaryButtonDown()) {
                scrollPane.onScroll(3, point);
            } else if (e.isSecondaryButtonDown()) {
                scrollPane.onScroll(-3, point);
            }
        }
        if (currentMode == Mode.LINE || currentMode == ANG) {
            e.consume();
            // Create a new line
            LineGroup line = new LineGroup("Line_" + lineCount++, e.getX(), e.getY(), this, currentPickedColor);
            addElement(line, false);
            currentSelectedItemIndex = elements.size() - 1;
        }

        if (currentMode == Mode.AREA_VERTICE_SELECT) {
            e.consume();
            ((AreaGroup) elements.get(elements.size() - 1)).addVertex(e.getX(), e.getY(), false);
        }

        if (currentMode == Mode.AREA_CREATION) {
            e.consume();
            currentMode = AREA_VERTICE_SELECT;
            // Create a new line
            AreaGroup areaGroup = new AreaGroup("Area_" + areaCount++, e.getX(), e.getY(), this, currentPickedColor);
            addElement(areaGroup, false);
            currentSelectedItemIndex = elements.size() - 1;
        }
    };

    /**
     * Handles on mouse dragged event:
     * - Updates either start or end X and Y position of a line.
     */
    private EventHandler<MouseEvent> mouseDraggedHandler = event -> {
        if (event.isControlDown() || currentMode == Mode.PAN || currentSelectedItemIndex == -1) return;
        LayerListItem item = elements.get(currentSelectedItemIndex);
        if (item.getType() == LayerListItem.Type.LINE) {
            LineGroup line = (LineGroup) item;
            if (currentMode == Mode.LINE && currentSelectedItemIndex != -1) {
                line.setEndPoint(getCorrectedPointX(bounds, event.getX()), getCorrectedPointY(bounds, event.getY()));
            }
            if (currentMode == Mode.ANG && currentSelectedItemIndex != -1) {
                event.consume();
                double length = getDeltaAngledLineLength(line, event.getX(), event.getY(), helperLineAngle + addedLineAngle, true);
                double x = getAngledPointX(helperLineAngle, line.getStartPointX(), length, true);
                double y = getAngledPointY(helperLineAngle, line.getStartPointY(), length, true);
                line.setEndPoint(getCorrectedPointX(bounds, x), getCorrectedPointY(bounds, y));
            }
        }

    };

    public Color getCurrentPickedColor() {
        return currentPickedColor;
    }

    public void setCurrentPickedColor(Color currentPickedColor) {
        this.currentPickedColor = currentPickedColor;
        prefs.put(Constants.STTGS_COLOR_PICKER, currentPickedColor.toString());
    }

    public double getAddedLineAngle() {
        return addedLineAngle;
    }

    public void setAddedLineAngle(double addedLineAngle) {
        this.addedLineAngle = Math.toRadians(addedLineAngle);
        prefs.putDouble(Constants.STTGS_ANGLE_PICKER, addedLineAngle);
    }

    public void addElement(LayerListItem item, boolean editorItemLoad) {
        if (item.getType() == LayerListItem.Type.LINE) {
            ((LineGroup) item).setOnMouseClicked(event -> {
                if (currentMode == ANG_SEL) {
                    LineGroup line = (LineGroup) event.getSource();
                    helperLineAngle = line.getLineAngle();

                    if (currentMode == ANG_SEL) currentMode = ANG;
                    if (modeListener != null) modeListener.onModeChange(currentMode);
                }
            });
        }
        if (item.isVisualElement()) getChildren().add((Node) item);
        elements.add(item);
        if (elementListener != null) {
            if (item.getType() == LayerListItem.Type.LINE) {
                elementListener.onLineAdd((LineGroup) item, editorItemLoad);
            } else if (item.getType() == LayerListItem.Type.AREA && editorItemLoad) {
                elementListener.onAreaAdd((AreaGroup) item, editorItemLoad);
            }
        }
    }

    public void addAreaGroup(AreaGroup areaGroup, boolean editorItemLoad) {
        getChildren().add(areaGroup);
        elements.add(areaGroup);
        if (elementListener != null) elementListener.onAreaAdd(areaGroup, editorItemLoad);
    }

    public ScaleRatio getCurrentScale() {
        return currentScale;
    }

    public void setCurrentScale(ScaleRatio currentScale) {
        this.currentScale = currentScale;
    }

    /**
     * Removes a layer (line) from the ImageEditorStackGroup
     */
    public void removeLineGroup(String name) {
        for (int i = 0; i < elements.size(); i++) {
            LayerListItem item = elements.get(i);
            if (item.getPrimaryText().equals(name)) {
                elements.remove(i);
                getChildren().remove(i + 1); // + 1 because the first layer is the base image
            }
        }
    }

    public void renameLineGroup(String oldName, String name) {
        for (int i = 0; i < elements.size(); i++) {
            LayerListItem item = elements.get(i);
            if (item.getPrimaryText().equals(oldName)) {
                elements.get(i).setPrimaryText(name);
            }
        }
    }


    public void clearList() {
        elements.clear();
        areaCount = 1;
        lineCount = 1;
    }

    public ArrayList<LineGroup> getLines() {
        ArrayList<LineGroup> lines = new ArrayList<>();
        for (LayerListItem item : elements) {
            if (item.getType() == LayerListItem.Type.LINE) {
                lines.add((LineGroup) item);
            }
        }
        return lines;
    }

    /**
     * Individual line point change handlers
     */
    @Override
    public void onStartPointChange(LineGroup line, double x, double y) {
        if (currentMode == ImageEditorStackGroup.Mode.SELECT) {
            line.setStartPoint(getCorrectedPointX(bounds, x), getCorrectedPointY(bounds, y));
        }
    }

    @Override
    public void onEndPointChange(LineGroup line, double x, double y) {
        if (currentMode == ImageEditorStackGroup.Mode.SELECT) {
            line.setEndPoint(getCorrectedPointX(bounds, x), getCorrectedPointY(bounds, y));
        }
    }

    @Override
    public void onAngledStartPointChange(LineGroup line, double x, double y) {
        if (currentMode == ImageEditorStackGroup.Mode.SELECT) {
            double lineAngle = line.getLineAngle();
            double length = getDeltaAngledLineLength(line, x, y, lineAngle, false);
            double angledPointX = getAngledPointX(lineAngle, line.getEndPointX(), length, false);
            double angledPointY = getAngledPointY(lineAngle, line.getEndPointY(), length, false);

            line.setStartPoint(getCorrectedAngledPoint(bounds, new Point2D(angledPointX, angledPointY), lineAngle, line.getEndPointX(), line.getEndPointY()));
        }
    }

    @Override
    public void onAngledEndPointChange(LineGroup line, double x, double y) {
        if (currentMode == ImageEditorStackGroup.Mode.SELECT) {
            double lineAngle = line.getLineAngle();
            double length = getDeltaAngledLineLength(line, x, y, lineAngle, true);
            double angledPointX = getAngledPointX(lineAngle, line.getStartPointX(), length, false);
            double angledPointY = getAngledPointY(lineAngle, line.getStartPointY(), length, false);

            line.setEndPoint(getCorrectedAngledPoint(bounds, new Point2D(angledPointX, angledPointY), lineAngle, line.getStartPointX(), line.getStartPointY()));
        }
    }

    @Override
    public void onLineChange(LineGroup line, double dx, double dy) {
        if (currentMode == ImageEditorStackGroup.Mode.SELECT) {
            line.moveLineGroup(getCorrectedDx(bounds, line, dx), getCorrectedDy(bounds, line, dy));
        }
    }

    @Override
    public void onLineChange(LineGroup lineGroup) {
        if (elementListener != null) elementListener.onLineChange(lineGroup);
    }

    /**
     * Area
     **/
    @Override
    public void onVertexChange(AreaGroup area, int vertexIndex, double x, double y) {
        //TODO
    }

    @Override
    public void onAreaChanged(AreaGroup areaGroup) {
        if (elementListener != null) elementListener.onAreaChange(areaGroup);

    }

    @Override
    public void onAreaComplete(AreaGroup area) {
        currentMode = Mode.AREA_CREATION;
        elementListener.onAreaAdd(area, false);

    }

    public void setCurrentMode(Mode mode) {
        currentSelectedItemIndex = -1;
        helperLineAngle = -1;
        this.currentMode = mode;
        if (modeListener != null) modeListener.onModeChange(currentMode);
    }

    public Mode getCurrentMode(){
        return currentMode;
    }

    public void setLayerHelperLinesVisible(boolean visible) {
        for (LayerListItem item : elements) {
            if (item.getType() == LayerListItem.Type.LINE) ((LineGroup) item).setPrecisionHelpersVisible(visible);
        }
    }

    public void setColorHelperLinesVisible(boolean visible) {
        for (LayerListItem item : elements) {
            if (item.getType() == LayerListItem.Type.LINE) ((LineGroup) item).setColorHelpersVisible(visible);
        }
    }

    private double getDeltaAngledLineLength(LineGroup line, double refX, double refY, double angle, boolean startPoint) {
        double linePointX = line.getStartPointX();
        double linePointY = line.getStartPointY();
        if (!startPoint) {
            linePointX = line.getEndPointX();
            linePointY = line.getEndPointY();
        }
        double xLength = refX - linePointX;
        double yLength = refY - linePointY;

        double length = Math.sqrt((xLength * xLength) + (yLength * yLength));

        double degAngle = Math.toDegrees(angle);
        if (degAngle > 360) degAngle -= 360;
        if ((degAngle < 45 || (degAngle >= 315 && degAngle < 360)) && xLength < 0) length = -length;
        else if (degAngle >= 135 && degAngle < 225 && xLength > 0) length = -length;
        else if (degAngle >= 45 && degAngle < 135 && yLength < 0) length = -length;
        else if (degAngle >= 225 && degAngle < 315 && yLength > 0) length = -length;

        return length;
    }

    private double getCorrectedPointX(Bounds bounds, double x) {
        double endPointX = x;
        if (endPointX < 0) endPointX = 0;
        if (endPointX > bounds.getWidth()) endPointX = bounds.getWidth();
        return endPointX;
    }

    private boolean isXWithinBounds(Bounds bounds, double x) {
        return (x >= 0 && x < bounds.getWidth());
    }

    private boolean isYithinBounds(Bounds bounds, double y) {
        return (y >= 0 && y < bounds.getHeight());
    }


    private Point2D getCorrectedAngledPoint(Bounds bounds, Point2D point, double lineAngle, double startX, double startY) {
        double x = point.getX();
        double y = point.getY();
        if (x < 0) {
            double angY = getAngledPointY(lineAngle, startY, getLengthFromAngledX(lineAngle, startX, 0), false);
            if (isYithinBounds(bounds, angY)) return new Point2D(0, angY);
        }
        if (x > bounds.getWidth()) {
            double angY = getAngledPointY(lineAngle, startY, getLengthFromAngledX(lineAngle, startX, bounds.getWidth()), false);
            if (isYithinBounds(bounds, angY)) return new Point2D(bounds.getWidth(), angY);
        }
        if (y < 0) {
            double angX = getAngledPointX(lineAngle, startX, getLengthFromAngledY(lineAngle, startY, 0), false);
            if (isXWithinBounds(bounds, angX)) return new Point2D(angX, 0);

        }
        if (y > bounds.getHeight()) {
            double angX = getAngledPointX(lineAngle, startX, getLengthFromAngledY(lineAngle, startY, bounds.getHeight()), false);
            if (isXWithinBounds(bounds, angX)) return new Point2D(angX, bounds.getHeight());
        }
        return point;
    }

    private double getCorrectedPointY(Bounds bounds, double y) {
        double endPointY = y;
        if (endPointY < 0) endPointY = 0;
        if (endPointY > bounds.getHeight()) endPointY = bounds.getHeight();
        return endPointY;
    }

    private double getCorrectedDx(Bounds bounds, LineGroup line, double dx) {
        double startPointX = line.getStartPointX();
        double endPointX = line.getEndPointX();
        if (startPointX + dx < 0) return -startPointX;
        if (startPointX + dx > bounds.getWidth()) return bounds.getWidth() - startPointX;
        if (endPointX + dx < 0) return -endPointX;
        if (endPointX + dx > bounds.getWidth()) return bounds.getWidth() - endPointX;
        return dx;
    }

    private double getCorrectedDy(Bounds bounds, LineGroup line, double dy) {
        double startPointY = line.getStartPointY();
        double endPointY = line.getEndPointY();
        if (startPointY + dy < 0) return -startPointY;
        if (startPointY + dy > bounds.getHeight()) return bounds.getHeight() - startPointY;
        if (endPointY + dy < 0) return -endPointY;
        if (endPointY + dy > bounds.getHeight()) return bounds.getHeight() - endPointY;
        return dy;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int count) {
        this.lineCount = count;
    }

    private double getAngledPointX(double angle, double startPointX, double length, boolean addedAngle) {
        if (!addedAngle)
            return startPointX + length * Math.cos(angle);
        return startPointX + length * Math.cos(angle + addedLineAngle);
    }

    private double getAngledPointY(double angle, double startPointY, double length, boolean addedAngle) {
        if (!addedAngle)
            return startPointY + length * Math.sin(angle);
        return startPointY + length * Math.sin(angle + addedLineAngle);
    }

    private double getLengthFromAngledX(double angle, double startPointX, double angledPointX) {
        return (angledPointX - startPointX) / Math.cos(angle);
    }

    private double getLengthFromAngledY(double angle, double startPointY, double angledPointY) {
        return (angledPointY - startPointY) / Math.sin(angle);
    }

    public interface ModeListener {
        void onModeChange(Mode mode);
    }

    public interface ElementListener {
        void onLineAdd(LineGroup line, boolean sessionLoad);

        void onAreaAdd(AreaGroup area, boolean sessionLoad);

        void onLineChange(LineGroup lineGroup);

        void onAreaChange(AreaGroup areaGroup);
    }
}
