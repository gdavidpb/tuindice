package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import androidx.cardview.widget.CardView
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.ui.adapters.EvaluationAdapter
import com.gdavidpb.tuindice.ui.adapters.payloads.EvaluationPayload
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.ResourcesManager
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.item_evaluation.view.*

open class EvaluationViewHolder(
        itemView: View,
        private val manager: EvaluationAdapter.AdapterManager
) : BaseViewHolder<EvaluationItem, EvaluationPayload>(itemView) {

    init {
        with(itemView) {
            onClickOnce {
                val item = getItem()

                if (!item.isDone)
                    manager.onEvaluationClicked(item)
            }

            sBarGrade.onSeekBarChange {
                onProgressChanged { progress, fromUser ->
                    if (fromUser) {
                        val item = getItem()
                        val grade = progress.toGrade()

                        manager.onEvaluationGradeChanged(item, grade, false)
                    }
                }

                onStopTrackingTouch { progress ->
                    val item = getItem()
                    val grade = progress.toGrade()

                    manager.onEvaluationGradeChanged(item, grade, true)
                }
            }

            cBoxEvaluation.setOnCheckedChangeListener { _, isChecked ->
                val item = getItem()

                manager.onEvaluationDoneChanged(item, isChecked, true)
            }
        }
    }

    override fun bindView(item: EvaluationItem) {
        super.bindView(item)

        itemView as CardView

        with(itemView) {
            setGrades(grade = item.grade, maxGrade = item.maxGrade)
            setStates(isDone = item.isDone, isSwiping = item.isSwiping)
            setAdditionalData(type = item.typeText, notes = item.notesText, date = item.dateText)
        }
    }

    override fun bindPayload(item: EvaluationItem, payload: List<EvaluationPayload>) {
        super.bindPayload(item, payload)

        itemView as CardView

        with(itemView) {
            payload.forEach {
                when (it) {
                    is EvaluationPayload.UpdateGrade -> setGrades(grade = it.grade, maxGrade = item.maxGrade)
                    is EvaluationPayload.UpdateStates -> setStates(isDone = it.isDone, isSwiping = it.isSwiping)
                }
            }
        }
    }

    private fun CardView.setAdditionalData(type: CharSequence, notes: CharSequence, date: CharSequence) {
        tViewEvaluationType.text = type
        tViewEvaluationNotes.text = notes
        tViewEvaluationDate.text = date
    }

    private fun CardView.setGrades(grade: Double, maxGrade: Double) {
        val gradeText = context.getString(R.string.evaluation_grade_max, grade, maxGrade)

        sBarGrade.max = maxGrade.toProgress()
        sBarGrade.progress = grade.toProgress()
        tViewEvaluationGrade.text = gradeText
    }

    private fun CardView.setStates(isDone: Boolean, isSwiping: Boolean) {
        val cardColor = when {
            isSwiping -> R.color.color_disabled
            isDone -> R.color.color_retired
            else -> R.color.color_approved
        }.let { resource -> ResourcesManager.getColor(resource, context) }

        val cardBackgroundColor = when {
            isSwiping -> R.color.color_swipe
            else -> android.R.color.white
        }.let { resource -> ResourcesManager.getColor(resource, context) }

        viewEvaluationColor.backgroundColor = cardColor
        setCardBackgroundColor(cardBackgroundColor)
        cBoxEvaluation.setChecked(checked = isDone, notify = false)

        cBoxEvaluation.isEnabled = !isSwiping
        sBarGrade.isEnabled = !isDone && !isSwiping

        if (isDone) {
            tViewEvaluationDate.strikeThrough()
            tViewEvaluationNotes.strikeThrough()
        } else {
            tViewEvaluationDate.clearStrikeThrough()
            tViewEvaluationNotes.clearStrikeThrough()
        }
    }
}