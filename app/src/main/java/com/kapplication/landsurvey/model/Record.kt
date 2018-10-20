package com.kapplication.landsurvey.model

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.kapplication.landsurvey.utils.Utils
import java.io.File
import java.util.*

class Record {
    var name: String = ""
    var perimeter: Double = 0.0
        get() = field.format(2)
    var area: Double = 0.0
        get() = field.format(2)
    var startTime: String = ""
    var endTime: String = ""
    var points: LinkedList<LatLng> = LinkedList()
    var altitudeRange: LinkedList<Double> = LinkedList()
    var altitudeRangeString: String = ""

    companion object {
        fun from(file: File): Record {
            val record = Record()
            val contents: List<String> = file.readLines()
            for (content in contents) {
                if (content.contains("Perimeter")) {
                    record.perimeter = content.substringAfter(": ").toDouble()
                }
                if (content.contains("Area")) {
                    record.area = content.substringAfter(": ").toDouble()
                }
                if (content.contains("Start Time")) {
                    record.startTime = content.substringAfter(": ")
                }
                if (content.contains("End Time")) {
                    record.endTime = content.substringAfter(": ")
                }
                if (content.contains("Altitude Range")) {
                    record.altitudeRangeString = content.substringAfter(": ")
                }
                if (content.contains(", ")) {
                    record.points.add(LatLng(content.substringBefore(",").toDouble(), content.substringAfter(", ").toDouble()))
                }
            }
            record.name = file.name
            return record
        }
    }

    private fun Double.format(fractionDigits: Int): Double {
        return String.format("%.${fractionDigits}f", this).toDouble()
    }

    override fun toString(): String {
        /*
        Name: 111111
        Perimeter(m): 305.93m
        Area(㎡): 8843.26
        Start Time: 2018-10-20 06:44:46
        End Time: 2018-10-20 06:44:54
        Coordinates(5){
            37.42187650366643, -122.08305235952139
            37.42147123889159, -122.08261214196682
            37.4212622151379, -122.08326157182456
            37.42153514412658, -122.08420135080813
            37.42234993109213, -122.08383623510599
        }
        */
        val sb = StringBuilder()
        sb.append("Name: $name\n")
                .append("Perimeter(m): $perimeter\n")
                .append("Area(㎡): $area\n")
                .append("Start Time: $startTime\n")
                .append("End Time: $endTime\n")
                .append("Altitude Range: ${if (altitudeRange.isEmpty()) 0.0 else altitudeRange.first} ~ ${if (altitudeRange.isEmpty()) 0.0 else altitudeRange.last}\n")
                .append("Coordinates(${points.size}){\n")
        for (point in points) {
            sb.append("\t${point.latitude}, ${point.longitude}\n")
        }
        sb.append("}\n")
        return sb.toString()
    }

    fun writeToFile() : Boolean {
        Log.i("kenshin", this.toString())
        try {
            val file = File(Utils.getLandSurveyDir(), "$name.txt")
            if (file.exists()) {
                return false
            }
            file.writeText(toString())
            return true
        } catch (e: Exception) {
            Log.e("kenshin", "save file failed.", e)
            return false
        }
    }
}