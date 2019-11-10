package me.danielschaefer.sensorwrangler.gui

import javafx.collections.ObservableList
import javafx.scene.chart.XYChart
import me.danielschaefer.sensorwrangler.Measurement


class Graph(title: String, val axisNames: Array<String>, val yAxis: Measurement? = null) : Chart(title) {
    var mappedList: ObservableList<XYChart.Data<String, Number>>? = null

    var windowSize: Int = 25
    var lowerBound: Double = -10.0
    var upperBound: Double = 10.0
    var tickSpacing: Double = 1.0

    init {
        if (yAxis != null) {
            println("Graph $title was initialized with ${yAxis.values}")
        } else {
            println("Graph $title wasn't initialized")
        }
    }
}