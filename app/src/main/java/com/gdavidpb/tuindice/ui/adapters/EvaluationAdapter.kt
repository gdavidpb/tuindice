package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.ui.adapters.base.BaseAdapter
import com.gdavidpb.tuindice.ui.viewholders.EvaluationViewHolder
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder

class EvaluationAdapter(
        private val manager: AdapterManager
) : BaseAdapter<EvaluationItem, Nothing>() {

    private val adapterSorter = compareBy(
            EvaluationItem::isDone,
            EvaluationItem::date
    ).thenByDescending(EvaluationItem::grade)

    interface AdapterManager {
        fun onEvaluationClicked(item: EvaluationItem, position: Int)

        fun onEvaluationChanged(item: EvaluationItem, position: Int, dispatchChanges: Boolean)

        fun onDataChanged()
        fun onDataUpdated()

        fun getItem(position: Int): EvaluationItem
    }

    override fun provideComparator() = compareBy(EvaluationItem::id)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<EvaluationItem, Nothing> {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_evaluation, parent, false)

        return EvaluationViewHolder(itemView, manager)
    }

    override fun submitList(list: List<EvaluationItem>) {
        super.submitList(list.sortedWith(adapterSorter))

        manager.onDataChanged()
    }

    override fun addItem(item: EvaluationItem, position: Int) {
        super.addItem(item, position)

        manager.onDataChanged()
    }

    override fun addItemAt(item: EvaluationItem, position: Int, notifyChange: Boolean) {
        super.addItemAt(item, position, notifyChange)

        manager.onDataChanged()
    }

    override fun removeItemAt(position: Int, notifyChange: Boolean) {
        super.removeItemAt(position, notifyChange)

        manager.onDataChanged()
    }

    override fun replaceItemAt(item: EvaluationItem, position: Int, notifyChange: Boolean) {
        super.replaceItemAt(item, position, notifyChange)

        manager.onDataUpdated()
    }

    fun ensureSorting() {
        super.submitList(currentList.sortedWith(adapterSorter))
    }

    fun computeGradeSum(): Double {
        return currentList.sumByDouble { evaluation -> evaluation.data.grade }
    }
}