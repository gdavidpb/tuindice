package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.EvaluationItem
import com.gdavidpb.tuindice.ui.adapters.EvaluationAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toEvaluationDate
import com.gdavidpb.tuindice.utils.mappers.toEvaluationTypeName
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

            sBarGrade.max = item.maxGrade
            sBarGrade.progress = item.grade

            tViewEvaluationType.text = item.type.toEvaluationTypeName(context)
            tViewEvaluationNotes.text = if (item.notes.isNotEmpty()) item.notes else "─"
            tViewEvaluationGrade.text = context.getString(R.string.evaluation_grade_max, item.grade, item.maxGrade)
            tViewEvaluationDate.text = item.date.toEvaluationDate()

            val doneColor = manager.resolveDoneColor()
            val pendingColor = manager.resolvePendingColor()

            isEnabled = !item.isLoading

            cBoxEvaluation.isEnabled = !item.isLoading
            sBarGrade.isEnabled = !item.isDone && !item.isLoading

            if (item.isDone) {
                tViewEvaluationDate.strikeThrough()
                tViewEvaluationNotes.strikeThrough()

                viewEvaluationColor.backgroundColor = doneColor
            } else {
                tViewEvaluationDate.clearStrikeThrough()
                tViewEvaluationNotes.clearStrikeThrough()

                viewEvaluationColor.backgroundColor = pendingColor
            }

            if (item.isLoading) {
                viewEvaluationOverlay.visible()
                pBarEvaluation.visible()
            } else {
                viewEvaluationOverlay.gone()
                pBarEvaluation.gone()
            }

            sBarGrade.onSeekBarChange { progress, fromUser ->
                if (fromUser) {
                    val updatedEvaluation = item.copy(grade = progress)

                    manager.onEvaluationChanged(updatedEvaluation, adapterPosition)
                }
            }

            cBoxEvaluation.setOnCheckedChangeListener { _, isChecked ->
                val updatedEvaluation = item.copy(isDone = isChecked)

                manager.onEvaluationDoneChanged(updatedEvaluation, adapterPosition)
            }
        }
    }
}