package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.ui.adapters.base.BaseAdapter
import com.gdavidpb.tuindice.ui.viewholders.EvaluationViewHolder
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder

open class EvaluationAdapter(
        private val manager: AdapterManager
) : BaseAdapter<Evaluation>() {

    interface AdapterManager {
        fun onEvaluationChanged(item: Evaluation, position: Int)
        fun onEvaluationDoneChanged(item: Evaluation, position: Int)

        fun resolveDoneColor(): Int
        fun resolvePendingColor(): Int
    }

    override fun provideComparator() = compareBy(Evaluation::id)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Evaluation> {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_evaluation, parent, false)

        return EvaluationViewHolder(itemView, manager)
    }

    fun computeGradeSum(): Int {
        return items.sumBy { evaluation -> evaluation.grade }
    }
}