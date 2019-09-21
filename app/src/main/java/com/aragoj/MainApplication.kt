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

package com.aragoj

import com.aragoj.mainscreen.MainScreenView
import com.aragoj.utils.Translator
import de.saxsys.mvvmfx.FluentViewLoader
import de.saxsys.mvvmfx.MvvmFX
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

fun main(args: Array<String>){
    Application.launch(MainApplication::class.java, *args)
}

class MainApplication : Application() {

    override fun start(primaryStage: Stage) {
        initializeLibraries()

        MvvmFX.setGlobalResourceBundle(Translator.getBundle())
        val viewTuple = FluentViewLoader.fxmlView(MainScreenView::class.java).load()
        val root = viewTuple.view
        val scene = Scene(root, 800.0, 600.0)
        scene.stylesheets.addAll(javaClass.getResource("/css/MainApplication.css").toExternalForm())
        primaryStage.icons.add(Image(MainApplication::class.java.getResourceAsStream("/images/icon.png")))
        primaryStage.title = "AragoJ"
        primaryStage.scene = scene
        primaryStage.setOnCloseRequest { e -> viewTuple.codeBehind.onDestroy(e) }
        viewTuple.codeBehind.stage = primaryStage
        primaryStage.show()
    }

    private fun initializeLibraries() {
        RxTracer.enable()
    }
}