package ui.controller;

import com.jfoenix.controls.JFXListView;
import equation.model.EquationItem;
import equation.ui.EquationDialogController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import ui.cellfactory.LayerListViewCell;
import ui.custom.segline.SegLineGroup;
import ui.model.LayerListItem;
import ui.model.ScaleRatio;
import utils.Translator;

public class LayerTabPageController
    implements EquationDialogController.OnActionListener, LayerListViewCell.LayerListener {
  @FXML private JFXListView<LayerListItem> layerListView;
  private LayerChangeListener listener;
  private ScaleRatio currentScale;

  private LayerListItem currentSelected;

  @FXML private void initialize() {
    layerListView.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.DELETE) {
        if (listener != null) {
          removeSelectedLayer();
        }
      }
    });
    setupCellFactory();
    layerListView.setOnMousePressed(event -> {
      if (currentSelected != null && listener != null) {
        listener.onLayersSelected(Collections.singletonList(layerListView.getSelectionModel()
            .getSelectedItem()));
      }
    });
    layerListView.getSelectionModel()
        .selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {
          currentSelected = newValue;
          if(currentSelected != null){
            setContextMenu();
          } else {
            layerListView.setContextMenu(null);
          }
        });
  }

  private void setupCellFactory() {
    layerListView.setCellFactory(param -> {
      LayerListViewCell cell = new LayerListViewCell(LayerTabPageController.this, currentScale);
      cell.setPadding(new Insets(1, 4, 1, 4));
      return cell;
    });
  }

  private void removeSelectedLayer() {
    int index = layerListView.getSelectionModel()
        .getSelectedIndex();
    listener.onRemoveLayer(layerListView.getItems()
        .get(index)
        .getPrimaryText());
    layerListView.getItems()
        .remove(index);
    layerListView.getSelectionModel()
        .clearSelection();
  }

  private void setContextMenu() {
    ContextMenu listItemContextMenu = new ContextMenu();
    MenuItem unselect = new MenuItem(Translator.getString("unselect"));
    MenuItem removeContextItem = new MenuItem(Translator.getString("delete"));

    unselect.setOnAction(event -> {
      deselect();
      if (listener != null) {
        listener.onLayersSelected(Collections.emptyList());
      }
    });
    removeContextItem.setOnAction(event -> {
      removeSelectedLayer();
    });
    listItemContextMenu.getItems()
        .setAll(unselect, removeContextItem);
    layerListView.setContextMenu(listItemContextMenu);
  }

  public void clearList() {
    layerListView.getItems()
        .clear();
  }

  public void refreshList() {
    layerListView.refresh();
  }

  public void addLayer(LayerListItem item) {
    layerListView.getItems()
        .add(item);
  }

  public void selectLastLayer() {
    layerListView.getSelectionModel()
        .selectLast();
  }

  public void setListener(LayerChangeListener listener) {
    this.listener = listener;
  }

  public void setCurrentScale(ScaleRatio currentScale) {
    this.currentScale = currentScale;
    setupCellFactory();
  }

  /**
   * Cell editing listeners
   */

  @Override public void onRenameLayer(int index, String name) throws NameAlreadyTaken {
    List<LayerListItem> layerItems = layerListView.getItems();
    for (int i = 0; i < layerItems.size(); i++) {
      LayerListItem line = layerItems.get(i);
      if (i != index && line.getPrimaryText()
          .equals(name)) {
        throw new NameAlreadyTaken();
      }
    }
    LayerListItem item = layerListView.getItems()
        .get(index);
    item.setPrimaryText(name);
    listener.onRenameLayer(item.getPrimaryText(), name);
  }

  public void setSelected(LayerListItem item) {
    layerListView.getSelectionModel()
        .select(item);
  }

  public void deselect() {
    layerListView.getSelectionModel()
        .clearSelection();
  }

  /**
   * Listener interface to be notified when a layer changes
   */
  public interface LayerChangeListener {
    void onRemoveLayer(String name);

    void onRenameLayer(String oldName, String newName);

    void onEquationAdd(EquationItem equationItem);

    void onLayersSelected(List<LayerListItem> items);
  }

  /**
   * **************** Button listeners ****************
   */
  public void onAddBtnClick(ActionEvent actionEvent) {
    EquationDialogController equationDialog = new EquationDialogController();
    equationDialog.init(layerListView.getScene()
        .getWindow(), this, getLines(), currentScale);
  }

  public void onRemoveBtnLayer(ActionEvent actionEvent) {
    removeSelectedLayer();
  }

  private List<SegLineGroup> getLines() {
    ArrayList<SegLineGroup> lines = new ArrayList<>();
    List<LayerListItem> items = layerListView.getItems();
    for (LayerListItem item : items) {
      if (item instanceof SegLineGroup) {
        lines.add((SegLineGroup) item);
      }
    }
    return lines;
  }

  public List<LayerListItem> getLayers() {
    List<LayerListItem> layers = layerListView.getItems();
    if (layers == null) return new ArrayList<>();
    return layers;
  }

  /**
   * Listener interface to be notified when an equation is added
   */

  @Override public void onAddEquation(EquationItem equation) {
    layerListView.getItems()
        .add(equation);
    listener.onEquationAdd(equation);
  }

  public class NameAlreadyTaken extends Exception {
    public NameAlreadyTaken() {
    }
  }
}
