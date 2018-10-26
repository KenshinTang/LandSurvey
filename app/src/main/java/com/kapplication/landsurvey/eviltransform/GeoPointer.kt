package com.kapplication.landsurvey.eviltransform

import java.text.DecimalFormat

open class GeoPointer {
    var longitude: Double = 0.toDouble()
    var latitude: Double = 0.toDouble()

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        } else {
            if (other is GeoPointer) {
                val otherPointer = other as GeoPointer?
                return df.format(latitude) == df.format(otherPointer!!.latitude) && df.format(longitude) == df.format(otherPointer.longitude)
            } else {
                return false
            }
        }
    }

    override fun toString(): String {
        val sb = StringBuilder("latitude:$latitude")
        sb.append(" longitude:$longitude")
        return sb.toString()
    }

    fun distance(target: GeoPointer): Double {
        val earthR = 6371000.0
        val x = (Math.cos(this.latitude * Math.PI / 180) * Math.cos(target.latitude * Math.PI / 180)
                * Math.cos((this.longitude - target.longitude) * Math.PI / 180))
        val y = Math.sin(this.latitude * Math.PI / 180) * Math.sin(target.latitude * Math.PI / 180)
        var s = x + y
        if (s > 1) {
            s = 1.0
        }
        if (s < -1) {
            s = -1.0
        }
        val alpha = Math.acos(s)
        return alpha * earthR
    }

    companion object {
        internal var df = DecimalFormat("0.000000")
    }
}