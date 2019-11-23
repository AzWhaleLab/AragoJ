package imageprocess;

import com.aragoj.plugins.imagereader.metadata.Metadata;
import javafx.scene.image.Image;

public class AJImage {
  private int openerSourceId = -1;

  private String name;
  private String path;

  private int width;
  private int height;

  private Metadata metadata;

  private Image thumbnail;

  public AJImage(String name, String path, int width, int height, Metadata metadata, Image thumbnail) {
    this.name = name;
    this.path = path;
    this.width = width;
    this.height = height;
    this.metadata = metadata;
    this.thumbnail = thumbnail;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public void setThumbnail(Image thumbnail) {
    this.thumbnail = thumbnail;
  }

  public Image getThumbnail() {
    return thumbnail;
  }

  public int getOpenerSourceId() {
    return openerSourceId;
  }

  public void setOpenerSourceId(int openerSourceId) {
    this.openerSourceId = openerSourceId;
  }
}
