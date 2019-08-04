package com.aragoj.mainscreen

import de.saxsys.mvvmfx.SceneLifecycle
import de.saxsys.mvvmfx.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import com.aragoj.mainscreen.menuitems.MenuItemViewModel

class MainScreenViewModel : ViewModel, SceneLifecycle {
    private lateinit var menuItemViewModel: MenuItemViewModel

    private val compositeDisposable = CompositeDisposable()

    override fun onViewAdded() {
        handleMenuItems()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onViewRemoved() {
        compositeDisposable.clear()
    }

    /**
     * Menu items handling
     */

    private fun handleMenuItems() {
        handleFileMenuItems()
        handleViewMenuItems()
        handleUnitConversionMenuItems()
        handleCalibrationMenuItems()
        menuItemViewModel.calibrationMenuItemEvents()
    }

    private fun handleFileMenuItems() {
        compositeDisposable += menuItemViewModel.fileMenuItemEvents()
                .subscribe({}, {e -> e.printStackTrace()})
    }

    private fun handleViewMenuItems() {
        compositeDisposable += menuItemViewModel.viewMenuItemEvents()
                .subscribe()
    }

    private fun handleUnitConversionMenuItems() {
        compositeDisposable += menuItemViewModel.unitConversionMenuItemEvents()
                .subscribe()
    }

    private fun handleCalibrationMenuItems() {
        compositeDisposable += menuItemViewModel.calibrationMenuItemEvents()
                .subscribe()
    }

    /**
     * Misc
     */
    fun addSubViewModel(viewModel: MenuItemViewModel) {
        menuItemViewModel = viewModel
    }
}