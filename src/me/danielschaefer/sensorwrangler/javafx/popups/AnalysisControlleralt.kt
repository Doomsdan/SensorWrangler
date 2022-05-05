/**
 * Sample Skeleton for 'Analysis.fxml' Controller Class
 */

import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.FXML
import javafx.scene.chart.*
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.input.DragEvent

class AnalysisControlleralt {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private lateinit var resources: ResourceBundle

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private lateinit var location: URL

     // fx:id="avgPower"
    private var avgPower = Label() // Value injected by FXMLLoader

    @FXML // fx:id="ftp"
    private var ftp = Label() // Value injected by FXMLLoader


    @FXML // fx:id="minutes"
    private var minutes= Label() // Value injected by FXMLLoader

    @FXML // fx:id="peakPower"
    private var peakPower= Label() // Value injected by FXMLLoader
    @FXML // fx:id="xAxis"
    private  var xAxis= NumberAxis() // Value injected by FXMLLoader

    @FXML // fx:id="yAxis"
    private var yAxis= CategoryAxis() // Value injected by FXMLLoader


    @FXML // fx:id="powerChart"
    private var powerChart= LineChart(xAxis, yAxis) // Value injected by FXMLLoader

    @FXML // fx:id="slider"
    private var slider= Slider() // Value injected by FXMLLoader

    @FXML
    fun UpdateFTP(event: DragEvent) {

    }
    fun initSlider(timestamps: ArrayList<Long>){
        slider.min = 1.0
        slider.max = ((timestamps.last() -timestamps[0])).toDouble()
        slider.value = 1.0
        slider.blockIncrement = 1.0
        slider.majorTickUnit = 1.0
        slider.minorTickCount = 0
    }

    fun initLabels(timestamps: ArrayList<Long>, value: ArrayList<Float>){
        avgPower.text= value.average().toString()
        println(value.average().toString())
        peakPower.text = (value.maxOrNull() ?: 0).toString()
        minutes.text = slider.value.toString()
        calculateFTP(timestamps,value)

    }
    @FXML
    private fun calculateFTP(timestamps: ArrayList<Long>, values: ArrayList<Float>) {
        var ftp = 0.0
        for (i in 0 until timestamps.size step 1){

            if (timestamps.last() - timestamps[i]<= slider.value){
                var j = 0
                while (timestamps[j]-timestamps[i] <= slider.value){
                    j++;
                }
                if (values.subList(i,j-1).average() < ftp){
                    ftp = values.subList(i,j-1).average()
                }
            }else{
                break
            }
        }
    }


    @FXML // This method is called by the FXMLLoader when initialization is complete
    fun initialize() {
        assert(avgPower != null) {"fx:id=\"avgPower\" was not injected: check your FXML file 'Analysis.fxml'." }
        assert(ftp != null) {"fx:id=\"ftp\" was not injected: check your FXML file 'Analysis.fxml'." }
        assert(minutes != null) {"fx:id=\"minutes\" was not injected: check your FXML file 'Analysis.fxml'." }
        assert(peakPower != null) {"fx:id=\"peakPower\" was not injected: check your FXML file 'Analysis.fxml'." }
        assert(powerChart != null) {"fx:id=\"powerChart\" was not injected: check your FXML file 'Analysis.fxml'." }
        assert(slider != null) {"fx:id=\"slider\" was not injected: check your FXML file 'Analysis.fxml'." }

    }

}
