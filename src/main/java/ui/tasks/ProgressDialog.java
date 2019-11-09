package ui.tasks;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import utils.Translator;

public class ProgressDialog implements ProgressTaskListener {
  @FXML Label progressPrimary;
  @FXML Label progressDescription;
  @FXML VBox container;
  @FXML JFXButton cancelButton;

  private JFXDialog dialog;
  private ProgressTask task;

  public void show(ProgressTask task, StackPane stackPane) {
    FXMLLoader loader =
        new FXMLLoader(getClass().getResource("/fmxl/ProgressDialog.fxml"), Translator.getBundle());
    loader.setController(this);
    try {
      Parent root = loader.load();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    dialog = new JFXDialog(stackPane, container, JFXDialog.DialogTransition.CENTER);
    dialog.setOverlayClose(false);
    dialog.show();
    this.task = task;
    this.task.setTaskListener(this);
    this.task.startTask();
  }

  @FXML
  public void onCancelClick(ActionEvent actionEvent) {
    task.cancelTask();
    dialog.close();
  }

  @Override public void onProgressChanged(String progress, String descriptiveStatus) {
    Platform.runLater(() -> {
      progressPrimary.setText(progress);
      progressDescription.setText(descriptiveStatus);
    });
  }

  @Override public void onTaskFinished() {
    Platform.runLater(() -> {
      if(dialog != null) dialog.close();
    });
  }

  @Override public void onTaskFailed(String errorText) {
    Platform.runLater(() -> {
      progressDescription.setStyle("-fx-text-fill: red;");
      progressDescription.setText("Error: " + errorText);
      //if(dialog != null) dialog.close();
    });
  }
}
