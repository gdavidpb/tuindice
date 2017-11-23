package com.gdavidpb.tuindice.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdavidpb.tuindice.Constants
import com.gdavidpb.tuindice.DstAccount
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.abstracts.UpdatableFragment
import com.gdavidpb.tuindice.adapters.QuarterAdapter
import kotlinx.android.synthetic.main.view_tab_summary.*
import java.util.*

class SummaryTab: UpdatableFragment() {

    private lateinit var adapter: QuarterAdapter

    override fun onInitialize(view: View?) {
        adapter = QuarterAdapter(context!!)

        lViewSummary.emptyView = tViewEmpty
        lViewSummary.adapter = adapter
    }

    override fun onUpdate(instanceState: Bundle?) {
        val account = instanceState?.getSerializable(Constants.EXTRA_ACCOUNT) as? DstAccount ?: DstAccount()

        if (account.isEmpty())
            return

        adapter.addAll(account.quarters)

        adapter.sort({ a, b ->
            val calendarA = Calendar.getInstance()
            val calendarB = Calendar.getInstance()

            calendarA.timeInMillis = a.startTime
            calendarB.timeInMillis = b.startTime

            calendarB.compareTo(calendarA)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.view_tab_summary, container, false)
}