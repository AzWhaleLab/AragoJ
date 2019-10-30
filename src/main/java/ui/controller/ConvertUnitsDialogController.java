package ui.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;
import ui.MainApplication;
import ui.custom.segline.SegLineGroup;
import ui.model.LabeledComboOption;
import ui.model.ScaleRatio;
import utils.Translator;

import java.io.IOException;
import java.util.ArrayList;

public class ConvertUnitsDialogController {

    private Stage stage;
    private ArrayList<SegLineGroup> lines;

    @FXML private JFXTextField unitLengthTextField;
    @FXML private JFXTextField unitsTextField;
    @FXML private Label lengthRatioLabel;
    @FXML private JFXComboBox<LabeledComboOption> lengthReferenceComboBox;
    @FXML private CheckBox applyToAllCheckBox;
    @FXML private JFXButton okButton;

    private OnActionListener listener;
    private ScaleRatio currentScale;
    private double ratio = -1;

    public void init(Window owner, OnActionListener listener, ArrayList<SegLineGroup> lines, ScaleRatio currentScale){
        this.lines = lines;
        this.listener = listener;
        this.currentScale = currentScale;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fmxl/ConvertUnitsDialog.fxml"), Translator.getBundle());
            loader.setController(this);
            Parent root = loader.load();
            Scene scene = new Scene(root, 320, 205);
            final ObservableList<String> stylesheets = scene.getStylesheets();
            stylesheets.addAll(getClass().getResource("/css/MainApplication.css").toExternalForm());
            Stage stage = new Stage();
            stage.getIcons().add(new Image(MainApplication.class.getResourceAsStream("/images/icon.png")));
            stage.setTitle(Translator.getString("convertUnits"));
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
        lengthReferenceComboBox.setCellFactory(new Callback<ListView<LabeledComboOption>, ListCell<LabeledComboOption>>() {
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

        lengthReferenceComboBox.setConverter(new StringConverter<LabeledComboOption>() {
            @Override
            public String toString(LabeledComboOption object) {
                if(object != null){
                    return object.getValue();
                }
                return "";
            }

            @Override
            public LabeledComboOption fromString(String string) {
                return lengthReferenceComboBox.getItems().stream().filter(item -> string.equals(item.getValue())).findFirst().orElse(new LabeledComboOption(string, string));
            }
        });

        // Setup ComboBox
        lengthReferenceComboBox.valueProperty().addListener(new ChangeListener<LabeledComboOption>() {
            @Override
            public void changed(ObservableValue ov, LabeledComboOption oldV, LabeledComboOption newV) {
                String val = newV.getValue();
                String ident = newV.getIdentifier();
                if(!val.matches("[0-9]{1,13}(\\.[0-9]{0,3})?") && val.length() > 0){
                    //String e = val.replaceAll("[^\\d]", "");
                    String e = oldV.getValue();
                    lengthReferenceComboBox.setValue(new LabeledComboOption(ident,e));
                }
                setLengthRatio();
            }
        });

        unitLengthTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!newValue.matches("[0-9]{1,13}(\\.[0-9]{0,3})?") && newValue.length() > 0){
                    unitLengthTextField.setText(oldValue);
                }
                setLengthRatio();
            }
        });

        unitsTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                setLengthRatio();
            }
        });

        for(SegLineGroup line : lines) {
            double value = currentScale.getScaledValue(line.getLength());
            lengthReferenceComboBox.getItems().add(new LabeledComboOption(line.getName() + ": " + value + " " + currentScale.getUnits(), String.valueOf(value)));
        }
    }

    private void setLengthRatio(){
        if(lengthReferenceComboBox.getValue() == null) return;
        String referenceValue = lengthReferenceComboBox.getValue().getValue();
        String newUnitValue = unitLengthTextField.getText();

        if(referenceValue.matches("[0-9]{1,13}(\\.[0-9]{0,3})?") && referenceValue.length() > 0 && newUnitValue.matches("[0-9]{1,13}(\\.[0-9]{0,3})?") && newUnitValue.length() > 0) {
            double refVal = Double.parseDouble(referenceValue);
            double newUnitVal = Double.parseDouble(newUnitValue);
            if(refVal > 0){
                ratio = Math.round((double) newUnitVal/refVal * 1000.0) / 1000.0;
                lengthRatioLabel.setText("1 "+ currentScale.getUnits()+ " : " + ratio + " " + unitsTextField.getText());
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
        if(ratio > -1 && unitsTextField.getText().length() > 0){
            listener.onChangeScale(currentScale, new ScaleRatio(currentScale.getRatio()*ratio, unitsTextField.getText()), applyToAllCheckBox.isSelected());
        }
    }


    public interface OnActionListener{
        void onChangeScale(ScaleRatio oldScale, ScaleRatio newScale, boolean allImages);
    }
}
