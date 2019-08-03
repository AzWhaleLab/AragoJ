package ui.controller;

import com.jfoenix.controls.JFXListView;
import equation.model.EquationItem;
import equation.ui.EquationDialogController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import ui.cellfactory.LayerListViewCell;
import ui.custom.LineGroup;
import ui.model.LayerListItem;
import ui.model.ScaleRatio;
import utils.Translator;

import java.util.ArrayList;
import java.util.List;

public class LayerTabPageController implements EquationDialogController.OnActionListener, LayerListViewCell.LayerListener {
    @FXML private JFXListView<LayerListItem> layerListView;
    private LineChangeListener listener;
    private ScaleRatio currentScale;

    @FXML
    private void initialize(){
        layerListView.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.DELETE){
                if(listener != null){
                    removeSelectedLine();
                }
            }
        });
        setContextMenu();
        setupCellFactory();
    }

    private void setupCellFactory() {
        layerListView.setCellFactory(param -> {
            LayerListViewCell cell = new LayerListViewCell(LayerTabPageController.this, currentScale);
            cell.setPadding(new Insets(1, 4, 1, 4));
            return cell;
        });
    }

    private void removeSelectedLine() {
        int index = layerListView.getSelectionModel().getSelectedIndex();
        if(layerListView.getSelectionModel().getSelectedItem() instanceof LineGroup){
            listener.onRemoveLine(((LineGroup) layerListView.getSelectionModel().getSelectedItem()).getName());
        }
        layerListView.getItems().remove(index);
    }

    private void setContextMenu() {
        ContextMenu listItemContextMenu = new ContextMenu();
        MenuItem removeContextItem = new MenuItem(Translator.getString("delete"));

        removeContextItem.setOnAction(event -> {
            removeSelectedLine();
        });
        listItemContextMenu.getItems().setAll(removeContextItem);
        layerListView.setContextMenu(listItemContextMenu);
    }

    public void clearList(){
        layerListView.getItems().clear();
    }
    public void refreshList(){
        layerListView.refresh();
    }
    public void addLayer(LayerListItem item){
        layerListView.getItems().add(item);
    }

    public void setListener(LineChangeListener listener){
        this.listener = listener;
    }

    public void setCurrentScale(ScaleRatio currentScale) {
        this.currentScale = currentScale;
        setupCellFactory();
    }


    /**
     * Cell editing listeners
     */

    @Override
    public void onRenameLayer(int index, String name) throws NameAlreadyTaken {
        List<LayerListItem> layerItems = layerListView.getItems();
        for(int i = 0; i<layerItems.size() ; i++){
            LayerListItem line = layerItems.get(i);
            if(i != index && line.getPrimaryText().equals(name)){
                throw new NameAlreadyTaken();
            }
        }
        LayerListItem item = layerListView.getItems().get(index);
        if(item instanceof LineGroup){
            LineGroup line = (LineGroup) item;
            listener.onRenameLine(line.getName(), name);
            line.setName(name);
        }
        else if(item instanceof EquationItem){
            ((EquationItem) item).setName(name);
        }
    }


    /**
     * Listener interface to be notified when a layer changes
     */
    public interface LineChangeListener{
        void onRemoveLine(String name);
        void onRenameLine(String oldName, String newName);
    }

    /**
     * ****************
     * Button listeners
     * ****************
     */
    public void onAddBtnClick(ActionEvent actionEvent) {
        EquationDialogController equationDialog = new EquationDialogController();
        equationDialog.init(layerListView.getScene().getWindow(), this, getLines(), currentScale);
    }

    public void onRemoveBtnLayer(ActionEvent actionEvent) {
        removeSelectedLine();
    }

    private List<LineGroup> getLines(){
        ArrayList<LineGroup> lines = new ArrayList<>();
        List<LayerListItem> items = layerListView.getItems();
        for(LayerListItem item : items){
            if(item instanceof LineGroup){
                lines.add((LineGroup) item);
            }
        }
        return lines;
    }

    public List<LayerListItem> getLayers(){
        List<LayerListItem> layers = layerListView.getItems();
        if(layers == null) return new ArrayList<>();
        return layers;
    }


    /**
     * Listener interface to be notified when an equation is added
     */

    @Override
    public void onAddEquation(EquationItem equation) {
        layerListView.getItems().add(equation);
    }

    public class NameAlreadyTaken extends Exception{
        public NameAlreadyTaken() {}
    }

}
