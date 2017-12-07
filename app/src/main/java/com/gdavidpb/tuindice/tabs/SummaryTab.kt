package com.gdavidpb.tuindice.tabs

import android.os.Bundle
import android.support.v7.content.res.AppCompatResources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdavidpb.tuindice.*
import com.gdavidpb.tuindice.abstracts.UpdatableFragment
import com.gdavidpb.tuindice.adapters.QuarterAdapter
import kotlinx.android.synthetic.main.view_tab_summary.*

class SummaryTab: UpdatableFragment() {

    private lateinit var adapter: QuarterAdapter

    override fun onInitialize(view: View?) {
        if (context == null) return

        /* Set up adapter */
        adapter = QuarterAdapter(context!!)

        lViewSummary.emptyView = tViewEmpty
        lViewSummary.adapter = adapter

        /* Set up drawables */
        val drawableAdd = AppCompatResources.getDrawable(context!!, R.drawable.ic_add)

        bAdd.setImageDrawable(drawableAdd)

        /* Set up listeners */
        /*
        bAdd.setOnClickListener { onAddClick() }

        lViewSummary.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                bAdd.visibility = if (scrollState == SCROLL_STATE_IDLE) View.VISIBLE else View.INVISIBLE
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) { }
        })
        */
    }

    override fun onUpdate(instanceState: Bundle?) {
        val account = instanceState?.getSerializable(Constants.EXTRA_ACCOUNT) as? DstAccount ?: return

        adapter.addAll(account.quarters)

        adapter.sort({ a, b -> b.startTime.compareTo(a.startTime) })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.view_tab_summary, container, false)

    /*
    private fun onAddClick() {
        val quarter = DstQuarter(QuarterType.GUESS,
                Date().time,
                Date().time,
                0.0,
                0.0)

        adapter.add(quarter)

        adapter.sort({ a, b -> b.startTime.compareTo(a.startTime) })
    }
    */
}