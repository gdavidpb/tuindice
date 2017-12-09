package com.gdavidpb.tuindice.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class TabHostAdapter(fragmentManager: FragmentManager)
    : FragmentStatePagerAdapter(fragmentManager) {

    private val fragmentList: ArrayList<Fragment> = ArrayList()

    override fun getCount(): Int = fragmentList.size

    override fun getItem(position: Int): Fragment = fragmentList[position]

    override fun getItemPosition(`object`: Any): Int = fragmentList.indexOf(`object`)

    fun addItem(fragment: Fragment) { fragmentList.add(fragment) }
}