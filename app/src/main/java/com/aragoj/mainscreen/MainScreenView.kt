package com.aragoj.mainscreen

import de.saxsys.mvvmfx.FxmlPath
import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.fxml.FXML
import com.aragoj.mainscreen.menuitems.MenuItemView

@FxmlPath("/fmxl/mainscreen/MainDialog.fxml")
class MainScreenView : FxmlView<MainScreenViewModel>{

    @FXML private lateinit var menuView: MenuItemView

    @InjectViewModel private lateinit var viewModel: MainScreenViewModel

    fun initialize(){
        viewModel.addSubViewModel(menuView.getViewModel())
    }
}