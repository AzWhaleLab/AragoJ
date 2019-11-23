package imageprocess;

import com.aragoj.plugins.imagereader.metadata.Metadata;
import javafx.scene.image.Image;

public class AJImageCalibration extends AJImage {

  private String statusColor;

  public AJImageCalibration(String name, String path, int width, int height, Metadata metadata, Image thumbnail, String statusColor) {
    super(name, path, width, height, metadata, thumbnail);
    this.statusColor = statusColor;
  }

  public String getStatusColor() {
    return statusColor;
  }
}
