package com.gdavidpb.tuindice.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TabHostAdapter(fragmentManager: androidx.fragment.app.FragmentManager)
    : androidx.fragment.app.FragmentStatePagerAdapter(fragmentManager) {

    private val fragmentList: ArrayList<androidx.fragment.app.Fragment> = ArrayList()

    override fun getCount(): Int = fragmentList.size

    override fun getItem(position: Int): androidx.fragment.app.Fragment = fragmentList[position]

    override fun getItemPosition(item: Any): Int = fragmentList.indexOf(item)

    fun addItem(fragment: androidx.fragment.app.Fragment) = fragmentList.add(fragment)
}