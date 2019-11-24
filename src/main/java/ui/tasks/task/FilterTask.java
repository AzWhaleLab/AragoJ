package ui.tasks.task;

import java.io.File;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.util.Duration;
import opencv.filters.Filter;
import opencv.filters.FilterArguments;
import ui.tasks.ProgressTask;
import ui.tasks.ProgressTaskListener;

public class FilterTask implements ProgressTask {
  private final ResultListener listener;
  private ProgressTaskListener progressTaskListener;
  private final Filter filter;
  private final FilterArguments filterArguments;
  private final Image image;
  private final String path;

  private Task<Void> task;
  private PauseTransition pause = new PauseTransition(Duration.millis(400)); // Debounce

  public FilterTask(ResultListener resultListener, Filter filter, FilterArguments filterArguments,
      Image image, String path) {
    this.listener = resultListener;
    this.filter = filter;
    this.filterArguments = filterArguments;
    this.image = image;
    this.path = path;
  }

  @Override public void startTask() {
    if (progressTaskListener != null) {
      progressTaskListener.onProgressChanged("", "Applying filter...");
    }
    if (task != null) {
      task.cancel();
    }
    pause.setOnFinished(event -> {
      task = new Task<Void>() {
        @Override protected Void call() throws Exception {
          try {
            String resultPath = "";
            if (!path.isEmpty()) {
              String name = new File(path).getName();
              resultPath = "./tmp/" + name + "_tmp.bmp";
            }

            Image resultImage = filter.applyFilter(image, filterArguments, resultPath);
            if (!isCancelled()) {
              listener.onFilterFinished(filter, filterArguments, resultImage, resultPath);
              if (progressTaskListener != null) progressTaskListener.onTaskFinished();
            }
          } catch (FilterArguments.NoArgumentFound e) {
            listener.onFilterFailed(filter, filterArguments, image);
            if (progressTaskListener != null) {
              progressTaskListener.onTaskFailed("Filter couldn't be applied.", true, false);
            }
            e.printStackTrace();
          }
          return null;
        }

        @Override protected void failed() {
          String error = getException().toString();
          if (getException() instanceof OutOfMemoryError) {
            error = "Out of memory";
          }
          if (progressTaskListener != null) progressTaskListener.onTaskFailed(error, true, false);
        }
      };
      new Thread(task).start();
    });
    pause.playFromStart();
  }

  @Override public void cancelTask() {
    if (task != null) {
      task.cancel();
      pause.pause();
    }
    task = null;
  }

  @Override public void setTaskListener(ProgressTaskListener listener) {
    this.progressTaskListener = listener;
  }

  public interface ResultListener {
    void onFilterFinished(Filter filter, FilterArguments filterArguments, Image image,
        String resultPath);

    void onFilterFailed(Filter filter, FilterArguments filterArguments, Image image);
  }
}
