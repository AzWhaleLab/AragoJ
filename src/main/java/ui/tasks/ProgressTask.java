package ui.tasks;

/**
 * Denotes a task that should be done asynchronously and a cancellable progress dialog should be
 * shown.
 *
 * @see ProgressDialog
 */
public interface ProgressTask {
  void startTask();

  void cancelTask();

  void setTaskListener(ProgressTaskListener listener);
}
