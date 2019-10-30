package session.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import ui.custom.segline.SegLineGroup;

@XmlRootElement(name = "segline")
public class EditorItemSegLine implements EditorItemLayer{
  private String name;
  private String color;
  private List<EditorItemPosition> points;

  public EditorItemSegLine(){
    points = new ArrayList<>();
  }

  public EditorItemSegLine(SegLineGroup segLineGroup){
    this.name = segLineGroup.getPrimaryText();
    this.color = segLineGroup.getColorString();
    this.points = segLineGroup.getExportablePoints();
  }

  @XmlElement(name="name")
  public String getName(){
    return name;
  }

  @XmlElementWrapper
  @XmlAnyElement(lax=true)
  public List<EditorItemPosition> getPoints(){
    return points;
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

  public void setPoints(List<EditorItemPosition> points) {
    this.points = points;
  }

  @XmlTransient
  @Override public String getIdentifier() {
    return name;
  }

  @Override public void setIdentifier(String name) {
    this.name = name;
  }
}
