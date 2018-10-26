package com.kapplication.landsurvey.eviltransform

class WGSPointer : GeoPointer {

    constructor() {}

    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }

    fun toGCJPointer(): GCJPointer {
        if (TransformUtil.outOfChina(this.latitude, this.longitude)) {
            return GCJPointer(this.latitude, this.longitude)
        }
        val delta = TransformUtil.delta(this.latitude, this.longitude)
        return GCJPointer(this.latitude + delta[0], this.longitude + delta[1])
    }
}