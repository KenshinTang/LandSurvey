package com.kapplication.landsurvey.model

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import java.util.*

class Path {

    private var mList = LinkedList<LatLng>()
    private var mListener: OnPathChangeListener? = null


    interface OnPathChangeListener {
        fun onPathChanged()
    }

    fun setOnPathChangeListener(listener: OnPathChangeListener) {
        mListener = listener
    }

    fun add(point: LatLng) {
        mList.add(point)
        mListener?.onPathChanged()
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
            Log.i("kenshin", "Path(${mList.size}):$l")
        }
    }
}