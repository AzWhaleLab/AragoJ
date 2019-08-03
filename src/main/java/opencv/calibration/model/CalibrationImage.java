package opencv.calibration.model;

import javafx.scene.image.Image;

public class CalibrationImage {
    private Image image;
    private boolean found;

    public CalibrationImage(Image image, boolean found) {
        this.image = image;
        this.found = found;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }
}
