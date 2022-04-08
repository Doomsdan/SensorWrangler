package me.danielschaefer.sensorwrangler.javafx.popups.test

import me.danielschaefer.sensorwrangler.base.App
import java.io.*
import java.nio.file.*
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

//import org.json.*


class DataPointsExporter {
    //val configFile = getConfigPath()
    //private val configReader: Reader = FileReader(configFile)
    //val jsonReader: JSONObject = JSONObject(getConfigPath())
    //val number = jsonReader.getInt("index")
    var i = 0
    var j = 0
    var path = "C:\\Users\\Daniel\\Desktop\\Programmieren\\Projekte\\SensorWrangler\\data\\Measurement-"
    fun extractDataPoints(){
        j = 0
        i++
        var pathnew = path+i
        while (Paths.get(pathnew).isDirectory()){
            i++
            pathnew = path+i
        }
        Paths.get(pathnew).createDirectory()
       /* if (!getDataPath().isDirectory) {
            getDataPath().toPath().createDirectory()
        }
        val newIndex = number + 1
        jsonReader.put("index", "$newIndex")*/
     for (sensor in App.instance.wrangler.sensors ){

         for (measurement in sensor.measurements){
             val dataFile = Paths.get(pathnew,sensor.title+measurement.description+".txt").toFile()
             dataFile.createNewFile()
             val writer: Writer = FileWriter(dataFile,true);
             for (dataPoint in measurement.dataPoints){
                 if(dataPoint.timestamp.toInt() != -1) {
                     writer.write("${dataPoint.value},${dataPoint.timestamp}\n");
                 }
                    dataPoint.timestamp = -1
                 writer.flush()
             }
             writer.close()
         }

     }
        /*val configWriter: Writer = FileWriter(configFile,true);
        incrementConfigNumber(configWriter)
        configWriter.close()*/
 }

    fun extractnewDataPoints(){
        j++
        var pathnew = path+i
        if (!Paths.get(pathnew).isDirectory()){
            Paths.get(pathnew).createDirectory()
        }

        /* if (!getDataPath().isDirectory) {
             getDataPath().toPath().createDirectory()
         }
         val newIndex = number + 1
         jsonReader.put("index", "$newIndex")*/
        for (sensor in App.instance.wrangler.sensors ){

            for (measurement in sensor.measurements){
                val dataFile = Paths.get(pathnew,sensor.title+measurement.description+"take"+j+".txt").toFile()
                dataFile.createNewFile()
                val writer: Writer = FileWriter(dataFile,true);
                for (dataPoint in measurement.dataPoints){
                    if(dataPoint.timestamp.toInt() != -1) {
                        writer.write("${dataPoint.value},${dataPoint.timestamp}\n");
                    }
                    dataPoint.timestamp = -1
                    writer.flush()
                }
                writer.close()
            }

        }
        /*val configWriter: Writer = FileWriter(configFile,true);
        incrementConfigNumber(configWriter)
        configWriter.close()*/
    }
/*
    fun getConfigPath(): File {
        var relPath = File(System.getProperty("user.dir"))
        return Paths.get(relPath.parent, "data", "config.json").toFile()
    }

    fun getDataPath(): File {
        var relPath = File(System.getProperty("user.dir"))
        return Paths.get(relPath.parent, "data", "Measurement$number").toFile()
    }



    fun incrementConfigNumber(writer: Writer){

    }
    fun closeWriter(){
      configReader.close()
    }*/

}


