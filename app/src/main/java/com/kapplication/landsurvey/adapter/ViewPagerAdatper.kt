package com.kapplication.landsurvey.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.kapplication.landsurvey.fragments.OperationFragment

class ViewPagerAdatper(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private var mTitles = arrayOf("Automatic\nmode", "Piling\nmode", "Manual\nmode")

    override fun getItem(position: Int): Fragment {
        return OperationFragment.newInstance(position)
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mTitles[position]
    }

}