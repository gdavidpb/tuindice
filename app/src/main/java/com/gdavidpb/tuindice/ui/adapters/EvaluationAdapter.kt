package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.ui.adapters.base.BaseAdapter
import com.gdavidpb.tuindice.ui.viewholders.EvaluationViewHolder
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder

open class EvaluationAdapter(
        private val manager: AdapterManager
) : BaseAdapter<EvaluationItem>() {

    interface AdapterManager {
        fun onEvaluationClicked(item: EvaluationItem, position: Int)

        fun onEvaluationChanged(item: EvaluationItem, position: Int, dispatchChanges: Boolean)
        fun onEvaluationDoneChanged(item: EvaluationItem, position: Int, dispatchChanges: Boolean)

        fun onDataChanged()
        fun onDataUpdated()

        fun getItem(position: Int): EvaluationItem
    }

    override fun provideComparator() = compareBy(EvaluationItem::id)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<EvaluationItem> {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_evaluation, parent, false)

        return EvaluationViewHolder(itemView, manager)
    }

    override fun swapItems(new: List<EvaluationItem>) {
        super.swapItems(new)

        manager.onDataChanged()
    }

    override fun addSortedItem(item: EvaluationItem, comparator: Comparator<EvaluationItem>) {
        super.addSortedItem(item, comparator)

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

    fun updateItem(item: EvaluationItem) {
        items.indexOfFirst { evaluation ->
            evaluation.id == item.id
        }.also { position ->
            replaceItemAt(item, position)
        }
    }

    fun computeGradeSum(): Int {
        return items.sumBy { evaluation -> evaluation.data.grade }
    }
}