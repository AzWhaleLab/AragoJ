package ui.tasks.task;

import imageprocess.ImageManager;
import java.io.FileNotFoundException;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import ui.tasks.ProgressTask;
import ui.tasks.ProgressTaskListener;

public class LoadImageTask implements ProgressTask {
  private Task task;

  private ImageManager imageManager;
  private String path;
  private int preferredSourceId;
  private final ResultListener resultListener;
  private ProgressTaskListener progressListener;

  public LoadImageTask(int preferredSourceId, String path, ResultListener resultListener,
      ImageManager imageManager) {
    this.path = path;
    this.preferredSourceId = preferredSourceId;
    this.resultListener = resultListener;
    this.imageManager = imageManager;
  }

  @Override public void startTask() {
    if (progressListener != null) progressListener.onProgressChanged("", "");
    task = new Task<Void>() {
      @Override protected Void call() throws Exception {
        if (resultListener != null) {
          resultListener.onImageLoaded(imageManager.loadImage(preferredSourceId, path));
        }
        if (progressListener != null) progressListener.onTaskFinished();
        return null;
      }

      @Override protected void failed() {
        String error = getException().toString();
        boolean shouldShowAlternative = false;
        if (getException() instanceof OutOfMemoryError) {
          error = "Out of memory.";
        } else if(getException() instanceof FileNotFoundException){
          error = "File not found.";
          shouldShowAlternative = true;
        }
        if (progressListener != null) progressListener.onTaskFailed(error, false, shouldShowAlternative);
        if(resultListener != null) resultListener.onImageLoadFail();
      }
    };
    new Thread(task).start();
  }

  @Override public void cancelTask() {
    if (task != null) {
      task.cancel();
    }
  }

  @Override public void setTaskListener(ProgressTaskListener listener) {
    this.progressListener = listener;
  }

  public interface ResultListener {
    void onImageLoaded(Image image);
    void onImageLoadFail();
  }
}
