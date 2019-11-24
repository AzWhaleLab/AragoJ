package ui.tasks;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXSpinner;
import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import utils.Translator;

public class ProgressDialog implements ProgressTaskListener {
  @FXML Label progressPrimary;
  @FXML Label progressDescription;
  @FXML VBox container;
  @FXML JFXButton cancelButton;
  @FXML JFXSpinner spinner;
  @FXML Hyperlink alternativeBtn;

  private JFXDialog dialog;
  private ProgressTask task;

  private String alternativeText;
  private FailAlternativeListener alternativeListener;

  public void show(ProgressTask task, boolean cancellable, boolean transparent,
      StackPane stackPane) {
    FXMLLoader loader =
        new FXMLLoader(getClass().getResource("/fmxl/ProgressDialog.fxml"), Translator.getBundle());
    loader.setController(this);
    try {
      Parent root = loader.load();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    if (!cancellable) {
      cancelButton.setVisible(false);
    }
    dialog = new JFXDialog(stackPane, container, JFXDialog.DialogTransition.CENTER);
    if (transparent) {
      container.setBackground(Background.EMPTY);
      ((StackPane) container.getParent()).setBackground(Background.EMPTY);
    }
    spinner.managedProperty().bind(spinner.visibleProperty());
    alternativeBtn.managedProperty().bind(alternativeBtn.visibleProperty());
    progressDescription.managedProperty().bind(progressDescription.visibleProperty());
    progressPrimary.managedProperty().bind(progressPrimary.visibleProperty());
    dialog.setOverlayClose(false);
    dialog.show();
    setupAlternative();
    this.task = task;
    this.task.setTaskListener(this);
    this.task.startTask();
  }

  private void setupAlternative() {
    if(alternativeText != null && alternativeListener != null){
      alternativeBtn.setText(alternativeText);
      alternativeBtn.setOnAction(event -> {
        if (alternativeListener != null) {
          alternativeListener.onAlternativeClicked();
        }
      });
    }

  }

  @FXML public void onCancelClick(ActionEvent actionEvent) {
    task.cancelTask();
    dialog.close();
  }

  public void close() {
    if(dialog != null){
      task.cancelTask();
      dialog.close();
    }
  }

  @Override public void onProgressChanged(String progress, String descriptiveStatus) {
    Platform.runLater(() -> {
      progressPrimary.setText(progress);
      progressDescription.setText(descriptiveStatus);
    });
  }

  @Override public void onTaskFinished() {
    Platform.runLater(() -> {
      if (dialog != null) dialog.close();
    });
  }

  @Override public void onTaskFailed(String errorText, boolean shouldShowProgress,
      boolean shouldShowAlternative) {
    if (!shouldShowProgress) {
      spinner.setVisible(false);
    }
    if(shouldShowAlternative){
      alternativeBtn.setVisible(true);
    }
    Platform.runLater(() -> {
      progressDescription.setStyle("-fx-text-fill: red;");
      progressDescription.setText("Error: " + errorText);
      //if(dialog != null) dialog.close();
    });
  }

  public void setFailAlternative(String text, FailAlternativeListener listener) {
    alternativeText = text;
    alternativeListener = listener;
  }

  public interface FailAlternativeListener {
    void onAlternativeClicked();
  }
}
