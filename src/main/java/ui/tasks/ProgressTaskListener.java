package ui.tasks;

public interface ProgressTaskListener {
  void onProgressChanged(String progress, String descriptiveStatus);
  void onTaskFinished();
  void onTaskFailed(String errorMessage);
}
