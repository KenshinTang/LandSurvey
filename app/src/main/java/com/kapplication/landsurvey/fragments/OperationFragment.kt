package com.kapplication.landsurvey.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.kapplication.landsurvey.R
import mehdi.sakout.fancybuttons.FancyButton

private const val ARG_TYPE = "ARG_TYPE"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [OperationFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [OperationFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class OperationFragment : Fragment() {
    private val TAG: String = "OperationFragment"

    private var mType: Int = 0
    private var listener: OnFragmentInteractionListener? = null

    private var mButtonPiling: FancyButton? = null
    private var mButtonUndoPiling: ImageView? = null
    private var mIsMeasuring: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mType = it.getInt(ARG_TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_operation, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
        mButtonPiling = view?.findViewById(R.id.imageButton_piling_start)
        mButtonPiling?.setOnClickListener {
            startPiling()
        }

        mButtonUndoPiling = view?.findViewById(R.id.imageButton_piling_undo)
        mButtonUndoPiling?.setOnClickListener {
            piling()
        }
    }

    private fun startPiling() {
        Log.d(TAG, "startPiling")
        with(mButtonPiling!!) {
            setBackgroundColor(ContextCompat.getColor(activity as Context, if (mIsMeasuring) R.color.defaultButtonColor else android.R.color.holo_red_light))
            setIconResource(if (mIsMeasuring) R.drawable.btn_thumb_tack else R.drawable.history)
            setText(getString(if (mIsMeasuring) R.string.start_piling else R.string.stop_piling))
            mIsMeasuring = !mIsMeasuring
        }
    }

    private fun piling() {
        if (mIsMeasuring) {
            
        }
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
         * @param type Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OperationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(type: Int) =
                OperationFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_TYPE, type)
                    }
                }
    }
}
