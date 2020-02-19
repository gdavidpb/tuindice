package com.gdavidpb.tuindice.ui.viewholders

import android.graphics.Color
import android.view.View
import androidx.cardview.widget.CardView
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.ui.adapters.EvaluationAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.ResourcesManager
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toEvaluationItem
import kotlinx.android.synthetic.main.item_evaluation.view.*

open class EvaluationViewHolder(
        itemView: View,
        private val manager: EvaluationAdapter.AdapterManager
) : BaseViewHolder<EvaluationItem>(itemView) {
    override fun bindView(item: EvaluationItem) {
        with(itemView) {
            this as CardView

            cBoxEvaluation.setOnCheckedChangeListener(null)

            cBoxEvaluation.isChecked = item.isDone

            sBarGrade.max = item.data.maxGrade
            sBarGrade.progress = item.data.grade

            tViewEvaluationType.text = item.typeText
            tViewEvaluationNotes.text = item.notesText
            tViewEvaluationGrade.text = item.gradeText
            tViewEvaluationDate.text = item.dateText

            cBoxEvaluation.isEnabled = !item.isSwiping
            sBarGrade.isEnabled = !item.isDone && !item.isSwiping

            val cardColor = if (item.isSwiping)
                ResourcesManager.getColor(R.color.color_disabled, context)
            else
                item.color

            val cardBackgroundColor = if (item.isSwiping)
                ResourcesManager.getColor(R.color.color_swipe, context)
            else
                Color.WHITE

            viewEvaluationColor.backgroundColor = cardColor

            setCardBackgroundColor(cardBackgroundColor)

            if (item.isDone) {
                tViewEvaluationDate.strikeThrough()
                tViewEvaluationNotes.strikeThrough()

                setOnClickListener(null)
            } else {
                tViewEvaluationDate.clearStrikeThrough()
                tViewEvaluationNotes.clearStrikeThrough()

                onClickOnce { manager.onEvaluationClicked(item, adapterPosition) }
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
                    val updatedItem = manager.getItem(adapterPosition)

                    manager.onEvaluationChanged(updatedItem, adapterPosition, true)
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