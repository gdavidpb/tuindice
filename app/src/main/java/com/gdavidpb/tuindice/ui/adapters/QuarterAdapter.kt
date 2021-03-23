package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.QuarterItem
import com.gdavidpb.tuindice.presentation.model.SubjectItem
import com.gdavidpb.tuindice.ui.adapters.base.BaseAdapter
import com.gdavidpb.tuindice.ui.viewholders.QuarterViewHolder
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.extensions.computeGradeSumUntil
import kotlinx.android.synthetic.main.item_quarter.view.*
import kotlin.math.roundToInt

class QuarterAdapter(
        private val manager: AdapterManager
) : BaseAdapter<QuarterItem, Nothing>() {

    interface AdapterManager {
        fun onSubjectClicked(quarterItem: QuarterItem, subjectItem: SubjectItem)
        fun onSubjectChanged(item: SubjectItem, dispatchChanges: Boolean)
        fun onQuarterChanged(item: QuarterItem, position: Int)

        fun computeGradeSum(quarter: QuarterItem): Double
        fun getItem(position: Int): QuarterItem
    }

    private val averageSubjects by lazy {
        if (currentList.isNotEmpty())
            (currentList.sumBy { it.subjectsItems.size } / currentList.size.toFloat()).roundToInt()
        else
            0
    }

    override fun provideComparator() = compareBy(QuarterItem::id)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<QuarterItem, Nothing> {
        val itemView = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_quarter, parent, false)

        /* Default inflation */
        with(itemView) {
            repeat(averageSubjects) {
                LayoutInflater
                        .from(lLayoutQuarterContainer.context)
                        .inflate(R.layout.item_subject, lLayoutQuarterContainer)
            }
        }

        return QuarterViewHolder(itemView, manager)
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].uid
    }

    fun computeGradeSum(until: QuarterItem): Double {
        return currentList.computeGradeSumUntil(until)
    }
}