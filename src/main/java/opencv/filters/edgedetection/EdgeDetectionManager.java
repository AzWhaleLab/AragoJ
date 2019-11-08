package opencv.filters.edgedetection;

import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.util.Duration;
import opencv.calibration.tools.OpenCVUtils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class EdgeDetectionManager {

  private Task<Void> task;
  // Debounce
  private PauseTransition pause = new PauseTransition(Duration.millis(400));

  public void applyCannyEdgeDetection(ResultListener resultListener, String imagePath, int threshold){
    if(task != null){
      task.cancel();
    }
    task = new Task<Void>() {
      @Override protected Void call() throws Exception {
        pause.setOnFinished(event -> {
          Image resultImage = applyCannyEdgeDetectionSync(imagePath, threshold);
          if(!isCancelled()){
            resultListener.onEdgeDetectionFinished(resultImage);
          }
        });
        pause.playFromStart();

        return null;
      }
    };
    new Thread(task).start();
  }

  public Image applyCannyEdgeDetectionSync(String imagePath, int threshold){
    Mat image = Imgcodecs.imread(imagePath);
    Mat result = new Mat();
    Imgproc.Canny(image, result, threshold, threshold*3);
    return OpenCVUtils.getImageFromMat(result);
  }

  public interface ResultListener {
    void onEdgeDetectionFinished(Image image);
  }
}
