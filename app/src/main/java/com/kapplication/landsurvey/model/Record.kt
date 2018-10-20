package com.kapplication.landsurvey.model

class Record {
    var name: String = "11111111111"
    var perimeter: Double = 2140.31234
        get() = field.format(2)
    var area: Double = 241.52234
        get() = field.format(2)
    var startTime: Long = 645465784354
    var endTime: Long = 54683285
    var latitude: Double = 0.0
        get() = field.format(6)
    var longitude: Double = 0.0
        get() = field.format(6)


    private fun Double.format(fractionDigits: Int): Double {
        return String.format("%.${fractionDigits}f", this).toDouble()
    }
}