package com.aragoj.opencv.calibration.ui;

import com.aragoj.session.model.EditorItemZoom;
import com.aragoj.ui.custom.ZoomableScrollPane;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "item")
public class CalibrationImageItem {
    private EditorItemZoom zoom;
    private String sourceImagePath;

    public CalibrationImageItem(ZoomableScrollPane scrollPane){
        this.zoom = new EditorItemZoom(scrollPane.getHvalue(), scrollPane.getVvalue(), scrollPane.getScaleValue());
    }

    @XmlElement
    public EditorItemZoom getZoom() {
        return zoom;
    }

    public void setZoom(EditorItemZoom zoom) {
        this.zoom = zoom;
    }


    @XmlElement
    public String getSourceImagePath() {
        return sourceImagePath;
    }

    public void setSourceImagePath(String sourceImagePath) {
        this.sourceImagePath = sourceImagePath;
    }


//    private ArrayList<EditorItemLayer> convertItemLayer(List<LayerListItem> layers){
//        ArrayList<EditorItemLayer> editorList = new ArrayList<>();
//        for(LayerListItem item : layers){
//            if(item instanceof EquationItem){
//                editorList.add((EquationItem) item);
//            } else if(item instanceof LineGroup){
//                editorList.add(new EditorItemLine((LineGroup) item));
//            }
//        }
//        return editorList;
//    }
}
