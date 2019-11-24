package ui.tasks.task;

import ui.model.ImageItem;
import imageprocess.ImageManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.concurrent.Task;
import ui.tasks.ProgressTask;
import ui.tasks.ProgressTaskListener;

public class ImageImporterTask implements ProgressTask {

  private Task task;

  private int pluginId = -1;
  private final List<File> files;
  private final ResultListener resultListener;
  private ProgressTaskListener progressListener;
  private ImageManager imageManager;

  public ImageImporterTask(ImageManager imageManager, List<File> files, int pluginId, ResultListener resultListener){
    this.imageManager = imageManager;
    this.files = files;
    this.resultListener = resultListener;
    this.pluginId = pluginId;
  }

  @Override public void startTask() {
    if(progressListener != null) progressListener.onProgressChanged("", "Importing images...");
    task = new Task<Void>() {
      @Override protected Void call() throws Exception {
        ArrayList<ImageItem> items = new ArrayList<>();
        for(int i = 0; i<files.size(); i++){
          if(progressListener != null) progressListener.onProgressChanged(i+1 + "/" +files.size(), "Importing images...");

          File file = files.get(i);
          if (file.exists()) {
            try {
              ImageItem item = new ImageItem(imageManager.retrieveImage(pluginId, file.getAbsolutePath()));
              items.add(item);
            } catch (Exception e){
              e.printStackTrace();
            }
          }
        }
        if(resultListener != null) resultListener.onImagesImported(items);
        if(progressListener != null) progressListener.onTaskFinished();
        return null;
      }

      @Override protected void failed() {
        String error = getException().toString();
        if(getException() instanceof OutOfMemoryError){
          error = "Out of memory";
        }
        getException().printStackTrace();
        if(progressListener != null) progressListener.onTaskFailed(error, true, false);
      }
    };
    new Thread(task).start();
  }

  @Override public void cancelTask() {
    if(task != null){
      task.cancel();
    }
  }

  @Override public void setTaskListener(ProgressTaskListener listener) {
    this.progressListener = listener;
  }

  public interface ResultListener{
    void onImagesImported(List<ImageItem> imageItems);
  }
}
