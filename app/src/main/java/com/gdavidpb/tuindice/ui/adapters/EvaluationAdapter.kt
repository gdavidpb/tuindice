package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.ui.adapters.base.BaseAdapter
import com.gdavidpb.tuindice.ui.adapters.payloads.EvaluationPayload
import com.gdavidpb.tuindice.ui.viewholders.EvaluationViewHolder
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder

class EvaluationAdapter(
        private val manager: AdapterManager
) : BaseAdapter<EvaluationItem, EvaluationPayload>() {

    private val adapterSorter = compareBy(
            EvaluationItem::isDone,
            EvaluationItem::date
    )

    interface AdapterManager {
        fun onEvaluationClicked(item: EvaluationItem)
        fun onEvaluationGradeChanged(item: EvaluationItem, grade: Double, dispatchChanges: Boolean)
        fun onEvaluationDoneChanged(item: EvaluationItem, done: Boolean, dispatchChanges: Boolean)

        fun onEvaluationAdded(item: EvaluationItem)
        fun onEvaluationRemoved(item: EvaluationItem)
        fun onEvaluationUpdated(item: EvaluationItem)

        fun onSubmitEvaluations(items: List<EvaluationItem>)
    }

    override fun provideComparator() = compareBy(EvaluationItem::id)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<EvaluationItem, EvaluationPayload> {
        val itemView = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_evaluation, parent, false)

        return EvaluationViewHolder(itemView, manager)
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].uid
    }

    fun submitEvaluations(items: List<EvaluationItem>) {
        submitSortedList(items)
        manager.onSubmitEvaluations(currentList)
    }

    fun getEvaluation(position: Int): EvaluationItem {
        return getItem(position)
    }

    fun addEvaluation(item: EvaluationItem, position: Int) {
        addItem(item, position)
        submitSortedList()
        manager.onEvaluationAdded(item)
    }

    fun removeEvaluation(item: EvaluationItem) {
        removeItem(item)
        manager.onEvaluationRemoved(item)
    }

    fun setEvaluationGrade(item: EvaluationItem, grade: Double) {
        updateItem(
                item = item.copy(grade = grade),
                EvaluationPayload.UpdateGrade(grade = grade)
        )

        manager.onEvaluationUpdated(item)
    }

    fun setEvaluationDone(item: EvaluationItem, done: Boolean) {
        updateItem(
                item = item.copy(isDone = done),
                EvaluationPayload.UpdateStates(isDone = done, isSwiping = item.isSwiping)
        )

        submitSortedList()

        manager.onEvaluationUpdated(item)
    }

    fun setEvaluationSwiping(item: EvaluationItem, swiping: Boolean) {
        updateItem(
                item = item.copy(isSwiping = swiping),
                EvaluationPayload.UpdateStates(isSwiping = swiping, isDone = item.isDone)
        )
    }

    fun computeGradeSum(): Double {
        return currentList.sumByDouble { evaluation -> evaluation.data.grade }
    }

    private fun submitSortedList(list: List<EvaluationItem> = currentList) {
        super.submitList(list.sortedWith(adapterSorter))
    }
}