package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.QuarterItem
import com.gdavidpb.tuindice.presentation.model.SubjectItem
import com.gdavidpb.tuindice.ui.adapters.base.BaseAdapter
import com.gdavidpb.tuindice.ui.viewholders.QuarterViewHolder
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_quarter.view.*
import kotlin.math.roundToInt

class QuarterAdapter(
        private val manager: AdapterManager
) : BaseAdapter<QuarterItem>() {

    init {
        setHasStableIds(true)
    }

    interface AdapterManager {
        fun onSubjectOptionsClicked(quarterItem: QuarterItem, subjectItem: SubjectItem)
        fun onSubjectGradeChanged(quarterItem: QuarterItem, subjectItem: SubjectItem, grade: Int, dispatchChanges: Boolean)

        fun onSubmitQuarters(items: List<QuarterItem>)
    }

    private val averageSubjects by lazy {
        if (currentList.isNotEmpty())
            (currentList.sumOf { it.subjectsItems.size } / currentList.size.toFloat()).roundToInt()
        else
            0
    }

    override fun provideComparator() = compareBy(QuarterItem::id)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<QuarterItem> {
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

    fun submitQuarters(items: List<QuarterItem>) {
        submitList(items)
        manager.onSubmitQuarters(items)
    }

    fun getQuarter(position: Int): QuarterItem {
        return getItem(position)
    }

    fun addQuarter(item: QuarterItem, position: Int) {
        addItem(item, position)
    }

    fun removeQuarter(item: QuarterItem) {
        removeItem(item)
    }

    fun updateQuarter(item: QuarterItem) {
        updateItem(item)
    }

    fun getCurrentQuarter(): QuarterItem? {
        return currentList.find { it.isCurrent }
    }
}