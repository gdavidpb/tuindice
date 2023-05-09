package com.gdavidpb.tuindice.evaluations.ui.viewholder

import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.ui.adapter.EvaluationAdapter
import com.gdavidpb.tuindice.base.ui.viewholder.BaseViewHolder
import com.gdavidpb.tuindice.base.utils.extension.*
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.ui.custom.EvaluationGradeSeekBar
import com.gdavidpb.tuindice.evaluations.ui.custom.TintedCheckBox
import com.google.android.material.textview.MaterialTextView

class EvaluationViewHolder(
	itemView: View,
	private val manager: EvaluationAdapter.AdapterManager
) : BaseViewHolder<EvaluationItem>(itemView) {

	init {
		with(itemView) {
			val cBoxEvaluation by view<TintedCheckBox>(R.id.cBoxEvaluation)
			val sBarEvaluationGrade by view<EvaluationGradeSeekBar>(R.id.sBarEvaluationGrade)
			val btnEvaluationOptions by view<AppCompatImageButton>(R.id.btnEvaluationOptions)

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

						manager.onEvaluationGradeChanged(item, grade)
					}
				}

				onStopTrackingTouch {
					val item = getItem()
					val grade = sBarEvaluationGrade.grade

					manager.onEvaluationGradeChanged(item, grade)
				}
			}

			cBoxEvaluation.onCheckedChange { isChecked ->
				val item = getItem()

				manager.onEvaluationDoneChanged(item, isChecked)
			}
		}
	}

	override fun bindView(item: EvaluationItem) {
		super.bindView(item)

		with(itemView as CardView) {
			setStates(isDone = item.isDone)
			setGrades(grade = item.grade, maxGrade = item.maxGrade)
			setAdditionalData(type = item.typeText, name = item.nameText, date = item.dateText)
		}
	}

	private fun CardView.setAdditionalData(
		type: CharSequence,
		name: CharSequence,
		date: CharSequence
	) {
		val tViewEvaluationName by view<MaterialTextView>(R.id.tViewEvaluationName)
		val tViewEvaluationType by view<MaterialTextView>(R.id.tViewEvaluationType)
		val tViewEvaluationDate by view<MaterialTextView>(R.id.tViewEvaluationDate)

		tViewEvaluationName.text = name
		tViewEvaluationType.text = type
		tViewEvaluationDate.text = date
	}

	private fun CardView.setGrades(grade: Double, maxGrade: Double) {
		val tViewEvaluationGrade by view<MaterialTextView>(R.id.tViewEvaluationGrade)
		val sBarEvaluationGrade by view<EvaluationGradeSeekBar>(R.id.sBarEvaluationGrade)

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

		val viewEvaluationColor by view<View>(R.id.viewEvaluationColor)
		val cBoxEvaluation by view<TintedCheckBox>(R.id.cBoxEvaluation)
		val sBarEvaluationGrade by view<EvaluationGradeSeekBar>(R.id.sBarEvaluationGrade)
		val tViewEvaluationType by view<MaterialTextView>(R.id.tViewEvaluationType)
		val tViewEvaluationDate by view<MaterialTextView>(R.id.tViewEvaluationDate)

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