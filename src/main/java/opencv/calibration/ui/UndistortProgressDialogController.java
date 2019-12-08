package opencv.calibration.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.event.ActionEvent;
import ui.model.ImageItem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import opencv.calibration.model.CalibrationModel;
import opencv.calibration.tools.undistort.UndistortManager;

import java.util.List;

public class UndistortProgressDialogController implements UndistortManager.UndistortRunListener {

    @FXML Label progressPrimary;
    @FXML Label progressDescription;
    @FXML VBox container;
    @FXML JFXButton cancelButton;

    private JFXDialog dialog;
    private UndistortCallback progressListener;
    private UndistortManager undistortManager;
    private int selectedIndex;
    private boolean applyToAll = false;

    public UndistortProgressDialogController(UndistortCallback progressListener){
        this.progressListener = progressListener;
    }

    @FXML
    private void initialize(){
        cancelButton.setVisible(false);
    }

    @Override
    public void onProgress(int currentImage, int totalImages, String description) {
        Platform.runLater(() -> {
            progressPrimary.setText(currentImage + "/" + totalImages);
            progressDescription.setText(description);
        });
    }

    @Override
    public void onImageUndistorted(int currentImage, String savePath) {
        Platform.runLater(() -> {
            if(!applyToAll){
                progressListener.onImageItemUndistorted(selectedIndex, savePath, selectedIndex == currentImage);
            } else {
                progressListener.onImageItemUndistorted(currentImage, savePath, selectedIndex == currentImage);
            }
        });
    }

    @Override
    public void onFinish() {
        Platform.runLater(() -> {
            if(dialog != null) dialog.close();
        });
    }

    @FXML
    public void onCancelClick(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            if(dialog != null) dialog.close();
        });
    }

    public void undistortImages(CalibrationModel model, List<ImageItem> imageItemList, int selectedIndex, StackPane stackPane) {
        applyToAll = imageItemList.size() > 1;
        dialog = new JFXDialog(stackPane, container, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false);
        dialog.show();
        this.selectedIndex = selectedIndex;
        undistortManager = new UndistortManager(UndistortProgressDialogController.this);
        undistortManager.undistortImages(model, imageItemList);

    }

    public interface UndistortCallback{
        void onImageItemUndistorted(int index, String newPath, boolean select);
    }
}
