package com.kapplication.landsurvey.eviltransform

class GCJPointer : GeoPointer {


    constructor() {}

    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }

    fun toWGSPointer(): WGSPointer {
        if (TransformUtil.outOfChina(this.latitude, this.longitude)) {
            return WGSPointer(this.latitude, this.longitude)
        }
        val delta = TransformUtil.delta(this.latitude, this.longitude)
        return WGSPointer(this.latitude - delta[0], this.longitude - delta[1])
    }

    fun toExactWGSPointer(): WGSPointer {
        val initDelta = 0.01
        val threshold = 0.000001
        var dLat = initDelta
        var dLng = initDelta
        var mLat = this.latitude - dLat
        var mLng = this.longitude - dLng
        var pLat = this.latitude + dLat
        var pLng = this.longitude + dLng
        var wgsLat: Double
        var wgsLng: Double
        var currentWGSPointer: WGSPointer? = null
        for (i in 0..29) {
            wgsLat = (mLat + pLat) / 2
            wgsLng = (mLng + pLng) / 2
            currentWGSPointer = WGSPointer(wgsLat, wgsLng)
            val tmp = currentWGSPointer.toGCJPointer()
            dLat = tmp.latitude - this.latitude
            dLng = tmp.longitude - this.longitude
            if (Math.abs(dLat) < threshold && Math.abs(dLng) < threshold) {
                return currentWGSPointer
            } else {
                println(dLat.toString() + ":" + dLng)
            }
            if (dLat > 0) {
                pLat = wgsLat
            } else {
                mLat = wgsLat
            }
            if (dLng > 0) {
                pLng = wgsLng
            } else {
                mLng = wgsLng
            }
        }
        return currentWGSPointer!!
    }
}