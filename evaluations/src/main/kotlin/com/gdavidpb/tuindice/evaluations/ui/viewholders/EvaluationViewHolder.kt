package com.gdavidpb.tuindice.evaluations.ui.viewholders

import android.view.View
import androidx.cardview.widget.CardView
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.ui.adapters.EvaluationAdapter
import com.gdavidpb.tuindice.base.ui.viewholders.BaseViewHolder
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.evaluations.R
import kotlinx.android.synthetic.main.item_evaluation.view.*

class EvaluationViewHolder(
	itemView: View,
	private val manager: EvaluationAdapter.AdapterManager
) : BaseViewHolder<EvaluationItem>(itemView) {

	init {
		with(itemView) {
			onClickOnce {
				val item = getItem()

				if (!item.isDone)
					manager.onEvaluationClicked(item)
			}

			btnEvaluationOptions.onClickOnce {
				val item = getItem()

				manager.onEvaluationOptionsClicked(item, absoluteAdapterPosition)
			}

			sBarEvaluationGrade.onSeekBarChange {
				onProgressChanged { _, fromUser ->
					if (fromUser) {
						val item = getItem()
						val grade = sBarEvaluationGrade.grade

						manager.onEvaluationGradeChanged(item, grade, false)
					}
				}

				onStopTrackingTouch {
					val item = getItem()
					val grade = sBarEvaluationGrade.grade

					manager.onEvaluationGradeChanged(item, grade, true)
				}
			}

			cBoxEvaluation.onCheckedChange { isChecked ->
				val item = getItem()

				manager.onEvaluationDoneChanged(item, isChecked, true)
			}
		}
	}

	override fun bindView(item: EvaluationItem) {
		super.bindView(item)

		with(itemView as CardView) {
			setStates(isDone = item.isDone)
			setGrades(grade = item.grade, maxGrade = item.maxGrade)
			setAdditionalData(type = item.typeText, notes = item.notesText, date = item.dateText)
		}
	}

	private fun CardView.setAdditionalData(
		type: CharSequence,
		notes: CharSequence,
		date: CharSequence
	) {
		tViewEvaluationName.text = notes
		tViewEvaluationType.text = type
		tViewEvaluationDate.text = date
	}

	private fun CardView.setGrades(grade: Double, maxGrade: Double) {
		val gradeText = context.getString(
			R.string.evaluation_grade_max,
			grade.formatGrade(2),
			maxGrade.formatGrade()
		)

		sBarEvaluationGrade.maxGrade = maxGrade
		sBarEvaluationGrade.grade = grade
		tViewEvaluationGrade.text = gradeText
	}

	private fun CardView.setStates(isDone: Boolean) {
		val cardColor = when {
			isDone -> R.color.color_retired
			else -> R.color.color_approved
		}.let { resource -> context.getCompatColor(resource) }

		viewEvaluationColor.backgroundColor = cardColor
		cBoxEvaluation.setChecked(checked = isDone, notify = false)

		sBarEvaluationGrade.isEnabled = !isDone

		if (isDone) {
			tViewEvaluationDate.strikeThrough()
			tViewEvaluationType.strikeThrough()
		} else {
			tViewEvaluationDate.clearStrikeThrough()
			tViewEvaluationType.clearStrikeThrough()
		}
	}
}