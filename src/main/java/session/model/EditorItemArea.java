package session.model;

import ui.custom.AreaGroup;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "area")
@XmlType(propOrder = {"name", "vertices", "color"})
public class EditorItemArea implements EditorItemLayer {

    private String name;
    private String color;
    private List<EditorItemPosition> vertices;

    public EditorItemArea(){
        vertices = new ArrayList<>();
    }

    public EditorItemArea(AreaGroup areaGroup){
        this.name = areaGroup.getPrimaryText();
        this.color = areaGroup.getColorString();
        this.vertices = areaGroup.getExportableVertices();
    }

    @XmlElement(name="name")
    public String getName(){
        return name;
    }

    @XmlElementWrapper
    @XmlAnyElement(lax=true)
    public List<EditorItemPosition> getVertices(){
        return vertices;
    }

    @XmlElement(name="color")
    public String getColor() {
        return color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setVertices(List<EditorItemPosition> vertices) {
        this.vertices = vertices;
    }

    @XmlTransient
    @Override public String getIdentifier() {
        return name;
    }

    @Override public void setIdentifier(String name) {
        this.name = name;
    }
}
