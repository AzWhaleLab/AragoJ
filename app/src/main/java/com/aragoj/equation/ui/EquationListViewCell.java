package com.aragoj.equation.ui;

import com.jfoenix.controls.JFXListCell;
import com.aragoj.equation.model.EquationItem;

public class EquationListViewCell extends JFXListCell<EquationItem>{

    @Override
    public void updateItem(EquationItem item, boolean empty) {
        super.updateItem(item, empty);
        if(item != null && !empty){
            setGraphic(null);
            setText(item.getName());
//            glyph.setFill(item.getColorObj());
//            glyph.setSize(32,32);
//            controller.setLabelGraphic(glyph);
//            controller.setPrimaryText(item.getName());
//            String secText = item.getLength() + " pixels";
//            if(currentScale != null) secText += " - " + Utility.roundTwoDecimals(currentScale.getRatio()*item.getLength())  + " " + currentScale.getUnits();
//            controller.setSecondaryLabel(secText);
//            setGraphic(controller.getHBox());
        }
        else{
            setGraphic(null);
        }
    }
}
