package ui.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import ui.model.ImageItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.util.StringConverter;
import ui.MainApplication;
import ui.model.LabeledComboOption;
import ui.model.ScaleRatio;
import utils.Translator;

import java.io.IOException;
import utils.Utility;

public class DistanceScaleDialogController extends Dialog<Void> {
    private Stage stage;
    private ImageItem imageItem;

    @FXML private JFXTextField sensorWidthTextField;
    @FXML private JFXComboBox<LabeledComboOption> focalLengthComboBox;
    @FXML private JFXComboBox<LabeledComboOption> distanceComboBox;
    @FXML private JFXTextField correctionFactorTextField;
    @FXML private Label lengthRatioLabel;
    @FXML private CheckBox applyToAllCheckBox;
    @FXML private JFXButton okButton;

    private ScaleDialogController.OnActionListener listener;
    private double ratio = -1;

    public void init(Window owner, ScaleDialogController.OnActionListener listener, ImageItem imageItem){
        this.imageItem = imageItem;
        this.listener = listener;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fmxl/DistanceDialog.fxml"), Translator.getBundle());
            loader.setController(this);
            Parent root = loader.load();
            Scene scene = new Scene(root, 350, 250);
            final ObservableList<String> stylesheets = scene.getStylesheets();
            stylesheets.addAll(getClass().getResource("/css/MainApplication.css").toExternalForm());
            Stage stage = new Stage();
            stage.getIcons().add(new Image(MainApplication.class.getResourceAsStream("/images/icon.png")));
            stage.setTitle(Translator.getString("ratioDfScaling"));
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
        setUpLabeledComboBox(focalLengthComboBox);
        setUpLabeledComboBox(distanceComboBox);

        sensorWidthTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!newValue.matches(Utility.getNumberAccuracyRegex()) && newValue.length() > 0){
                    sensorWidthTextField.setText(oldValue);
                }
                setLengthRatio();
            }
        });

        correctionFactorTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!newValue.matches(Utility.getNumberAccuracyRegex()) && newValue.length() > 0){
                    correctionFactorTextField.setText(oldValue);
                }
                setLengthRatio();
            }
        });

        // TODO: FAST
        //LabeledComboOption focalLengthOption = imageItem.getFocalLengthSuggestion();
        //if(focalLengthOption != null){
        //    focalLengthComboBox.getItems().addAll(imageItem.getFocalLengthSuggestion());
        //}
        //distanceComboBox.getItems().addAll(imageItem.getTargetDistanceSuggestions());
    }

    private void setUpLabeledComboBox(JFXComboBox<LabeledComboOption> comboBox){
        comboBox.setCellFactory(new Callback<ListView<LabeledComboOption>, ListCell<LabeledComboOption>>() {
            @Override
            public ListCell<LabeledComboOption> call(ListView<LabeledComboOption> param) {
                return new ListCell<LabeledComboOption>(){
                    @Override
                    protected void updateItem(LabeledComboOption item, boolean empty){
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(item.getIdentifier());
                        }
                    }
                };
            }
        });
        comboBox.setConverter(new StringConverter<LabeledComboOption>() {
            @Override
            public String toString(LabeledComboOption object) {
                if(object != null){
                    return object.getValue();
                }
                return "";
            }

            @Override
            public LabeledComboOption fromString(String string) {
                return comboBox.getItems().stream().filter(item -> string.equals(item.getValue())).findFirst().orElse(new LabeledComboOption(string, string));
            }
        });

        comboBox.valueProperty().addListener(new ChangeListener<LabeledComboOption>() {
            @Override
            public void changed(ObservableValue ov, LabeledComboOption oldV, LabeledComboOption newV) {
                String val = newV.getValue();
                String ident = newV.getIdentifier();
                if(!val.matches(Utility.getNumberAccuracyRegex()) && val.length() > 0){
                    //String e = val.replaceAll("[^\\d]", "");
                    String e = oldV.getValue();
                    comboBox.setValue(new LabeledComboOption(ident,e));
                }
                setLengthRatio();
            }
        });
    }

    private void setLengthRatio() {
        //TODO
        if(distanceComboBox.getValue() == null || focalLengthComboBox.getValue() == null) return;
        String distanceValue = distanceComboBox.getValue().getValue();
        String focalLengthValue = focalLengthComboBox.getValue().getValue();
        String sensorWidthValue = sensorWidthTextField.getText();
        String correctionFactorValue = correctionFactorTextField.getText();
        if(correctionFactorValue.isEmpty()){
            correctionFactorValue = "0";
        }

        if(distanceValue.matches(Utility.getNumberAccuracyRegex()) && distanceValue.length() > 0
                && focalLengthValue.matches(Utility.getNumberAccuracyRegex()) && focalLengthValue.length() > 0
                && sensorWidthValue.matches(Utility.getNumberAccuracyRegex()) && sensorWidthValue.length() > 0
                && correctionFactorValue.matches(Utility.getNumberAccuracyRegex()) && correctionFactorValue.length() > 0) {
            double distance = Double.parseDouble(distanceValue);
            double focalLength = Double.parseDouble(focalLengthValue);
            double sensorWidth = Double.parseDouble(sensorWidthValue);
            double correctionFactor = Double.parseDouble(correctionFactorValue);
            distance += correctionFactor;
            if(focalLength > 0 && distance > 0 && sensorWidth > 0){
                ratio = (sensorWidth*distance*100.0) / (focalLength * imageItem.getWidth());
                lengthRatioLabel.setText("1 pixel : " +  Math.round(ratio * 1000.0) / 1000.0 + " cm");
                okButton.setDisable(false);
            }
            else{
                ratio = -1;
                lengthRatioLabel.setText("Not set");
                okButton.setDisable(true);
            }
        }
        else{
            ratio = -1;
            lengthRatioLabel.setText("Not set");
            okButton.setDisable(true);
        }
    }

    public void onCloseAction(ActionEvent actionEvent) {
        stage.close();
    }

    public void onOkAction(ActionEvent actionEvent) {
        stage.close();
        if (ratio > -1) {
            listener.onApplyScale(new ScaleRatio(ratio, "cm"), applyToAllCheckBox.isSelected());
        }
    }
}
