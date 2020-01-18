package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.ui.adapters.EvaluationAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.extensions.onSeekBarChange
import com.gdavidpb.tuindice.utils.mappers.toWeeksLeft
import com.gdavidpb.tuindice.utils.mappers.toEvaluationTypeName
import kotlinx.android.synthetic.main.item_evaluation.view.*

open class EvaluationViewHolder(
        itemView: View,
        private val manager: EvaluationAdapter.AdapterManager
) : BaseViewHolder<Evaluation>(itemView) {
    override fun bindView(item: Evaluation) {
        with(itemView) {
            cBoxEvaluation.setOnCheckedChangeListener(null)

            cBoxEvaluation.isChecked = item.done

            tViewEvaluationType.text = item.type.toEvaluationTypeName(context)
            tViewEvaluationDate.text = item.date.toWeeksLeft()
            tViewEvaluationNotes.text = item.notes
            tViewEvaluationGrade.text = context.getString(R.string.evaluation_grade_max, item.grade, item.maxGrade)

            sBarGrade.max = item.maxGrade
            sBarGrade.progress = item.grade

            cBoxEvaluation.setOnCheckedChangeListener { _, isChecked ->
                val updatedEvaluation = item.copy(done = isChecked)

                manager.onEvaluationChanged(updatedEvaluation, adapterPosition)
            }

            sBarGrade.onSeekBarChange { progress, fromUser ->
                if (fromUser) {
                    val updatedEvaluation = item.copy(grade = progress)

                    manager.onEvaluationChanged(updatedEvaluation, adapterPosition)
                }
            }
        }
    }
}