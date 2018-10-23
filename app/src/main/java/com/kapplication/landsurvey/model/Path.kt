package com.kapplication.landsurvey.model

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.util.*

private const val TAG = "Path"

class Path {

    private var mList = LinkedList<LatLng>()
    private var mListener: OnPathChangeListener? = null


    interface OnPathChangeListener {
        fun onPathChanged()
    }

    fun setOnPathChangeListener(listener: OnPathChangeListener) {
        mListener = listener
    }

    fun add(point: LatLng) : Boolean {
        return mList.add(point).also {
            if (it) {
                mListener?.onPathChanged()
            }
        }
    }

    // add point to list only if the distance is larger tan $limit meter
    fun add(point: LatLng, limit: Int) : Boolean {
        if (mList.isEmpty() || (SphericalUtil.computeDistanceBetween(point, mList.last) > limit)) {
            return add(point)
        }
        return false
    }

    fun remove() {
        mList.removeLast()
        mListener?.onPathChanged()
    }

    fun remove(point: LatLng) {
        mList.remove(point)
        mListener?.onPathChanged()
    }

    fun clear() {
        mList.clear()
        mListener?.onPathChanged()
    }

    fun size() : Int {
        return mList.size
    }

    fun getList() : LinkedList<LatLng> {
        return mList
    }

    fun print() {
        for (l in mList) {
            Log.i(TAG, "Path(${mList.size}):$l")
        }
    }
}