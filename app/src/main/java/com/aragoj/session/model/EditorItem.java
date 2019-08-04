package com.aragoj.session.model;

import com.aragoj.equation.model.EquationItem;
import javafx.scene.Node;
import com.aragoj.ui.custom.AreaGroup;
import com.aragoj.ui.custom.ImageEditorStackGroup;
import com.aragoj.ui.custom.LineGroup;
import com.aragoj.ui.custom.ZoomableScrollPane;
import com.aragoj.ui.model.LayerListItem;
import com.aragoj.ui.model.ScaleRatio;

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

//    public EditorItem(ZoomableScrollPane scrollPane){
//        zoom = new EditorItemZoom(scrollPane.getHvalue(), scrollPane.getVvalue(), scrollPane.getScaleValue());
//        layers = new ArrayList<>();
//
//        ImageEditorStackGroup stackGroup = (ImageEditorStackGroup) scrollPane.getTarget();
//        lineCount = stackGroup.getLineCount();
//        for(Node node : stackGroup.getChildren()){
//            if(node instanceof LineGroup){
//                LineGroup lineGroup = (LineGroup) node;
//                layers.add(new EditorItemLine(lineGroup.getName(), lineGroup.getStartPointX(), lineGroup.getStartPointY(), lineGroup.getEndPointX(), lineGroup.getEndPointY(), lineGroup.getColorObj()));
//            }
//        }
//        this.scaleRatio = stackGroup.getCurrentScale();
//    }

    public EditorItem(){
        this.layers = new ArrayList<>();
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
