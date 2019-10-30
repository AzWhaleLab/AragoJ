package session.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import ui.custom.angle.AngleGroup;
import ui.custom.segline.SegLineGroup;

@XmlRootElement(name = "angle")
@XmlType(propOrder = {"name", "points"})
public class EditorItemAngle implements EditorItemLayer{
  private String name;
  private List<EditorItemPosition> points;

  public EditorItemAngle(){
    points = new ArrayList<>();
  }

  public EditorItemAngle(AngleGroup angleGroup){
    this.name = angleGroup.getPrimaryText();
    this.points = angleGroup.getExportablePoints();
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

  public void setName(String name) {
    this.name = name;
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
