package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.chart.*
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Slider
import javafx.scene.layout.*
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.util.StringConverter
import me.danielschaefer.sensorwrangler.NamedThreadFactory
import me.danielschaefer.sensorwrangler.SensorWrangler
import me.danielschaefer.sensorwrangler.StringUtil
import me.danielschaefer.sensorwrangler.gui.*
import me.danielschaefer.sensorwrangler.gui.Chart
import me.danielschaefer.sensorwrangler.javafx.popups.Alert
import me.danielschaefer.sensorwrangler.javafx.popups.StartRecordingPopup
import me.danielschaefer.sensorwrangler.sensors.Sensor
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainWindow(private val primaryStage: Stage, private val wrangler: SensorWrangler) {
    private var paused: Boolean = false
    private var live: Boolean = true

    private lateinit var timeSlider: Slider
    private lateinit var buttonSkipToNow: Button

    init {
        primaryStage.apply {
            import()

            title = "SensorWrangler"

            val vBox = VBox(createMenuBar(primaryStage), createAllChartsBox(), createPlayBox())

            scene = Scene(
                vBox,
                App.instance.settings.defaultWindowWidth.toDouble(),
                App.instance.settings.defaultWindowHeight.toDouble()
            )

            // TODO: Set an icon for the program - how to embed resources in the .jar?
            //icons.add(Image(javaClass.getResourceAsStream("ruler.png")))

            show()

            updateShownTimeWindow()
        }
    }

    private fun import() {
        if (!App.instance.wrangler.import(App.instance.settings.configPath)) {
            Alert(primaryStage, "Import failed",
                "Failed to import configuration because '${App.instance.settings.configPath}' was not found.")
            return
        }

        App.instance.wrangler.sensors.filterIsInstance<Sensor>().forEach {
            it.addConnectionChangeListener(JavaFXUtil.createConnectionChangeListener(primaryStage))
        }
    }

    private fun createAllChartsBox(): GridPane {
        return GridPane().apply {
            // Let grid fill available space
            HBox.setHgrow(this, Priority.ALWAYS)
            VBox.setVgrow(this, Priority.ALWAYS)

            hgap = 25.0
            vgap = 25.0
            padding = Insets(25.0)

            val rows = App.instance.settings.chartGridRows;
            val cols = App.instance.settings.chartGridCols;

            //val fxChartIterator = fxCharts.iterator()
            rowLoop@ for (row in 0 until rows) {
                rowConstraints.add(RowConstraints().apply {
                    // Force row to resize, only then will the grid resize to its parent
                    vgrow = Priority.ALWAYS
                    // Keep all rows at the same height
                    percentHeight = 100.0 / rows
                })

                for (col in 0 until cols) {
                    // Add column only once
                    if (row == 0)
                        columnConstraints.add(ColumnConstraints().apply {
                            // Force row to resize, only then will the grid resize to its parent
                            hgrow = Priority.ALWAYS
                            // Keep all columns at the same width
                            percentWidth = 100.0 / cols
                        })

                    val chartBox = VBox(10.0).apply {
                        alignment = Pos.BOTTOM_CENTER
                    }
                    val chartDropdown = ComboBox<String>().apply {
                        App.instance.wrangler.charts.addListener(ListChangeListener {
                            items.setAll(it.list.map { it.title })
                        })
                        items.addAll(App.instance.wrangler.charts.map { it.title })
                        valueProperty().addListener(ChangeListener { observable, oldValue, newValue ->
                            // No need to do anything if we don't switch to a chart
                            // TODO: Maybe remove the current chart. Except it's not possible to manually select null
                            if (newValue == null)
                                return@ChangeListener

                            oldValue?.let {
                                App.instance.wrangler.findChartByTitle(oldValue)?.let {
                                    it.shown = false
                                }
                            }

                            App.instance.wrangler.findChartByTitle(newValue)?.let {
                                it.shown = true
                                chartBox.children[0] = createFxChart(it)
                            }
                            println("Switched from chart $oldValue to $newValue")
                        })
                    }

                    chartBox.children.setAll(Text("No Chart"), chartDropdown)

                    add(chartBox, col, row)
                }
            }
        }
    }

    private fun createPlayBox(): Node {
        val spacer = fun() = Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        timeSlider = Slider().apply {
            // TODO: Maybe start this only when the first sensor is connected
            min = Date().time.toDouble()
            max = min
            value = min

            // No ticks
            isShowTickMarks = false
            isShowTickLabels = false
            minorTickCount = 0
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        buttonSkipToNow = Button("Skip to now").apply {
            isDisable = true
            onAction = EventHandler {
                timeSlider.value = timeSlider.max
                live = true
                isDisable = true
            }
        }

        val buttonPause = Button("Pause").apply {
            onAction = EventHandler {
                paused = !paused
                live = !paused

                if (paused)
                    buttonSkipToNow.isDisable = false

                text = if (paused) "Start" else "Pause"
            }
        }

        val buttonProjected = Button("Start Recording").apply {
            onAction = EventHandler {
                if (App.instance.wrangler.isRecording.value) {
                    App.instance.wrangler.stopRecording()
                } else {
                    StartRecordingPopup(primaryStage)
                }
            }
            App.instance.wrangler.isRecording.addListener(ChangeListener<Boolean> { observable, old, new ->
                text = if (new) "Stop Recording" else "Start Recording"
            })
        }

        val selectedTimeLabel = Text()
        val selectedTimeBox = HBox(Text("Timestamp displayed: "), selectedTimeLabel)
        val beginningLabel = Text(StringUtil.formatDate(timeSlider.min))
        val nowLabel = Text()

        timeSlider.minProperty().addListener { _, _, new ->
            beginningLabel.text = StringUtil.formatDate(new)
        }

        timeSlider.maxProperty().addListener { _, _, new ->
            nowLabel.text = StringUtil.formatDate(new)
        }

        timeSlider.valueProperty().addListener { _, _, new ->
            selectedTimeLabel.text = StringUtil.formatDate(new)
        }

        val buttonBox = HBox(10.0, selectedTimeBox, spacer(), buttonPause, buttonSkipToNow, buttonProjected)
        val sliderBox = HBox(10.0, beginningLabel, timeSlider, nowLabel)

        return VBox(10.0, buttonBox, sliderBox).apply {
            padding = Insets(25.0)
        }
    }

    private fun updateShownTimeWindow() {
        Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("Update slider")).apply {
            scheduleAtFixedRate({
                val actuallyLive = timeSlider.value == timeSlider.max
                timeSlider.max = Date().time.toDouble()

                if (paused)
                    return@scheduleAtFixedRate

                if (live and actuallyLive) {
                    timeSlider.value = Date().time.toDouble()
                } else {
                    buttonSkipToNow.isDisable = false
                    // TODO: This might slowly fall behind the time,
                    //       if this thread isn't properly scheduled every 40ms
                    timeSlider.value += App.instance.settings.chartUpdatePeriod
                }

            }, 0, App.instance.settings.chartUpdatePeriod.toLong(), TimeUnit.MILLISECONDS)  // 40ms = 25FPS
        }
    }

    private fun createFxChart(chart: Chart): Node? {
        return when (chart) {
            is CurrentValueGraph -> {
                GridPane().apply {
                    vgap = 20.0
                    hgap = 20.0

                    chart.axes.forEachIndexed { row, axis ->
                        val text = Text(axis.description)
                        val value = Text()

                        // Show data from now until chart.windowSize ago
                        // TODO: Maybe dynamically adjust the period, e.g. if a sensors measures faster than 40ms
                        //       (The normal frequency of ANT+ sensors is 4Hz or 250ms)
                        // TODO: Kill thread, when chart is deselected. Can we bind its lifetime to the JavaFX chart object?
                        //       Or maybe have one thread for all charts?
                        Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("Update ${chart.title} window")).apply {
                            scheduleAtFixedRate({
                                Platform.runLater {
                                    // Get latest value that is before the current slider selection
                                    // The assumption is that the list of data points is sorted by timestamp
                                    // TODO: Measurements should have a second list of the sorted list
                                    val sortedDataPoints = axis.dataPoints
                                    val latestDataPoint = sortedDataPoints.lastOrNull { it.timestamp < timeSlider.value }

                                    value.text = (latestDataPoint?.value ?: 0.0).toString()
                                }
                            }, 0, App.instance.settings.chartUpdatePeriod.toLong(), TimeUnit.MILLISECONDS)  // 40ms = 25FPS
                        }
                        addRow(row, text, value)
                    }
                }
            }
            is BarGraph -> {
                val xAxis = CategoryAxis().apply {
                    label = chart.axisNames[0]
                    animated = false
                    // Long labels are automatically rotated to 90°.
                    // Setting it to 0° doesn't change that behaviour *shrug*
                    tickLabelRotation = 360.0
                }
                val fxYAxis = NumberAxis().apply {
                    label = chart.axisNames[0]
                    animated = false
                    isAutoRanging = false
                    lowerBound = chart.lowerBound
                    upperBound = chart.upperBound
                }
                BarChart(xAxis, fxYAxis).apply {
                    animated = false
                    val series = XYChart.Series<String, Number>().apply {
                        name = chart.title
                        val emptyList = mutableListOf<XYChart.Data<String, Number>>()
                        data = FXCollections.observableList(emptyList)
                    }

                    for (yAxis in chart.yAxes) {
                        // Start at 0, we need a starting value to later change the yValue of that
                        val data = XYChart.Data(yAxis.description, 0.0 as Number)
                        series.data.add(data)

                        // Show data from now until chart.windowSize ago
                        // TODO: Maybe dynamically adjust the period, e.g. if a sensors measures faster than 40ms
                        //       (The normal frequency of ANT+ sensors is 4Hz or 250ms)
                        // TODO: Kill thread, when chart is deselected. Can we bind its lifetime to the JavaFX chart object?
                        Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("Update ${chart.title} window")).apply {
                            scheduleAtFixedRate({
                                Platform.runLater {
                                    // Get latest value that is before the current slider selection
                                    // The assumption is that the list of data points is sorted by timestamp
                                    // TODO: Measurements should have a second list of the sorted list
                                    val sortedDataPoints = yAxis.dataPoints
                                    val latestDataPoint = sortedDataPoints.lastOrNull { it.timestamp < timeSlider.value }

                                    data.yValue = latestDataPoint?.value ?: 0.0
                                }
                            }, 0, App.instance.settings.chartUpdatePeriod.toLong(), TimeUnit.MILLISECONDS)  // 40ms = 25FPS
                        }
                    }

                    data.add(series)
                    this.isLegendVisible = false  // It's useless for bar charts
                }
            }
            is AxisGraph -> {
                val xAxis = NumberAxis().apply {
                    label = chart.axisNames[0]
                    isAutoRanging = false
                    tickUnit = 5_000.0  // Tick mark every 5 seconds
                    animated = false

                    tickLabelFormatter = object : StringConverter<Number>() {
                        override fun toString(unixTime: Number): String? {
                            return StringUtil.formatDate(unixTime)
                        }

                        override fun fromString(string: String?): Number {
                            // TODO: DateTimeParser to Number
                            return 0
                        }
                    }
                }

                val fxYAxis = NumberAxis().apply {
                    label = chart.axisNames[1]
                    isAutoRanging = false
                    lowerBound = chart.lowerBound
                    upperBound = chart.upperBound
                    tickUnit = chart.tickSpacing
                    animated = false
                }
                val fxChart = when (chart) {
                    is LineGraph -> LineChart(xAxis, fxYAxis).apply { createSymbols = chart.withDots }
                    is ScatterGraph -> ScatterChart(xAxis, fxYAxis)
                    else -> {
                        println("Cannot display this kind of chart")
                        null
                    }
                }

                fxChart?.apply {
                    title = chart.title
                    animated = false
                    for (yAxis in chart.yAxes) {
                        val series = XYChart.Series<Number, Number>().apply {
                            name = yAxis.description ?: "Data"
                        }


                        series.data = FXCollections.observableList(mutableListOf<XYChart.Data<Number, Number>>())
                        // Fill with past data
                        series.data.addAll(yAxis.dataPoints.map { dp -> XYChart.Data(dp.timestamp as Number, dp.value as Number) })

                        // TODO: Maybe we can define some sort of mapping to get rid of the additional listener,
                        //       like the cellFactory, but for charts
                        yAxis.dataPoints.addListener(ListChangeListener {
                            it.next()
                            series.data.addAll(it.addedSubList.map { dp -> XYChart.Data(dp.timestamp as Number, dp.value as Number) })
                        })

                        fxChart.data.add(series)

                        // Show data from now until chart.windowSize ago
                        // TODO: Maybe dynamically adjust the period, e.g. if a sensors measures faster than 40ms
                        //       (The normal frequency of ANT+ sensors is 4Hz or 250ms)
                        // TODO: Kill thread, when chart is deselected. Can we bind its lifetime to the JavaFX chart object?
                        Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("Update ${chart.title} window")).apply {
                            scheduleAtFixedRate({
                                Platform.runLater {
                                    xAxis.upperBound = timeSlider.value
                                    xAxis.lowerBound = xAxis.upperBound - chart.windowSize
                                }
                            }, 0, App.instance.settings.chartUpdatePeriod.toLong(), TimeUnit.MILLISECONDS)  // 40ms = 25FPS
                        }
                    }
                }
            }
            is DistributionGraph -> {
                PieChart().apply {
                    animated = false
                    startAngle = -90.0

                    // Start at 0, we need a starting value to later change the yValue of that
                    val leftData = PieChart.Data("Left", 50.0)
                    val rightData = PieChart.Data("Right", 50.0)

                    // Show data from now until chart.windowSize ago
                    // TODO: Maybe dynamically adjust the period, e.g. if a sensors measures faster than 40ms
                    //       (The normal frequency of ANT+ sensors is 4Hz or 250ms)
                    // TODO: Kill thread, when chart is deselected. Can we bind its lifetime to the JavaFX chart object?
                    Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("Update ${chart.title} window")).apply {
                        scheduleAtFixedRate({
                            Platform.runLater {
                                // Get latest value that is before the current slider selection
                                // The assumption is that the list of data points is sorted by timestamp
                                // TODO: Measurements should have a second list of the sorted list
                                val sortedDataPoints = chart.axis.dataPoints
                                val latestDataPoint = sortedDataPoints.lastOrNull { it.timestamp < timeSlider.value }

                                rightData.pieValue = latestDataPoint?.value ?: 50.0
                                leftData.pieValue = 100.0 - rightData.pieValue
                            }
                        }, 0, App.instance.settings.chartUpdatePeriod.toLong(), TimeUnit.MILLISECONDS)  // 40ms = 25FPS
                    }

                    data = FXCollections.observableArrayList(listOf(leftData, rightData))
                }
            }
            else -> {
                println("Cannot display this kind of chart")
                null
            }
        }
    }
}
