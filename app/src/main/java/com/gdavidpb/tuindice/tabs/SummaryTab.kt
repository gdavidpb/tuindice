package com.gdavidpb.tuindice.tabs

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.content.res.AppCompatResources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdavidpb.tuindice.*
import com.gdavidpb.tuindice.activities.MainActivity
import com.gdavidpb.tuindice.adapters.QuarterAdapter
import com.gdavidpb.tuindice.models.DstService
import com.gdavidpb.tuindice.models.LifecycleHandler
import com.gdavidpb.tuindice.models.database
import com.gdavidpb.tuindice.models.preferences
import kotlinx.android.synthetic.main.view_tab_summary.*
import org.jetbrains.anko.coroutines.experimental.asReference
import org.jetbrains.anko.longToast
import java.io.File
import java.util.*

class SummaryTab: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.view_tab_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
        val adapter = QuarterAdapter(context)

        lViewSummary.emptyView = tViewEmpty
        lViewSummary.adapter = adapter

        /* Set up drawables */
        val drawableAdd = AppCompatResources.getDrawable(context, R.drawable.ic_add)

        bAdd.setImageDrawable(drawableAdd)

        val accentColor = ContextCompat.getColor(context, R.color.colorAccent)

        swipeRefresh.setColorSchemeColors(accentColor)

        /* Set up listeners */
        swipeRefresh.setOnRefreshListener {
            refreshData(true)
        }

        val account = context.database.getActiveAccount()

        /* Set up adapter */
        adapter.addAll(account.quarters)

        adapter.sort({ a, b -> b.startTime.compareTo(a.startTime) })

        /* Check for updates */
        val now = Calendar.getInstance()
        val lastUpdate = Calendar.getInstance()

        lastUpdate.timeInMillis = account.lastUpdate

        if (lastUpdate.monthsTo(now) >= 3)
            refreshData(false)
    }

    private fun refreshData(ignoreCooldown: Boolean) {
        val context = context ?: return

        if (!ignoreCooldown)
            if (context.preferences.isCooldown()) {
                swipeRefresh.isRefreshing = false
                return
            } else
                context.preferences.setCooldown()

        swipeRefresh.isRefreshing = true

        val thisRef = this.asReference()

        async( /* on Task */ {
            try {
                val account = context.database.getActiveAccount()

                DstService().collectFrom(account.usbId, account.password, false, context)
            } catch (exception: Exception) {
                exception.printStackTrace()

                val writer = File(context.filesDir, "report-stacktrace").printWriter()

                writer.use { exception.printStackTrace(writer) }

                DstResponse<DstAccount>(exception)
            }
        }, /* on Response */ {
            if (LifecycleHandler.isAppVisible()) {
                val weakRef = thisRef()
                val weakActivity = (weakRef.activity as MainActivity)
                val weakContext = (weakActivity as Context)

                weakRef.swipeRefresh.isRefreshing = false
                weakContext.longToast(R.string.toastRefresh)

                if (result != null) {
                    val account = weakContext.database.getActiveAccount()

                    if (result.lastUpdate > account.lastUpdate) {
                        weakContext.database.updateAccount(account.id, result)

                        weakActivity.recreate()
                    }
                }
            }
        })
    }
}