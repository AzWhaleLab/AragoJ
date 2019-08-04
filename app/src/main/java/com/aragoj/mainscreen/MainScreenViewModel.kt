package com.aragoj.mainscreen

import com.aragoj.base.ViewModel
import com.aragoj.mainscreen.menuitems.MenuItemAction
import com.aragoj.mainscreen.menuitems.MenuItemViewModel
import com.uber.autodispose.autoDisposable

class MainScreenViewModel : ViewModel() {

  private lateinit var menuItemViewModel: MenuItemViewModel

  override fun onViewAdded() {
    super.onViewAdded()
    handleMenuItems()
  }

  override fun onViewRemoved() {
    super.onViewRemoved()
  }


  /**
   * Menu items handling
   */

  private fun handleMenuItems() {
    menuItemViewModel.menuItemClickEvents()
        .doOnNext { menuItemAction ->
          when(menuItemAction) {
            is MenuItemAction.Click -> handleMenuClick(menuItemAction.item)
            is MenuItemAction.Check -> handleMenuCheck(menuItemAction.item, menuItemAction.checked)
          }
        }
        .autoDisposable(this)
        .subscribe({}, { e -> e.printStackTrace() })  }


  private fun handleMenuCheck(item: MenuItemAction.Item, checked: Boolean) {
    when(item){
      MenuItemAction.Item.PRECISION_LINES_CHECK -> TODO()
      MenuItemAction.Item.IDENTIFIER_LINES_CHECK -> TODO()
      else -> Unit
    }
  }

  private fun handleMenuClick(item: MenuItemAction.Item) {
    when(item){
      MenuItemAction.Item.NEW_SESSION -> TODO()
      MenuItemAction.Item.OPEN_SESSION -> TODO()
      MenuItemAction.Item.SAVE_SESSION -> TODO()
      MenuItemAction.Item.SAVE_SESSION_AS -> TODO()
      MenuItemAction.Item.IMPORT_IMAGES -> TODO()
      MenuItemAction.Item.EXPORT_CSV -> TODO()
      MenuItemAction.Item.EXIT -> TODO()
      MenuItemAction.Item.CALIBRATE_CAMERA -> TODO()
      MenuItemAction.Item.UNDISTORT_IMAGE -> TODO()
      MenuItemAction.Item.CONVERT_UNITS -> TODO()
      MenuItemAction.Item.CONVERT_RATIO_DF -> TODO()
      MenuItemAction.Item.CONVERT_SCALE -> TODO()
      else -> Unit
    }
  }



  /**
   * Misc
   */
  fun addSubViewModel(viewModel: MenuItemViewModel) {
    menuItemViewModel = viewModel
  }
}