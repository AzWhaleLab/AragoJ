package ui.tasks.task;

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

  private Task<Void> task;
  private PauseTransition pause = new PauseTransition(Duration.millis(400)); // Debounce

  public FilterTask(ResultListener resultListener, Filter filter, FilterArguments filterArguments, Image image){
    this.listener = resultListener;
    this.filter = filter;
    this.filterArguments = filterArguments;
    this.image = image;
  }

  @Override public void startTask() {
    if(progressTaskListener != null) progressTaskListener.onProgressChanged("", "Applying filter...");
    if(task != null){
      task.cancel();
    }
    pause.setOnFinished(event -> {
      task = new Task<Void>() {
        @Override protected Void call() throws Exception {
          try {
            Image resultImage  = filter.applyFilter(image, filterArguments);
            if(!isCancelled()){
              listener.onFilterFinished(filter, filterArguments, resultImage);
              if(progressTaskListener != null) progressTaskListener.onTaskFinished();
            }
          } catch (FilterArguments.NoArgumentFound e) {
            listener.onFilterFailed(filter, filterArguments, image);
            if(progressTaskListener != null) progressTaskListener.onTaskFailed("Filter couldn't be applied.");
            e.printStackTrace();
          }
          return null;
        }

        @Override protected void failed() {
          String error = getException().toString();
          if(getException() instanceof OutOfMemoryError){
            error = "Out of memory";
          }
          if(progressTaskListener != null) progressTaskListener.onTaskFailed(error);
        }
      };
      new Thread(task).start();
    });
    pause.playFromStart();
  }

  @Override public void cancelTask() {
    if(task!= null){
      task.cancel();
      pause.pause();
    }
    task = null;
  }

  @Override public void setTaskListener(ProgressTaskListener listener) {
    this.progressTaskListener = listener;
  }

  public interface ResultListener{
    void onFilterFinished(Filter filter, FilterArguments filterArguments, Image image);
    void onFilterFailed(Filter filter, FilterArguments filterArguments, Image image);
  }
}
