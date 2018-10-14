package com.kapplication.landsurvey.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.kapplication.landsurvey.R
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DrawerFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DrawerFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class DrawerFragment : Fragment() {
    private val TAG: String = "DrawerFragment"

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private var mViewPager: ViewPager? = null
    private var mTabLayout: SmartTabLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_drawer, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
        val titles = arrayOf(getString(R.string.mode_automatic), getString(R.string.mode_piling), getString(R.string.mode_manual))
        mViewPager = view?.findViewById(R.id.viewpager)
//        mViewPager?.adapter = ViewPagerAdatper(fragmentManager!!)
        val fragmentPagerItems = FragmentPagerItems.with(activity)
                ?.add(titles[0], OperationFragment::class.java)
                ?.add(titles[1], OperationFragment::class.java)
                ?.add(titles[2], OperationFragment::class.java)
                ?.create()
        mViewPager?.adapter = FragmentPagerItemAdapter(fragmentManager!!, fragmentPagerItems)
        mTabLayout = view?.findViewById(R.id.tablayout)
        with(mTabLayout!!) {
            setCustomTabView{ container, position, adapter ->
                val inflater = LayoutInflater.from(container.context)
                val tab = inflater.inflate(R.layout.custom_tab, container, false)
                val customText = tab.findViewById<View>(R.id.custom_tab) as TextView
                customText.text = adapter.getPageTitle(position)
                tab
            }
            setViewPager(mViewPager)

            setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    Log.i(TAG, "position:$position")
                    switchOperationUI(position)
                }
            })

            setOnTabClickListener {
                Log.e(TAG, "position:$it")
                val fragment = (mViewPager?.adapter as FragmentPagerItemAdapter).getPage(it)
                val textView_Latlng = fragment?.view?.findViewById<TextView>(R.id.textView_Latlng)
                when (it) {
                    0 -> textView_Latlng?.text = "00.000000, 000.000000"
                    1 -> textView_Latlng?.text = "11.111111, 111.111111"
                    2 -> textView_Latlng?.text = "22.222222, 222.222222"
                }
            }
        }

    }

    private fun switchOperationUI(position: Int) {
        val root = (mViewPager?.adapter as FragmentPagerItemAdapter).getPage(position)?.view
        when (position) {
            0 -> {
                root?.findViewById<LinearLayout>(R.id.linear_auto_buttons)?.visibility = View.VISIBLE
                root?.findViewById<LinearLayout>(R.id.linear_piling_buttons)?.visibility = View.INVISIBLE
                root?.findViewById<LinearLayout>(R.id.linear_manual_buttons)?.visibility = View.INVISIBLE
            }
            1 -> {
                root?.findViewById<LinearLayout>(R.id.linear_auto_buttons)?.visibility = View.INVISIBLE
                root?.findViewById<LinearLayout>(R.id.linear_piling_buttons)?.visibility = View.VISIBLE
                root?.findViewById<LinearLayout>(R.id.linear_manual_buttons)?.visibility = View.INVISIBLE
            }
            2 -> {
                root?.findViewById<LinearLayout>(R.id.linear_auto_buttons)?.visibility = View.INVISIBLE
                root?.findViewById<LinearLayout>(R.id.linear_piling_buttons)?.visibility = View.INVISIBLE
                root?.findViewById<LinearLayout>(R.id.linear_manual_buttons)?.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
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
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DrawerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                DrawerFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
