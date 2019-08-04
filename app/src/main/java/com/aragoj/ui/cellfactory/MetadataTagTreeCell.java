package com.aragoj.ui.cellfactory;

import javafx.scene.control.TreeTableCell;
import com.aragoj.ui.model.TagRow;

public class MetadataTagTreeCell extends TreeTableCell<TagRow, TagRow> {

    private MetadataTagTreeCellController controller = new MetadataTagTreeCellController();

    private static final String DEFAULT_STYLE_CLASS = "tree-table-cell";

    public MetadataTagTreeCell(){
        super();
    }

    @Override
    public void updateItem(TagRow item, boolean empty) {
        super.updateItem(item, empty);
        if(item != null && !empty){
            controller.setExportVisibility(item.isToExportVisible() && !item.isDirectory());
            controller.setExportImageManaged(!item.isDirectory());
            controller.setPrimaryLabelText(item.getTag());

            setGraphic(controller.getHBox());
            layoutChildren();
        }
        else{
            setGraphic(null);
        }
    }

}
