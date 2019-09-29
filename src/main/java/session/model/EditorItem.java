package session.model;

import equation.model.EquationItem;
import javafx.scene.Node;
import ui.custom.AreaGroup;
import ui.custom.ImageEditorStackGroup;
import ui.custom.LineGroup;
import ui.custom.ZoomableScrollPane;
import ui.model.LayerListItem;
import ui.model.ScaleRatio;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class assumes that ZoomableScrollPane always has ImageEditorStackGroup as the child.
 */

@XmlRootElement(name = "item")
public class EditorItem {
    private EditorItemZoom zoom;
    private ArrayList<EditorItemLayer> layers;

    private ScaleRatio scaleRatio;
    private String sourceImagePath;

    public EditorItem(){
        this.layers = new ArrayList<>();
        this.scaleRatio = new ScaleRatio();
        this.zoom = new EditorItemZoom();
    }

    public EditorItem(ZoomableScrollPane scrollPane, List<LayerListItem> layers){
        ImageEditorStackGroup stackGroup = (ImageEditorStackGroup) scrollPane.getTarget();
        this.zoom = new EditorItemZoom(scrollPane.getHvalue(), scrollPane.getVvalue(), scrollPane.getScaleValue());
        this.layers = convertItemLayer(layers);
        this.scaleRatio = stackGroup.getCurrentScale();
    }

    @XmlElement
    public EditorItemZoom getZoom() {
        return zoom;
    }

    public void setZoom(EditorItemZoom zoom) {
        this.zoom = zoom;
    }

    public int getLineCount(){
        return layers.size()+1;
    }

    @XmlElementWrapper
    @XmlAnyElement(lax=true)
    public ArrayList<EditorItemLayer> getLayers() {
        return layers;
    }

    @XmlElement
    public ScaleRatio getScaleRatio() {
        return scaleRatio;
    }

    public boolean hasScaleRatio(){
        return  scaleRatio != null && scaleRatio.getRatio() > 0;
    }

    public void setScaleRatio(ScaleRatio scaleRatio) {
        this.scaleRatio = scaleRatio;
    }

    @XmlElement
    public String getSourceImagePath() {
        return sourceImagePath;
    }

    public void setSourceImagePath(String sourceImagePath) {
        this.sourceImagePath = sourceImagePath;
    }

    public void updateZoomAndScale(ZoomableScrollPane scrollPane){
        ImageEditorStackGroup stackGroup = (ImageEditorStackGroup) scrollPane.getTarget();
        this.zoom = new EditorItemZoom(scrollPane.getHvalue(), scrollPane.getVvalue(), scrollPane.getScaleValue());
        this.scaleRatio = stackGroup.getCurrentScale();

    }

    private ArrayList<EditorItemLayer> convertItemLayer(List<LayerListItem> layers){
        ArrayList<EditorItemLayer> editorList = new ArrayList<>();
        for(LayerListItem item : layers){
            if(item instanceof EquationItem){
                editorList.add((EquationItem) item);
            } else if(item instanceof LineGroup){
                editorList.add(new EditorItemLine((LineGroup) item));
            } else if(item instanceof AreaGroup){
                editorList.add(new EditorItemArea((AreaGroup) item));
            }
        }
        return editorList;
    }
}
