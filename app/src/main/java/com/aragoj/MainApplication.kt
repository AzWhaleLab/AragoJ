package com.aragoj

import com.aragoj.mainscreen.MainScreenView
import com.halfhp.rxtracer.RxTracer
import de.saxsys.mvvmfx.FluentViewLoader
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

fun main(args: Array<String>){
    Application.launch(*args)
}

class MainApplication : Application() {

    override fun start(primaryStage: Stage) {
        initializeLibraries()

        val viewTuple = FluentViewLoader.fxmlView(MainScreenView::class.java).load()
        val root = viewTuple.view
        val scene = Scene(root, 800.0, 600.0)
        scene.stylesheets.addAll(javaClass.getResource("/css/MainApplication.css").toExternalForm())
        primaryStage.icons.add(Image(MainApplication::class.java.getResourceAsStream("/images/icon.png")))
        primaryStage.title = "AragoJ"
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun initializeLibraries() {
        RxTracer.enable()
    }
}