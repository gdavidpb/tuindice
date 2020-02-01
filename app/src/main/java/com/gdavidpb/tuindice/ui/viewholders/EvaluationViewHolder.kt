package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.ui.adapters.EvaluationAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toEvaluationItem
import kotlinx.android.synthetic.main.item_evaluation.view.*
import org.jetbrains.anko.backgroundColor

open class EvaluationViewHolder(
        itemView: View,
        private val manager: EvaluationAdapter.AdapterManager
) : BaseViewHolder<EvaluationItem>(itemView) {
    override fun bindView(item: EvaluationItem) {
        with(itemView) {
            cBoxEvaluation.setOnCheckedChangeListener(null)

            cBoxEvaluation.isChecked = item.isDone

            sBarGrade.max = item.data.maxGrade
            sBarGrade.progress = item.data.grade

            tViewEvaluationType.text = item.typeText
            tViewEvaluationNotes.text = item.notesText
            tViewEvaluationGrade.text = item.gradesText
            tViewEvaluationDate.text = item.dateText

            isEnabled = !item.isLoading

            cBoxEvaluation.isEnabled = !item.isLoading
            sBarGrade.isEnabled = !item.isDone && !item.isLoading

            viewEvaluationColor.backgroundColor = item.color

            if (item.isDone) {
                tViewEvaluationDate.strikeThrough()
                tViewEvaluationNotes.strikeThrough()
            } else {
                tViewEvaluationDate.clearStrikeThrough()
                tViewEvaluationNotes.clearStrikeThrough()
            }

            if (item.isLoading) {
                viewEvaluationOverlay.visible()
                pBarEvaluation.visible()
            } else {
                viewEvaluationOverlay.gone()
                pBarEvaluation.gone()
            }

            sBarGrade.onSeekBarChange {
                onProgressChanged { progress, fromUser ->
                    if (fromUser) {
                        val updatedEvaluation = item.data.copy(grade = progress)
                        val updatedItem = updatedEvaluation.toEvaluationItem(context)

                        manager.onEvaluationChanged(updatedItem, adapterPosition, false)
                    }
                }

                onStopTrackingTouch {
                    manager.onEvaluationChanged(item, adapterPosition, true)
                }
            }

            cBoxEvaluation.setOnCheckedChangeListener { _, isChecked ->
                val updatedEvaluation = item.data.copy(isDone = isChecked)
                val updatedItem = updatedEvaluation.toEvaluationItem(context)

                manager.onEvaluationDoneChanged(updatedItem, adapterPosition, true)
            }
        }
    }
}