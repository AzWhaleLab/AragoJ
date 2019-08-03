package ui.cellfactory;

import com.jfoenix.controls.JFXListCell;
import com.jfoenix.svg.SVGGlyph;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import ui.controller.LayerTabPageController;
import utils.TextUtils;
import utils.Translator;

import java.io.IOException;

public class LayerListViewCellController {
    @FXML private Label image;
    @FXML private HBox hBox;
    @FXML private TextField primaryTextField;
    @FXML private Label primaryLabel;
    @FXML private Label secondaryLabel;

    private JFXListCell cell;
    private LayerListViewCell.LayerListener listener;

    private String oldPrimaryText;

    public LayerListViewCellController(JFXListCell cell, LayerListViewCell.LayerListener listener){
        this.cell = cell;

        this.listener = listener;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fmxl/LayerListViewCell.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        primaryLabel.managedProperty().bind(primaryLabel.visibleProperty());
        primaryLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        primaryTextField.managedProperty().bind(primaryTextField.visibleProperty());
        primaryTextField.setVisible(false);
        primaryTextField.setOnAction(event -> {
            renameLayer();
        });

        primaryLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton().equals(MouseButton.PRIMARY)){
                    if(event.getClickCount() == 2){
                        editNameLabel();
                    }
                }
            }
        });
        primaryTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean focused) {
                if(!focused){
                    renameLayer();
                }
            }
        });
        cell.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number width) {
                double prefWidth = TextUtils.computeTextWidth(primaryTextField.getFont(), primaryTextField.getText(), cell.getWidth()-57)+7;
                double containerWidth = width.doubleValue()-50;
                if(prefWidth > containerWidth) prefWidth = containerWidth;
                setPrimaryWidth(prefWidth);
            }
        });
        primaryTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String string) {
                double prefWidth = TextUtils.computeTextWidth(primaryTextField.getFont(), string, cell.getWidth()-57)+7;
                double width = 70;
                if(prefWidth > width) width = prefWidth;
                primaryTextField.setPrefWidth(width);
                primaryTextField.setMinWidth(width);
                primaryTextField.setMaxWidth(width);
                primaryLabel.setPrefWidth(prefWidth);
                primaryLabel.setMinWidth(prefWidth);
                primaryLabel.setMaxWidth(prefWidth);
                primaryLabel.setText(string);
            }
        });
    }

    private void editNameLabel() {
        primaryLabel.setVisible(false);
        primaryTextField.setVisible(true);
        primaryTextField.requestFocus();
    }

    private void nameAlreadyExistsError(){
        // TODO: Show error
//        Alert alert = new Alert(Alert.AlertType.ERROR, Translator.getString("nameAlreadyExists"), ButtonType.OK);
//        alert.showAndWait();

        primaryLabel.setText(oldPrimaryText);
        primaryTextField.setText(oldPrimaryText);
        editNameLabel();
        primaryTextField.selectAll();
    }

    private void renameLayer() {
        primaryLabel.setVisible(true);
        primaryTextField.setVisible(false);
        try {
            if(primaryLabel.getText().contains(",")){
                nameAlreadyExistsError();
                return;
            }
            listener.onRenameLayer(cell.getIndex(), primaryLabel.getText());
            oldPrimaryText = primaryLabel.getText();
        } catch (LayerTabPageController.NameAlreadyTaken nameAlreadyTaken) {
            nameAlreadyExistsError();
        }
    }

    public void setLabelGraphic(SVGGlyph glyph){
        image.setGraphic(glyph);
    }
    public HBox getHBox(){
        return hBox;
    }

    private void setPrimaryWidth(double width){
        primaryTextField.setPrefWidth(width);
        primaryTextField.setMinWidth(width);
        primaryTextField.setMaxWidth(width);
        primaryLabel.setPrefWidth(width);
        primaryLabel.setMinWidth(width);
        primaryLabel.setMaxWidth(width);
    }
    public void setPrimaryText(String text){
        oldPrimaryText = text;
        primaryTextField.setText(text);
        primaryLabel.setText(text);
    }
    public void setSecondaryLabel(String text){
        secondaryLabel.setText(text);
    }

}
