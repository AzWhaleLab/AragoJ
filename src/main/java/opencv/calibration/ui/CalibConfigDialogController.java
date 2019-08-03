package opencv.calibration.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import opencv.calibration.model.CalibrationConfig;
import opencv.calibration.ui.custom.JFXRadioButtonSkin;
import ui.MainApplication;
import utils.Constants;
import utils.Translator;

import java.io.IOException;
import java.util.function.UnaryOperator;
import java.util.prefs.Preferences;

public class CalibConfigDialogController {

    private Stage stage;
    private Preferences prefs;

    @FXML private JFXComboBox<CalibrationConfig.Pattern> patternComboBox;
    @FXML private JFXTextField hrzPointsTextField;
    @FXML private JFXTextField vertPointsTextField;
    @FXML private JFXTextField pointSizeTextField;
    @FXML private ToggleGroup lensToggleGroup;
    @FXML private JFXRadioButton rectlinearButton;
    @FXML private JFXRadioButton fisheyeButton;

    @FXML private JFXButton okButton;

    private OnActionListener listener;

    public void init(Window owner, OnActionListener listener){
        this.listener = listener;
        this.prefs = Preferences.userNodeForPackage(CalibrationConfig.class);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fmxl/CalibrationConfigDialog.fxml"), Translator.getBundle());
            loader.setController(this);
            Parent root = loader.load();
            Scene scene = new Scene(root, 285, 205);
            final ObservableList<String> stylesheets = scene.getStylesheets();
            stylesheets.addAll(getClass().getResource("/css/Calibration.css").toExternalForm());
            Stage stage = new Stage();
            stage.getIcons().add(new Image(MainApplication.class.getResourceAsStream("/images/icon.png")));
            stage.setTitle(Translator.getString("calibrationConfiguration"));
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
    private void initialize(){
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?([1-9][0-9]*)?")) {
                return change;
            }
            return null;
        };

        lensToggleGroup.selectToggle(rectlinearButton);
        rectlinearButton.setSkin(new JFXRadioButtonSkin(rectlinearButton));
        fisheyeButton.setSkin(new JFXRadioButtonSkin(fisheyeButton));

        hrzPointsTextField.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), 11, integerFilter));

        vertPointsTextField.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), 7, integerFilter));

        patternComboBox.setCellFactory(new Callback<ListView<CalibrationConfig.Pattern>, ListCell<CalibrationConfig.Pattern>>() {
            @Override
            public ListCell<CalibrationConfig.Pattern> call(ListView<CalibrationConfig.Pattern> param) {
                return new ListCell<CalibrationConfig.Pattern>(){
                    @Override
                    protected void updateItem(CalibrationConfig.Pattern item, boolean empty){
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.toString());
                        }
                    }

                };
            }
        });

        String sPattern = prefs.get(Constants.CALIB_PATTERN, CalibrationConfig.Pattern.CHESSBOARD.toString());
        CalibrationConfig.Pattern pattern = CalibrationConfig.Pattern.CHESSBOARD;
        if(sPattern.equalsIgnoreCase(CalibrationConfig.Pattern.CIRCLE_GRID.toString())){
            pattern = CalibrationConfig.Pattern.CIRCLE_GRID;
        }
        int hrzPoints = prefs.getInt(Constants.CALIB_HRZPOINTS, 11);
        int vertPoints = prefs.getInt(Constants.CALIB_VERTPOINTS, 7);
        String toggle = prefs.get(Constants.CALIB_LENS, "RECTILINEAR");
        if(toggle.equals(fisheyeButton.getUserData())){
            lensToggleGroup.selectToggle(fisheyeButton);
        } else {
            lensToggleGroup.selectToggle(rectlinearButton);
        }

        patternComboBox.getSelectionModel().select(pattern);
        hrzPointsTextField.setText(String.valueOf(hrzPoints));
        vertPointsTextField.setText(String.valueOf(vertPoints));

        patternComboBox.getItems().addAll(CalibrationConfig.Pattern.CHESSBOARD, CalibrationConfig.Pattern.CIRCLE_GRID);
        patternComboBox.getSelectionModel().selectFirst();
    }

    public void onOkAction(ActionEvent actionEvent) {
        stage.close();
        CalibrationConfig.Pattern pattern = patternComboBox.getValue();
        int hrzPoints = Integer.parseInt(hrzPointsTextField.getText());
        int vertPoints = Integer.parseInt(vertPointsTextField.getText());
        int pointSize = Integer.parseInt(pointSizeTextField.getText());
        String lens = (String) lensToggleGroup.getSelectedToggle().getUserData();

        prefs.put(Constants.CALIB_PATTERN, pattern.toString());
        prefs.putInt(Constants.CALIB_HRZPOINTS, hrzPoints);
        prefs.putInt(Constants.CALIB_VERTPOINTS, vertPoints);
        prefs.put(Constants.CALIB_LENS, lens);

        listener.onConfigurationSave(new CalibrationConfig(pattern, hrzPoints, vertPoints, pointSize, CalibrationConfig.Lens.fromString(lens)));
    }

    public void onCloseAction(ActionEvent actionEvent) {
        stage.close();
    }

    public interface OnActionListener{
        void onConfigurationSave(CalibrationConfig calibrationConfig);
    }
}
