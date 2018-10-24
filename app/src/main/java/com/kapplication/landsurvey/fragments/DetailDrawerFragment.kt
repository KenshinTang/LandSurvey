package com.kapplication.landsurvey.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.maps.model.Marker

import com.kapplication.landsurvey.R
import com.kapplication.landsurvey.model.Record
import com.kapplication.landsurvey.utils.Utils

private const val ARG_RECORD = "record"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DetailDrawerFragment.OnShowSurveyDetailListener] interface
 * to handle interaction events.
 * Use the [DetailDrawerFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */

private const val TAG = "DetailDrawerFragment"

class DetailDrawerFragment : Fragment() {
    private var record: Record? = null
    private var listener: OnShowSurveyDetailListener? = null

    private var mLatitudeTextView: TextView? = null
    private var mLongitudeTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            record = it.getParcelable(ARG_RECORD)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_detail_drawer, container, false)
        view.findViewById<TextView>(R.id.up).setOnClickListener {
            fragmentManager?.popBackStack()
        }
        mLatitudeTextView = view.findViewById(R.id.textView_latitude_content)
        mLongitudeTextView = view.findViewById(R.id.textView_longitude_content)
        view.findViewById<TextView>(R.id.textView_title).text = record?.name
        view.findViewById<TextView>(R.id.textView_altitude_content).text = record?.altitudeRange
        view.findViewById<TextView>(R.id.textView_area_content).text = Utils.convertArea(this.context!!, record?.area!!, 2)
        view.findViewById<TextView>(R.id.textView_perimeter_content).text = "${record?.perimeter}m"
        mLatitudeTextView?.text = record?.points?.last?.latitude.toString()
        mLongitudeTextView?.text = record?.points?.last?.longitude.toString()
        view.findViewById<TextView>(R.id.textView_start_time_content).text = record?.startTime
        view.findViewById<TextView>(R.id.textView_end_time_content).text = record?.endTime
        listener?.onShowSurveyDetail(record)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnShowSurveyDetailListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnShowSurveyDetailListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun onMarkerClick(marker: Marker?) {
        mLatitudeTextView?.text = marker?.position?.latitude.toString()
        mLongitudeTextView?.text = marker?.position?.longitude.toString()
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnShowSurveyDetailListener {
        fun onShowSurveyDetail(record: Record?)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment DetailDrawerFragment.
         */
        @JvmStatic
        fun newInstance(r: Record) =
                DetailDrawerFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_RECORD, r)
                    }
                }
    }
}
