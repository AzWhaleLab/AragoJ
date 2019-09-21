package com.aragoj.ui.controller;

import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableRow;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.util.Callback;
import com.aragoj.mainscreen.io.preferences.MetadataExportPreferencesManager;
import com.aragoj.ui.cellfactory.MetadataTagTreeCell;
import com.aragoj.ui.model.TagRow;
import com.aragoj.utils.Translator;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class MetaTreeTableController {

    @FXML
    private JFXTreeTableView<TagRow> treeTableView;

    @FXML
    private void initialize(){
        treeTableView.setColumnResizePolicy(JFXTreeTableView.CONSTRAINED_RESIZE_POLICY);

        JFXTreeTableColumn<TagRow,TagRow> tagCol = new JFXTreeTableColumn<>(Translator.getString("tagName"));
//        tagCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<TagRow, String> param) -> {
//            if(tagCol.validateValue(param)){
//                return param.getValue().getValue().tagProperty();
//            }
//            else{
//                return tagCol.getComputedValue(param);
//            }
//        });
        tagCol.setCellFactory(new Callback<TreeTableColumn<TagRow, TagRow>, TreeTableCell<TagRow, TagRow>>() {
            @Override
            public TreeTableCell<TagRow, TagRow> call(TreeTableColumn<TagRow, TagRow> param) {
                return new MetadataTagTreeCell();
            }
        });
        tagCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<TagRow, TagRow>, ObservableValue<TagRow>>() {
            @Override
            public ObservableValue<TagRow> call(TreeTableColumn.CellDataFeatures<TagRow, TagRow> param) {
                return new ReadOnlyObjectWrapper<>(param.getValue().getValue());
            }
        });
        tagCol.setPrefWidth(100);
        tagCol.setSortable(false);

        JFXTreeTableColumn<TagRow,String> valCol = new JFXTreeTableColumn<>(Translator.getString("value"));
        valCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<TagRow, String> param) -> {
            if(valCol.validateValue(param)){
                TagRow row = param.getValue().getValue();
                if(row.isDirectory()){
                    return new SimpleStringProperty("");
                }
                else{
                    return row.valueProperty();
                }
            }
            else{
                return valCol.getComputedValue(param);
            }
        });
        valCol.setSortable(false);

        // Disables column re-ordering
        // TODO: Refactor this for a non-deprecated method when it's available
        valCol.impl_setReorderable(false);
        tagCol.impl_setReorderable(false);

        treeTableView.getColumns().setAll(tagCol, valCol);

        treeTableView.setShowRoot(false);

        setContextMenus();
    }

    private void setContextMenus() {
        treeTableView.setRowFactory(treeTableView -> {
            ContextMenu directoryContextMenu = new ContextMenu();
            MenuItem gpsMapsItem = new MenuItem(Translator.getString("openInGmaps"));

            ContextMenu toExportContextMenu = new ContextMenu();
            MenuItem toExportItem = new MenuItem(Translator.getString("setToExport"));
            MenuItem toExportGroupItem = new MenuItem(Translator.getString("unsetGroupToExport"));

            JFXTreeTableRow<TagRow> row = new JFXTreeTableRow<TagRow>() {
                @Override
                public void updateItem(TagRow item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setContextMenu(null);
                    } else {
                        if(item.isDirectory()){
                            if(item.isToExportVisible()){
                                toExportGroupItem.setText((Translator.getString("unsetGroupToExport")));
                            } else {
                                toExportGroupItem.setText((Translator.getString("setGroupToExport")));
                            }
                            directoryContextMenu.getItems().setAll(toExportGroupItem);
                            if(item.getTag().equals("GPS")){
                                directoryContextMenu.getItems().add(gpsMapsItem);
                            }
                            setContextMenu(directoryContextMenu);
                        } else {
                            if(item.isToExportVisible()){
                                toExportItem.setText((Translator.getString("unsetToExport")));
                            } else {
                                toExportItem.setText((Translator.getString("setToExport")));
                            }
                            toExportContextMenu.getItems().setAll(toExportItem);
                            setContextMenu(toExportContextMenu);
                        }
                    }
                }
            };
            toExportItem.setOnAction( event -> {
                TagRow tagRow = row.getItem();
                tagRow.setToExport(!tagRow.isToExportVisible());
                if(tagRow.isToExportVisible()){
                    MetadataExportPreferencesManager.addToExportList(tagRow.getTag());
                } else {
                    MetadataExportPreferencesManager.removeToExportList(tagRow.getTag());
                }
                treeTableView.refresh();
            });
            toExportGroupItem.setOnAction( event -> {
                TagRow tagRowRoot = row.getItem();
                tagRowRoot.setToExport(!tagRowRoot.isToExportVisible());
                TreeItem<TagRow> treeItem = treeTableView.getTreeItem(row.getIndex());
                List<TreeItem<TagRow>> list = treeItem.getChildren();
                for(TreeItem<TagRow> tree : list){
                    TagRow tagRow = tree.getValue();
                    tagRow.setToExport(tagRowRoot.isToExportVisible());
//                    if(toExportGroupItem.getText().equalsIgnoreCase(Translator.getString("setGroupToExport"))){
//                        tagRow.setToExport(true);
//                    } else {
//                        tagRow.setToExport(false);
//                    }
                    if(tagRow.isToExportVisible()){
                        MetadataExportPreferencesManager.addToExportList(tagRow.getTag());
                    } else {
                        MetadataExportPreferencesManager.removeToExportList(tagRow.getTag());
                    }
                }
                treeTableView.refresh();
            });
            gpsMapsItem.setOnAction(event -> {
                TagRow tagRow = row.getItem();
                String latitude = tagRow.getChildren().get(0).getValue();
                String longitude = tagRow.getChildren().get(1).getValue();
                try {
                    Desktop.getDesktop().browse(new URI("https://www.google.com/maps/search/?api=1&query="+ latitude + "," + longitude));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            });
            return row;
        });
    }

    public void addRootTreeItem(ObservableList<TagRow> tags){
        TreeItem<TagRow> root = new RecursiveTreeItem<>(tags, RecursiveTreeObject::getChildren);
        treeTableView.setRoot(root);
    }

    public void clearRootTreeItem(){
        treeTableView.setRoot(null);
    }

}
