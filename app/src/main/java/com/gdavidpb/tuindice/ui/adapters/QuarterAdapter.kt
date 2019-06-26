package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.ui.adapters.base.BaseAdapter
import com.gdavidpb.tuindice.ui.viewholders.QuarterViewHolder
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_quarter.view.*
import kotlin.math.roundToInt

open class QuarterAdapter(
        private val callback: AdapterCallback
) : BaseAdapter<Quarter>() {

    interface AdapterCallback {
        fun onSubjectChanged(item: Subject)
        fun onQuarterChanged(item: Quarter, position: Int)

        fun computeGradeSum(quarter: Quarter): Double

        fun resolveColor(item: Quarter): Int
    }

    private val averageSubjects by lazy {
        if (items.isNotEmpty()) (items.sumBy { it.subjects.size } / items.size.toFloat()).roundToInt() else 0
    }

    override fun provideComparator() = compareBy(Quarter::id)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Quarter> {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_quarter, parent, false)

        /* Default inflation */
        with(itemView) {
            repeat(averageSubjects) {
                LayoutInflater.from(lLayoutQuarterContainer.context).inflate(R.layout.item_subject, lLayoutQuarterContainer)
            }
        }

        return QuarterViewHolder(itemView, callback)
    }

    fun getQuarters(): List<Quarter> {
        return items.toList()
    }
}