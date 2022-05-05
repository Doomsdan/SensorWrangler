package me.danielschaefer.sensorwrangler.javafx.popups; /**
 * Sample Skeleton for 'Analysis.fxml' Controller Class
 */

import java.io.Console;
import java.net.URL;
import java.util.*;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;

public class AnalysisControllerInJava implements Initializable {

    ArrayList<Long> timestamps = new ArrayList<Long>(); // Create an ArrayList object

    ArrayList<Float> values = new ArrayList<Float>(); // Create an ArrayList object

    XYChart.Series series = new XYChart.Series();
    double weight = 75;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="avgPower"
    private Label avgPower; // Value injected by FXMLLoader

    @FXML // fx:id="ftp"
    private Label ftp ; // Value injected by FXMLLoader

    @FXML // fx:id="minutes"
    private Label minutes; // Value injected by FXMLLoader

    @FXML // fx:id="peakPower"
    private Label peakPower; // Value injected by FXMLLoader

    @FXML // fx:id="xAxis"
    private NumberAxis xAxis; // Value injected by FXMLLoader

    @FXML // fx:id="yAxis"
    private NumberAxis yAxis; // Value injected by FXMLLoader

    @FXML // fx:id="powerChart"
    private LineChart<NumberAxis, NumberAxis> powerChart; // Value injected by FXMLLoader

    @FXML // fx:id="slider"
    private Slider slider; // Value injected by FXMLLoader

    @FXML // fx:id="Weight"
    private TextField Weight; // Value injected by FXMLLoader

    @FXML // fx:id="WeightButton"
    private Button WeightButton; // Value injected by FXMLLoader

    @FXML // fx:id="avgPowerKg"
    private Label avgPowerKg; // Value injected by FXMLLoader

    @FXML // fx:id="ftpProKg"
    private Label ftpProKg; // Value injected by FXMLLoader

    @FXML // fx:id="peakPowerPerKg"
    private Label peakPowerPerKg; // Value injected by FXMLLoader




    @FXML
    void UpdateFTP(MouseEvent event) {
        double time = Math.round((Double)slider.getValue()*100/60)/100.0;
        this.minutes.setText(String.valueOf((time)));
        calculateFTP(timestamps,values);
    }

    @FXML
    void setWeight(ActionEvent event) {
       weight = Double.parseDouble(Weight.getText());
        this.avgPowerKg.setText(Double.parseDouble(avgPower.getText().replace("W",""))/weight+ "W");
        this.peakPowerPerKg.setText(Double.parseDouble(peakPower.getText().replace("W",""))/weight+"W");
        this.ftpProKg.setText(Double.parseDouble(ftp.getText().replace("W",""))/weight + "W");
    }


    void initSlider(ArrayList<Long> timestamps){
        this.slider.setMin(1.0);
        this.slider.setMax((timestamps.get(timestamps.size() - 1) -timestamps.get(0)));
        this.slider.setValue(1.0);
        this.slider.setBlockIncrement(1.0);
        this.slider.setMajorTickUnit(1.0);
        this.slider.setMinorTickCount(0);
        slider.setShowTickLabels(false);
        slider.setSnapToTicks(true);
    }

    public void initLabels(ArrayList<Long> timestamps, ArrayList<Float> value, String avg){
        this.timestamps = timestamps;
        this.values = value;
        this.avgPower.setText(avg+ "W");
        this.avgPowerKg.setText(Double.parseDouble(avg)/weight+ "W");
        this.peakPower.setText(Collections.max(value).toString()+"W");
        this.peakPowerPerKg.setText(Double.parseDouble(peakPower.getText().replace("W",""))/weight+"W");
        double time = Math.round((Double)slider.getValue()*100/60)/100.0;
        this.minutes.setText(String.valueOf((time)));
        series.setName("Power");
        addData();


        calculateFTP(timestamps,value);

    }

    private void addData() {
        for (int i = 0;i<timestamps.size();i++){
            series.getData().add(new XYChart.Data(timestamps.get(i)-timestamps.get(0),values.get(i)));
        }
        powerChart.getData().add(series);
    }

    private void calculateFTP(ArrayList<Long> timestamps, ArrayList<Float> value) {
        double ftp = 0.0;
        for (int i = 0;i<timestamps.size();i++){

            if ((timestamps.get(timestamps.size()-1) - timestamps.get(i))>= this.slider.getValue()){
                int j = i;
                while (timestamps.get(j)-timestamps.get(i) <= this.slider.getValue()){
                    j++;
                    System.out.println(j);
                    if (j == timestamps.size()){
                        break;
                    }
                }
                double avg = getAverage(value.subList(i,j-1));
                if (avg > ftp){
                    ftp = avg;
                }
            }else{
                break;
            }
        }
        this.ftp.setText(ftp +"W");
        this.ftpProKg.setText(ftp/weight + "W");
    }

    private double getAverage(List<Float> subList) {
        float val = 0;
        if(subList.size() != 0){
            for (int i = 0;i< subList.size();i++){
                val += subList.get(i);
            }
            return (val)/subList.size();
        }
       return 0.0;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        minutes.setText("10");

    }
}
