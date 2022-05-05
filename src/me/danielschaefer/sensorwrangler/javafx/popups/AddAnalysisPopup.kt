package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.base.App
import me.danielschaefer.sensorwrangler.javafx.AnalysisTab
import java.io.File
import java.nio.file.Paths
import kotlin.math.roundToInt

class AddAnalysisPopup(val parentStage: Stage, analysisTab: AnalysisTab? = null) : Stage() {

    init {
        val fileLabel = Label()
        val hBOx = HBox(1000.0).apply {

            val fileButton = Button("Choose file").apply {
                setOnAction {
                    val fileChooser = FileChooser()
                    fileChooser.initialDirectory = File(App.instance.settings.dataDirectory)

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
                val test = Paths.get("..","src/me/danielschaefer/sensorwrangler/javafx/popups/Analysis.fxml").toAbsolutePath().toUri().toURL()
                println(test)
                val fxmlLoader = FXMLLoader(test)
                val parent : Parent = fxmlLoader.load()
                val c : AnalysisControllerInJava=fxmlLoader.getController()
                val scene = Scene(parent)
                this@AddAnalysisPopup.scene = scene
                this@AddAnalysisPopup.isMaximized = true
                this@AddAnalysisPopup.show()
                getValues(fileLabel.text,c)




            }
        }
        scene = Scene(
            VBox(
                25.0,
                hBOx,
                showAnalysis
            ).apply {
                padding = Insets(25.0)

            }
        )
        title = "About - SensorWrangler"
        sizeToScene()
        show()

    }

    private fun getValues(file: String?, c: AnalysisControllerInJava) {
        var text = File(file).readText()
        var valueTable = text.lines()
        println(valueTable[0])
        var timestamps = ArrayList<Long>()
        var value = ArrayList<Float>()
        for (dataPoint in valueTable){
            if (dataPoint.isEmpty()){
                break
            }
            var values = dataPoint.split(",")
            println(values[0])
            value.add(values[0].toFloat())
            timestamps.add((values[1].toLong()-values[1].toLong()%1000)/1000)
        }
       InitializeSlider(timestamps,c)
        InitializeLabels(timestamps,value,c)
    }

    private fun InitializeLabels(timestamps: ArrayList<Long>, value: ArrayList<Float>, c: AnalysisControllerInJava) {
        var avg = (value.average() * 100).roundToInt() /100.0
        c.initLabels(timestamps,value,avg.toString())
    }

    private fun InitializeSlider(timestamps: ArrayList<Long>, c: AnalysisControllerInJava) {
       c.initSlider(timestamps)
    }
}
