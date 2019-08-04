package com.aragoj.mainscreen.menuitems

import de.saxsys.mvvmfx.ViewModel
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

class MenuItemViewModel : ViewModel {

    private val menuItemEvents: PublishProcessor<MenuItemAction> = PublishProcessor.create()

    fun menuItemClickEvents(): Flowable<MenuItemAction>{
        return menuItemEvents.onBackpressureLatest()
    }

    fun menuItemClicked(menuItem: MenuItemAction.Item) {
        menuItemEvents.onNext(MenuItemAction.Click(menuItem))
    }

    fun menuItemChecked(checkItem: MenuItemAction.Item, selected: Boolean) {
        menuItemEvents.onNext(MenuItemAction.Check(checkItem, selected))
    }

}