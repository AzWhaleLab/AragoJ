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
import javafx.scene.control.*;
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

public class ScaleDialogController extends Dialog<Void> {

    private Stage stage;
    private ArrayList<SegLineGroup> lines;

    @FXML private JFXTextField trueLengthTextField;
    @FXML private JFXTextField unitsTextField;
    @FXML private Label lengthRatioLabel;
    @FXML private JFXComboBox<LabeledComboOption> pixelLengthComboBox;
    @FXML private CheckBox applyToAllCheckBox;
    @FXML private JFXButton okButton;

    private OnActionListener listener;
    private double ratio = -1;

    public void init(Window owner, OnActionListener listener,  ArrayList<SegLineGroup> lines){
        this.lines = lines;
        this.listener = listener;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fmxl/ScaleDialog.fxml"), Translator.getBundle());
            loader.setController(this);
            Parent root = loader.load();
            Scene scene = new Scene(root, 320, 205);
            final ObservableList<String> stylesheets = scene.getStylesheets();
            stylesheets.addAll(getClass().getResource("/css/MainApplication.css").toExternalForm());
            Stage stage = new Stage();
            stage.getIcons().add(new Image(MainApplication.class.getResourceAsStream("/images/icon.png")));
            stage.setTitle(Translator.getString("referenceScaling"));
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
        pixelLengthComboBox.setCellFactory(new Callback<ListView<LabeledComboOption>, ListCell<LabeledComboOption>>() {
            @Override
            public ListCell<LabeledComboOption> call(ListView<LabeledComboOption> param) {
                return new ListCell<LabeledComboOption>(){
                    @Override
                    protected void updateItem(LabeledComboOption item, boolean empty){
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.getIdentifier());
                        }
                    }

                };
            }
        });

        pixelLengthComboBox.setConverter(new StringConverter<LabeledComboOption>() {
            @Override
            public String toString(LabeledComboOption object) {
                if(object != null && object.getValue() != null){
                    return object.getValue();
                }
                return "";
            }

            @Override
            public LabeledComboOption fromString(String string) {
                return pixelLengthComboBox.getItems().stream().filter(item -> string.equals(item.getValue())).findFirst().orElse(new LabeledComboOption(string, string));
            }
        });

        // Setup ComboBox
        pixelLengthComboBox.valueProperty().addListener(new ChangeListener<LabeledComboOption>() {
            @Override
            public void changed(ObservableValue ov, LabeledComboOption oldV, LabeledComboOption newV) {
                String val = newV.getValue();
                String ident = newV.getIdentifier();
                if(!val.matches("[0-9]{1,13}(\\.[0-9]{0,3})?") && val.length() > 0){
                    //String e = val.replaceAll("[^\\d]", "");
                    String e = oldV.getValue();
                    pixelLengthComboBox.setValue(new LabeledComboOption(ident,e));
                }
                setLengthRatio();
            }
        });

        trueLengthTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!newValue.matches("[0-9]{1,13}(\\.[0-9]{0,3})?") && newValue.length() > 0){
                    trueLengthTextField.setText(oldValue);
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
            pixelLengthComboBox.getItems().add(new LabeledComboOption(line.getName() + ": " + line.getLength(), String.valueOf(line.getLength())));
        }
    }

    private void setLengthRatio(){
        if(pixelLengthComboBox.getValue() == null) return;
        String pixelValue = pixelLengthComboBox.getValue().getValue();
        String trueValue = trueLengthTextField.getText();

        if(pixelValue.matches("[0-9]{1,13}(\\.[0-9]{0,3})?") && pixelValue.length() > 0 && trueValue.matches("[0-9]{1,13}(\\.[0-9]{0,3})?") && trueValue.length() > 0) {
            double pixels = Double.parseDouble(pixelValue);
            double truevals = Double.parseDouble(trueValue);
            if(pixels > 0){
                ratio = truevals/pixels;
                lengthRatioLabel.setText("1 pixel : " + Math.round(truevals/pixels * 1000.0) / 1000.0 + " " + unitsTextField.getText());
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
            listener.onApplyScale(new ScaleRatio(ratio, unitsTextField.getText()), applyToAllCheckBox.isSelected());

        }
    }


    public interface OnActionListener{
        void onApplyScale(ScaleRatio scaleRatio, boolean allImages);
    }

}
