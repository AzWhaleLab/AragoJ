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

import com.jfoenix.controls.JFXAlert
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialogLayout
import javafx.scene.control.Label
import javafx.stage.Modality
import javafx.stage.Stage

/**
 * Utility builder to easily build JavaFX Alert Dialogs
 * Supports regular builder pattern & Kotlin DSL type-safe builder
 */

@DslMarker
annotation class AlertDialogDsl
annotation class ButtonDialogDsl

fun createDialog(stage: Stage, block: AlertDialog.Builder.() -> Unit): AlertDialog = AlertDialog.Builder(
    stage).apply(block).build()

class AlertDialog(private val stage:Stage, private val builder: Builder) {

  private lateinit var dialog: JFXAlert<Unit>

  init {
    buildAlert()
  }

  private fun buildAlert() {
    dialog = JFXAlert(stage)
    dialog.initModality(builder.modality)
    dialog.isOverlayClose = builder.closeOnOverlayClick

    val layout = JFXDialogLayout()
    layout.setHeading(Label(builder.title))
    builder.buttons
        .forEach { button ->
          val jfxButton = JFXButton(button.text)
          button.styleClass?.also { styleClass -> jfxButton.styleClass.add(styleClass) }
          button.action?.also { action -> jfxButton.setOnAction { action(dialog) }}
          layout.actions.add(jfxButton)
        }
    dialog.setContent(layout)
  }

  fun show() {
    dialog.show()
  }

  @AlertDialogDsl
  class Builder(private val stage: Stage) {
    @JvmField
    var title: String? = ""
    @JvmField
    var modality: Modality? = Modality.APPLICATION_MODAL
    @JvmField
    var closeOnOverlayClick: Boolean = true
    @JvmField
    var buttons: MutableList<Button> = ArrayList()

    fun setTitle(title: String): Builder = apply { this.title = title }

    fun setModality(modality: Modality): Builder = apply { this.modality = modality}

    fun setCloseOnOverlayClick(closeOnOverlayClick: Boolean): Builder = apply {this.closeOnOverlayClick = closeOnOverlayClick}

    fun addButton(block: Button.() -> Unit): Button = Button().also { button ->
      button.block()
      buttons.add(button)
    }

    fun build(): AlertDialog {
      return AlertDialog(stage, this@Builder)
    }
  }

  @ButtonDialogDsl
  class Button(){
    @JvmField
    var text: String = ""
    @JvmField
    var action: ((JFXAlert<Unit>) -> Unit)? = null
    @JvmField
    var styleClass: String? = null

    fun setText(text: String): Button = apply {this.text = text}
    fun setAction(action: ((JFXAlert<Unit>) -> Unit)?): Button = apply {this.action = action}
    fun setStyleClass(styleClass: String): Button = apply {this.styleClass = styleClass}
  }
}