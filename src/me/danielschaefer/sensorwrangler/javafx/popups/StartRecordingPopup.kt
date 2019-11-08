package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.DirectoryChooser
import javafx.stage.Modality
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.javafx.App

class StartRecordingPopup(val parentStage: Stage): Stage() {
    init {
        initModality(Modality.APPLICATION_MODAL)
        initOwner(parentStage)

        val targetDirPathLabel = Text(App.instance!!.settings.recordingDirectory ?: "No file selected")
        val chooseFileButton = Button("Choose log directory").apply {
            setOnAction {
                DirectoryChooser().apply {
                    title = "Chooose log directory"
                    showDialog(parentStage)?.let {
                        targetDirPathLabel.text = it.absolutePath
                        App.instance!!.settings.recordingDirectory = it.absolutePath
                        sizeToScene()
                    }
                }
            }
        }
        val startRecordingButton = Button("Start recording").apply {
            setOnAction {
                if (App.instance!!.settings.recordingDirectory == null) {
                    Alert(
                        parentStage,
                        "Must select file",
                        "Before starting logging, you must select a log file"
                    ).show()
                    return@setOnAction
                }
                App.instance!!.settings.recordingDirectory?.let {
                    App.instance!!.wrangler.startRecording()
                    close()
                }
            }
        }

        val vBox = VBox(targetDirPathLabel, chooseFileButton, startRecordingButton).apply {
            padding = Insets(25.0)
        }
        scene = Scene(vBox)
        title = "Add LineChart"

        sizeToScene()
        show()
    }
}