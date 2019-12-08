package opencv.calibration.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import java.nio.file.Files;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import opencv.calibration.file.CalibrationFileManager;
import opencv.calibration.model.CalibrationConfig;
import opencv.calibration.model.CalibrationModel;
import ui.MainApplication;
import utils.Constants;
import utils.Translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

public class UndistortDialog {

    private Stage stage;
    private Preferences prefs;

    private OnActionListener listener;

    @FXML private JFXComboBox<CalibrationModel> calibrationComboBox;
    @FXML private JFXButton importCalibrationButton;
    @FXML private CheckBox applyToAllCheckBox;
    @FXML private JFXButton okButton;

    public void init(Window owner, OnActionListener listener){
        this.listener = listener;
        this.prefs = Preferences.userNodeForPackage(CalibrationConfig.class);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fmxl/UndistortDialog.fxml"), Translator.getBundle());
            loader.setController(this);
            Parent root = loader.load();
            Scene scene = new Scene(root, 330, 125);
            final ObservableList<String> stylesheets = scene.getStylesheets();
            stylesheets.addAll(getClass().getResource("/css/Calibration.css").toExternalForm());
            Stage stage = new Stage();
            stage.getIcons().add(new Image(MainApplication.class.getResourceAsStream("/images/icon.png")));
            stage.setTitle(Translator.getString("undistortDots"));
            stage.setScene(scene);
            stage.initOwner(owner);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            this.stage = stage;
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        calibrationComboBox.setCellFactory(new Callback<ListView<CalibrationModel>, ListCell<CalibrationModel>>() {
            @Override
            public ListCell<CalibrationModel> call(ListView<CalibrationModel> param) {
                return new ListCell<CalibrationModel>(){
                    @Override
                    protected void updateItem(CalibrationModel item, boolean empty){
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }

                };
            }
        });

        calibrationComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CalibrationModel>() {
            @Override
            public void changed(ObservableValue<? extends CalibrationModel> observable, CalibrationModel oldValue, CalibrationModel newValue) {
                if(newValue != null){
                    okButton.setDisable(false);
                }
            }
        });

        okButton.setDisable(true);
        checkCalibrationFolder();
    }

    public void checkCalibrationFolder(){
        File folder = new File("./calibs");
        if(!folder.exists() || !folder.isDirectory()) return;
        File[] directoryListing = folder.listFiles();
        if(directoryListing == null) return;

        for(File file : directoryListing){
            int i = file.getName().lastIndexOf('.');
            if (i > 0) {
                String extension = file.getName().substring(i+1);
                if(extension.equalsIgnoreCase("acalib")){
                    try {
                        calibrationComboBox.getItems().add(CalibrationFileManager.getCalibration(file));
                    } catch (FileNotFoundException e) {
                        // Do nothing
                    }
                }
            }
        }
        List<CalibrationModel> modelList = calibrationComboBox.getItems();
        String selectedCalib = prefs.get(Constants.CALIB_SELECTED_FILE, null);
        if(modelList.size() > 0){
            calibrationComboBox.getSelectionModel().select(0);
            if(selectedCalib != null){
                for(int i = 0; i<modelList.size(); i++){
                    CalibrationModel model = modelList.get(i);
                    if(model.getName().equals(selectedCalib)){
                        calibrationComboBox.getSelectionModel().select(i);
                        break;
                    }
                }
            }


        }
    }

    public void onImportCalibration(ActionEvent actionEvent) {
        FileChooser ch = new FileChooser();
        String path = prefs.get(Constants.FILECHOOSER_CALIB_IMPORT_CALIB, "");
        if (path.length() > 0) {
            File folder = new File(path);
            if(folder.exists() && folder.isDirectory()){
                ch.setInitialDirectory(new File(path));
            }
        }
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter(Translator.getString("acalibfilechooser"), "*.acalib"));
        ch.setTitle(Translator.getString("importCalibration"));

        File file = ch.showOpenDialog(okButton.getScene().getWindow());
        if (file != null && file.exists()) {
            prefs.put(Constants.FILECHOOSER_CALIB_IMPORT_CALIB, file.getParent());
            try {
                CalibrationModel model = CalibrationFileManager.getCalibration(file);
                calibrationComboBox.getItems().addAll(model);
                calibrationComboBox.setValue(model);
                copyToCalibs(file);
            } catch (FileNotFoundException e) {
                //TODO
                e.printStackTrace();
            }
        }
    }

    private void copyToCalibs(File file) {
        File dest = new File("calibs\\" + file.getName());
        try {
            Files.copy(file.toPath(), dest.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onOkAction(ActionEvent actionEvent) {
        stage.close();
        CalibrationModel model = calibrationComboBox.getValue();
        if(model != null){
            prefs.put(Constants.CALIB_SELECTED_FILE, model.getName());
            listener.onApplyUndistort(model, applyToAllCheckBox.isSelected());
        }
    }

    public void onCloseAction(ActionEvent actionEvent) {
        stage.close();
    }


    public interface OnActionListener{
        void onApplyUndistort(CalibrationModel calibrationModel, boolean applyToAll);
    }
}
