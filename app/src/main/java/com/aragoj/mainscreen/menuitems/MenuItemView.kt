package com.aragoj.mainscreen.menuitems

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import de.saxsys.mvvmfx.ViewModel
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.MenuItem


class MenuItemView : FxmlView<MenuItemViewModel>{
    @FXML private lateinit var newSessionMenuItem: MenuItem
    @FXML private lateinit var openSessionMenuItem: MenuItem
    @FXML private lateinit var saveSessionMenuItem: MenuItem
    @FXML private lateinit var saveSessionAsMenuItem: MenuItem
    @FXML private lateinit var importImagesMenuItem: MenuItem
    @FXML private lateinit var exportCSVMenuItem: MenuItem

    @FXML private lateinit var precisionLinesCheckItem: CheckMenuItem
    @FXML private lateinit var identifierLinesCheckItem: CheckMenuItem

    @FXML private lateinit var convertUnitsMenuItem: MenuItem

    @FXML private lateinit var undistortMenuItem: MenuItem

    @InjectViewModel private lateinit var viewModel: MenuItemViewModel


    fun getViewModel(): MenuItemViewModel {
        return viewModel
    }

    /**
     * File
     */

    fun newSessionClick(actionEvent: ActionEvent){
        viewModel.fileMenuItemClicked(MenuItemAction.Item.NEW_SESSION)
    }

    fun openSessionClick(actionEvent: ActionEvent) {
        viewModel.fileMenuItemClicked(MenuItemAction.Item.OPEN_SESSION)
    }

    fun saveSessionClick(actionEvent: ActionEvent) {
        viewModel.fileMenuItemClicked(MenuItemAction.Item.SAVE_SESSION)
    }

    fun saveSessionAsClick(actionEvent: ActionEvent) {
        viewModel.fileMenuItemClicked(MenuItemAction.Item.SAVE_SESSION_AS)
    }

    fun importImagesClick(actionEvent: ActionEvent) {
        viewModel.fileMenuItemClicked(MenuItemAction.Item.IMPORT_IMAGES)
    }

    fun exportCSVClick(actionEvent: ActionEvent) {
        viewModel.fileMenuItemClicked(MenuItemAction.Item.EXPORT_CSV)
    }

    fun exitClick(actionEvent: ActionEvent) {
        viewModel.fileMenuItemClicked(MenuItemAction.Item.EXIT)
    }

    /**
     * View
     */

    fun precisionLinesCheckClick(actionEvent: ActionEvent) {
        viewModel.viewMenuItemChecked(MenuItemAction.Item.PRECISION_LINES_CHECK, (actionEvent.source as CheckMenuItem).isSelected)
    }

    fun identifierLinesCheckClick(actionEvent: ActionEvent) {
        viewModel.viewMenuItemChecked(MenuItemAction.Item.IDENTIFIER_LINES_CHECK, (actionEvent.source as CheckMenuItem).isSelected)
    }


    /**
     * Calibration
     */

    fun calibrateCameraClick(actionEvent: ActionEvent) {
        viewModel.calibrationMenuItemClicked(MenuItemAction.Item.CALIBRATE_CAMERA)
    }

    fun undistortImageClick(actionEvent: ActionEvent) {
        viewModel.calibrationMenuItemClicked(MenuItemAction.Item.UNDISTORT_IMAGE)
    }

    /**
     * Unit conversion
     */

    fun convertViaScaleClick(actionEvent: ActionEvent) {
        viewModel.unitConversionMenuItemClicked(MenuItemAction.Item.CONVERT_SCALE)
    }

    fun convertViaRatioDfClick(actionEvent: ActionEvent) {
        viewModel.unitConversionMenuItemClicked(MenuItemAction.Item.CONVERT_RATIO_DF)
    }
    fun convertUnitsClick(actionEvent: ActionEvent) {
        viewModel.unitConversionMenuItemClicked(MenuItemAction.Item.CONVERT_UNITS)
    }
}