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

package com.aragoj.mainscreen.dialogs

import javafx.event.ActionEvent
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.input.KeyCode

/**
 * Utility builder to easily build a JavaFX MenuBar
 * Supports regular builder pattern & Kotlin DSL type-safe builder
 */

@DslMarker
annotation class MenuBarDsl

@DslMarker
annotation class MenuDsl

fun createMenuBar(block: MenuBar.Builder.() -> Unit): javafx.scene.control.MenuBar = MenuBar.Builder().apply(
    block).build()

class MenuBar(private val builder: Builder) {

  private lateinit var menuBar: javafx.scene.control.MenuBar

  init {
    buildMenuBar()
  }

  private fun buildMenuBar(): javafx.scene.control.MenuBar {
    val menuBar = javafx.scene.control.MenuBar()
    builder.style?.also { style -> menuBar.style = style }
    builder.menus
        .forEach { menu ->
          menuBar.menus.add(getMenu(menu))
        }
    return menuBar
  }

  private fun getMenu(menu: Menu): javafx.scene.control.Menu {
    val fxMenu = javafx.scene.control.Menu(menu.text)
    menu.menuItems
        .forEach { menuItem ->
          when(menuItem){
            is Menu -> fxMenu.items.add(getMenu(menuItem))
            is MenuItem -> {
              val fxMenuItem = javafx.scene.control.MenuItem(menuItem.text)
              fxMenuItem.setOnAction { e -> menuItem.action?.invoke(e) }
              fxMenu.items.add(fxMenuItem)
              menuItem.keyCombination?.apply {
                fxMenu.accelerator = getKeyCombination(this)
              }
            }
            is Separator -> fxMenu.items.add(SeparatorMenuItem())
          }
        }
    return fxMenu
  }

  private fun getKeyCombination(keyCombination: KeyCombination): javafx.scene.input.KeyCodeCombination {
    val shiftMod = getModifierValue(keyCombination, KeyModifier.SHIFT)
    val controlMod = getModifierValue(keyCombination, KeyModifier.CONTROL)
    val altMod = getModifierValue(keyCombination, KeyModifier.ALT)
    val metaMod = getModifierValue(keyCombination, KeyModifier.META)
    val shortcutMod = getModifierValue(keyCombination, KeyModifier.SHORTCUT)
    return javafx.scene.input.KeyCodeCombination(keyCombination.code, shiftMod, controlMod, altMod, metaMod, shortcutMod)
  }

  private fun getModifierValue(keyCombination: KeyCombination,
                               keyModifier: KeyModifier): javafx.scene.input.KeyCombination.ModifierValue {
    return if (keyCombination.keyModifiers.contains(keyModifier))
      javafx.scene.input.KeyCombination.ModifierValue.DOWN
    else
      javafx.scene.input.KeyCombination.ModifierValue.ANY
  }

  @MenuBarDsl
  class Builder() {
    @JvmField
    var menus: MutableList<Menu> = ArrayList()

    @JvmField
    var style: String? = null

    fun setStyle(style: String): Builder = apply { this.style = style }
    fun menu(block: MenuBar.Menu.() -> Unit): Menu = Menu().also { menu ->
      menu.block()
      menus.add(menu)
    }

    fun build(): javafx.scene.control.MenuBar {
      return MenuBar(this@Builder).buildMenuBar()
    }
  }

  @MenuDsl
  class Menu() : MenuItem() {
    @JvmField
    var menuItems: MutableList<Item> = ArrayList()

    fun menu(block: MenuBar.Menu.() -> Unit): Menu = Menu().also { menu ->
      menu.block()
      menuItems.add(menu)
    }

    fun menuItem(block: MenuBar.MenuItem.() -> Unit): MenuItem = MenuItem().also { menuItem ->
      menuItem.block()
      menuItems.add(menuItem)
    }

    fun checkMenuItem(block: MenuBar.CheckMenuItem.() -> Unit): CheckMenuItem = CheckMenuItem().also { checkMenuItem ->
      checkMenuItem.block()
      menuItems.add(checkMenuItem)
    }

    fun separatorMenuItem(){
      menuItems.add(Separator())
    }
  }

  class CheckMenuItem() : MenuItem()

  class Separator() : Item()

  open class MenuItem() : Item() {
    @JvmField
    var text: String = ""
    @JvmField
    var action: ((ActionEvent) -> Unit)? = null
    @JvmField
    var keyCombination: KeyCombination? = null

    fun setText(text: String): MenuItem = apply { this.text = text }
    fun setAction(action: ((ActionEvent) -> Unit)): MenuItem = apply { this.action = action }
    fun setKeyCombinaton(keyCombination: KeyCombination): MenuItem = apply {
      this.keyCombination = keyCombination
    }


  }
  open class Item()

  data class KeyCombination(val code: KeyCode, val keyModifiers: List<KeyModifier>)
  enum class KeyModifier { SHIFT, CONTROL, ALT, META, SHORTCUT }
}