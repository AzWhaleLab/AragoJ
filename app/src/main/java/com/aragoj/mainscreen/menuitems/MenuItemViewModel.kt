package com.aragoj.mainscreen.menuitems

import de.saxsys.mvvmfx.ViewModel
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

class MenuItemViewModel : ViewModel {

    private val fileMenuItemEvents: PublishProcessor<MenuItemAction> = PublishProcessor.create()
    private val viewMenuItemEvents: PublishProcessor<MenuItemAction> = PublishProcessor.create()
    private val calibrationeMenuItemEvents: PublishProcessor<MenuItemAction> = PublishProcessor.create()
    private val unitConversionMenuItemEvents: PublishProcessor<MenuItemAction> = PublishProcessor.create()

    fun fileMenuItemEvents(): Flowable<MenuItemAction>{
        return fileMenuItemEvents.onBackpressureLatest()
    }

    fun viewMenuItemEvents(): Flowable<MenuItemAction>{
        return viewMenuItemEvents.onBackpressureLatest()
    }

    fun calibrationMenuItemEvents(): Flowable<MenuItemAction>{
        return calibrationeMenuItemEvents.onBackpressureLatest()
    }

    fun unitConversionMenuItemEvents(): Flowable<MenuItemAction>{
        return unitConversionMenuItemEvents.onBackpressureLatest()
    }


    fun fileMenuItemClicked(menuItem: MenuItemAction.Item) {
        fileMenuItemEvents.onNext(MenuItemAction.Click(menuItem))
    }

    fun viewMenuItemChecked(checkItem: MenuItemAction.Item, selected: Boolean) {
        viewMenuItemEvents.onNext(MenuItemAction.Check(checkItem, selected))
    }

    fun calibrationMenuItemClicked(menuItem: MenuItemAction.Item) {
        calibrationeMenuItemEvents.onNext(MenuItemAction.Click(menuItem))
    }

    fun unitConversionMenuItemClicked(menuItem: MenuItemAction.Item) {
        unitConversionMenuItemEvents.onNext(MenuItemAction.Click(menuItem))
    }
}