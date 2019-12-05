package ui.tasks.task;

import imageprocess.ImageManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javafx.concurrent.Task;
import session.model.EditorItem;
import ui.model.ImageItem;
import ui.model.UIEditorItem;
import ui.tasks.ProgressTask;
import ui.tasks.ProgressTaskListener;

public class SessionOpenTask implements ProgressTask {

  private ImageManager imageManager;
  private Task task;
  private ProgressTaskListener progressListener;
  private final List<EditorItem> itemsToLoad;
  private final ResultListener resultListener;

  public SessionOpenTask(ImageManager imageManager, List<EditorItem> itemsToLoad,
      ResultListener resultListener) {
    this.imageManager = imageManager;
    this.itemsToLoad = itemsToLoad;
    this.resultListener = resultListener;
  }

  @Override public void startTask() {
    if(progressListener != null) progressListener.onProgressChanged("", "Importing images...");
    task = new Task<List<UIEditorItem>>() {
      @Override protected List<UIEditorItem> call() throws Exception {
        ArrayList<UIEditorItem> uiEditorItems = new ArrayList<>();
        for (int i = 0; i<itemsToLoad.size(); i++) {
          EditorItem item = itemsToLoad.get(i);
          if(progressListener != null) progressListener.onProgressChanged(i+1 + "/" +itemsToLoad.size(), "Importing images...");
          File file = new File(item.getSourceImagePath());
          if (file.exists()) {
            ImageItem imageItem = new ImageItem(
                imageManager.retrieveImage(item.getOpenedWith(), file.getAbsolutePath()));
            uiEditorItems.add(new UIEditorItem(item, imageItem));
          }
        }
        return uiEditorItems;
      }

      @Override protected void succeeded() {
        try {
          List<UIEditorItem> editorItems = get();
          if(resultListener != null) resultListener.onSessionLoaded(editorItems);
          if(progressListener != null) progressListener.onTaskFinished();
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
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
    void onSessionLoaded(List<UIEditorItem> editorItems);
  }
}
