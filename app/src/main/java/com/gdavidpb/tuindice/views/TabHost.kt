package com.gdavidpb.tuindice.views

import android.content.Context
import android.support.annotation.StringRes
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.adapters.TabHostAdapter

import kotlinx.android.synthetic.main.view_tab_host.view.*
import com.gdavidpb.tuindice.abstracts.UpdatableFragment

class TabHost(context: Context?, attrs: AttributeSet?)
    : RelativeLayout(context, attrs) {

    private lateinit var tabHostAdapter: TabHostAdapter

    init {
        LayoutInflater
                .from(context)
                .inflate(R.layout.view_tab_host, this)
    }

    fun setUp(fragmentManager: FragmentManager, init: TabHost.() -> Unit) {
        tabHostAdapter = TabHostAdapter(fragmentManager)

        tabHostContent.adapter = tabHostAdapter

        init()

        tabHostAdapter.notifyDataSetChanged()

        tabHostContent.addOnPageChangeListener(
                TabLayout.TabLayoutOnPageChangeListener(tabHostLayout))

        tabHostLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tabHostContent.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    fun addTab(@StringRes stringRes: Int, fragment: UpdatableFragment) {
        val tab = tabHostLayout.newTab().setText(stringRes)

        tabHostLayout.addTab(tab)

        tabHostAdapter.addItem(fragment)
    }

    fun setCurrentTab(position: Int) { tabHostContent.currentItem = position }

    fun getCurrentTab(): Int = tabHostContent.currentItem
}