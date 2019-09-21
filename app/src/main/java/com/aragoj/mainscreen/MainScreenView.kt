/*
 * Copyright 2019 franciscoaleixo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aragoj.mainscreen

import com.aragoj.base.BaseView
import com.aragoj.mainscreen.dialogs.MenuBar
import com.aragoj.mainscreen.dialogs.createDialog
import com.aragoj.mainscreen.dialogs.createMenuBar
import com.jfoenix.controls.JFXAlert
import de.saxsys.mvvmfx.FxmlPath
import de.saxsys.mvvmfx.InjectViewModel
import javafx.fxml.FXML
import javafx.scene.input.KeyCode
import javafx.scene.layout.VBox
import javafx.stage.WindowEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL
import java.util.*

@FxmlPath("/fmxl/mainscreen/MainDialog.fxml")
class MainScreenView : BaseView<MainScreenViewModel>() {


  @FXML
  private lateinit var menuView: VBox

  @InjectViewModel
  private lateinit var viewModel: MainScreenViewModel

  override fun initialize(location: URL?, resources: ResourceBundle?) {
    populateMenuBar()

    GlobalScope.launch {
      processEvents()
    }
  }

  private fun populateMenuBar() {
    menuView.children.add(createMenuBar {
      style = "-fx-background-color: #FFFFFF;"
      menu {
        text = getString("file")
        menuItem {
          text = getString("newsession")
          keyCombination = MenuBar.KeyCombination(KeyCode.N, listOf(MenuBar.KeyModifier.CONTROL))
          action = { viewModel.handleMenuItemClick() }
        }
        menuItem {
          text = getString("open")
          keyCombination = MenuBar.KeyCombination(KeyCode.O, listOf(MenuBar.KeyModifier.CONTROL))
        }
        menuItem {
          text = getString("save")
          keyCombination = MenuBar.KeyCombination(KeyCode.S, listOf(MenuBar.KeyModifier.CONTROL))
        }
        menuItem {
          text = getString("saveAs")
          keyCombination = MenuBar.KeyCombination(KeyCode.N,
              listOf(MenuBar.KeyModifier.CONTROL, MenuBar.KeyModifier.SHIFT))
        }
        menuItem {
          text = getString("importimages")
          keyCombination = MenuBar.KeyCombination(KeyCode.I, listOf(MenuBar.KeyModifier.CONTROL))
        }
        menuItem {
          text = getString("exportcsv")
          keyCombination =
              MenuBar.KeyCombination(KeyCode.E, listOf(MenuBar.KeyModifier.CONTROL))
        }
        menuItem {
          text = getString("exit")
        }
      }
      menu {
        text = getString("view")
        checkMenuItem {
          text = getString("precisionLines")
        }
        checkMenuItem {
          text = getString("identifierLines")
        }
      }
      menu {
        text = getString("module")
        menu {
          text = getString("calibration")
          menuItem {
            text = getString("calibrateCamera")
          }
          separatorMenuItem()
          menuItem {
            text = getString("undistortDots")
          }
        }
        menu {
          text = getString("scale")
          menuItem {
            text = getString("reference")
          }
          menuItem {
            text = getString("ratioDf")
          }
          menuItem {
            text = getString("convertUnits")
          }
        }
      }
    })
  }

  private fun processEvents() {
    viewModel.events
        .doOnNext { event ->
          when (event) {
            is ShowCloseDialog -> closeAppWithSureDialog(event.session.name)
            is ShowNewSessionDialog -> startNewSession(event.session.name)
          }
        }.subscribe()

  }
  /**
   * Menu Dialogs
   */


  /**
   * Called in case of uncontrolled closing
   */
  fun onDestroy(windowEvent: WindowEvent) {
    // Cancel the destroy & do a controled closing
    windowEvent.consume()
    viewModel.handleExitClick()
  }

  fun startNewSession(sessionName: String) {
    val saveAction: ((JFXAlert<Unit>) -> Unit) = { alert ->
      alert.hideWithAnimation()
      viewModel.handleNewSessionVerificationDialogInput(shouldSave = true)
    }
    val dontSaveAction: ((JFXAlert<Unit>) -> Unit) = { alert ->
      alert.hideWithAnimation()
      viewModel.handleNewSessionVerificationDialogInput(shouldSave = false)
    }
    showSureDialog(sessionName, saveAction, dontSaveAction)
  }

  fun closeAppWithSureDialog(sessionName: String) {
    val saveAction: ((JFXAlert<Unit>) -> Unit) = { alert ->
      alert.hideWithAnimation()
      viewModel.handleCloseVerificationDialogInput(shouldSave = true)
    }
    val dontSaveAction: ((JFXAlert<Unit>) -> Unit) = { alert ->
      alert.hideWithAnimation()
      viewModel.handleCloseVerificationDialogInput(shouldSave = false)
    }
    showSureDialog(sessionName, saveAction, dontSaveAction)
  }

  private fun showSureDialog(sessionName: String, saveAction: ((JFXAlert<Unit>) -> Unit),
                             dontSaveAction: ((JFXAlert<Unit>) -> Unit)) {
    createDialog(stage) {
      title = "Do you want to save changes to $sessionName?"
      closeOnOverlayClick = false
      addButton {
        text = getString("save")
        action = saveAction
      }
      addButton {
        text = getString("dontsave")
        action = dontSaveAction
      }
      addButton {
        text = getString("cancel")
        action = { alert ->
          alert.hideWithAnimation()
        }
      }
    }.show()
  }

}