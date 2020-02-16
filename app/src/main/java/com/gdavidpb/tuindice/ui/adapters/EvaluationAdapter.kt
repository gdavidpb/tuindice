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
        fun onEvaluationChanged(item: EvaluationItem, position: Int, dispatchChanges: Boolean)
        fun onEvaluationDoneChanged(item: EvaluationItem, position: Int, dispatchChanges: Boolean)

        fun getItem(position: Int): EvaluationItem
    }

    override fun provideComparator() = compareBy(EvaluationItem::id)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<EvaluationItem> {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_evaluation, parent, false)

        return EvaluationViewHolder(itemView, manager)
    }

    fun computeGradeSum(): Int {
        return items.sumBy { evaluation -> evaluation.data.grade }
    }
}