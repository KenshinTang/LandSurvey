package com.kapplication.landsurvey.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.kapplication.landsurvey.R
import com.kapplication.landsurvey.model.Record

private const val ARG_RECORD = "record"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DetailDrawerFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DetailDrawerFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */

private const val TAG = "DetailDrawerFragment"

class DetailDrawerFragment : Fragment() {
    private var record: Record? = null
    private var listener: OnFragmentInteractionListener? = null

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
        view.findViewById<TextView>(R.id.textView_title).text = record?.name
        view.findViewById<TextView>(R.id.textView_altitude_content).text = record?.altitudeRange
        view.findViewById<TextView>(R.id.textView_area_content).text = "${record?.area}㎡"
        view.findViewById<TextView>(R.id.textView_perimeter_content).text = "${record?.perimeter}m"
//        view.findViewById<TextView>(R.id.textView_latitude_content).text = mCurrentLatlng.latitude
//        view.findViewById<TextView>(R.id.textView_longitude_content).text = mCurrentLatlng.longitude
        view.findViewById<TextView>(R.id.textView_start_time_content).text = record?.startTime
        view.findViewById<TextView>(R.id.textView_end_time_content).text = record?.endTime
        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
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
