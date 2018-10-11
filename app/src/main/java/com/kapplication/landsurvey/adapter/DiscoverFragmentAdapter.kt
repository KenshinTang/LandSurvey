package com.kapplication.landsurvey.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.kapplication.landsurvey.fragments.PagerChildFragment


class DiscoverFragmentAdapter(fm: FragmentManager, vararg titles: String) : FragmentPagerAdapter(fm) {
    internal var mTitles: Array<String>

    init {
        mTitles = titles as Array<String>
    }

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            PagerChildFragment.newInstance("1", "a")
        } else if (position == 1) {
            PagerChildFragment.newInstance("2", "b")
        } else {
            PagerChildFragment.newInstance("3", "c")
        }
    }

    override fun getCount(): Int {
        return mTitles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mTitles[position]
    }
}