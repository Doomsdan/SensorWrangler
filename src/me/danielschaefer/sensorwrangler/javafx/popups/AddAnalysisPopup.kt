package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.base.App
import me.danielschaefer.sensorwrangler.javafx.AnalysisTab
import java.io.File

class AddAnalysisPopup(val parentStage: Stage, analysisTab: AnalysisTab? = null) : Stage() {

    init {

        val hBOx = HBox(1000.0).apply {
            val fileLabel = Label()
            val fileButton = Button("Choose file").apply {
                setOnAction {
                    val fileChooser = FileChooser()
                    fileChooser.initialDirectory = File(App.instance.settings.recordingDirectory).parentFile

                    fileChooser.showOpenDialog(this@AddAnalysisPopup)?.absolutePath?.let {
                        fileLabel.text = it
                        val regex = "Power\\s\\d{1,2}".toRegex();
                        if(!regex.containsMatchIn(fileLabel.text)){
                            fileLabel.text = "Für diese Messung wurde noch keine Analyse implementiert "
                        }
                    }
                }
            }
            children.addAll(fileLabel, fileButton)
        }
        val showAnalysis = Button("Show Analysis").apply {
            setOnAction {
                setScene(Scene(FXMLLoader.load(AddAnalysisPopup::class.java.getResource("Analysis.fxml"))))

            }
        }
        scene = Scene(
            VBox(
                25.0,
                hBOx
            ).apply {
                padding = Insets(25.0)

            }
        )
        title = "About - SensorWrangler"
        sizeToScene()
        show()

    }
}
