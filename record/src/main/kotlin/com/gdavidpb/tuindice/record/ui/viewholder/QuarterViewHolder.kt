package com.gdavidpb.tuindice.record.ui.viewholder

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.ui.viewholder.BaseViewHolder
import com.gdavidpb.tuindice.base.utils.extension.backgroundColor
import com.gdavidpb.tuindice.base.utils.extension.onClickOnce
import com.gdavidpb.tuindice.base.utils.extension.onSeekBarChange
import com.gdavidpb.tuindice.base.utils.extension.view
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.ui.adapter.QuarterAdapter
import com.gdavidpb.tuindice.record.ui.custom.SubjectGradeSeekBar
import com.google.android.material.textview.MaterialTextView

class QuarterViewHolder(
	itemView: View,
	private val manager: QuarterAdapter.AdapterManager
) : BaseViewHolder<QuarterItem>(itemView) {

	override fun bindView(item: QuarterItem) {
		super.bindView(item)

		with(itemView as CardView) {
			setStates(color = item.color)
			setData(
				title = item.TitleText,
				grade = item.gradeDiffText,
				gradeSum = item.gradeSumText,
				credits = item.creditsText
			)

			adjustViews(count = item.subjectsItems.size)

			bindViewSubjects(quarterItem = item)
		}
	}

	private fun CardView.setStates(@ColorInt color: Int) {
		val viewQuarterColor by view<View>(R.id.viewQuarterColor)

		viewQuarterColor.backgroundColor = color
	}

	private fun CardView.setData(
		title: CharSequence,
		grade: CharSequence,
		gradeSum: CharSequence,
		credits: CharSequence
	) {
		val tViewQuarterTitle by view<MaterialTextView>(R.id.tViewQuarterTitle)
		val tViewQuarterGradeDiff by view<MaterialTextView>(R.id.tViewQuarterGradeDiff)
		val tViewQuarterGradeSum by view<MaterialTextView>(R.id.tViewQuarterGradeSum)
		val tViewQuarterCredits by view<MaterialTextView>(R.id.tViewQuarterCredits)

		tViewQuarterTitle.text = title
		tViewQuarterGradeDiff.text = grade
		tViewQuarterGradeSum.text = gradeSum
		tViewQuarterCredits.text = credits
	}

	private fun CardView.adjustViews(count: Int) {
		val lLayoutQuarterContainer by view<LinearLayout>(R.id.lLayoutQuarterContainer)
		val container = lLayoutQuarterContainer

		/* Add views */
		while (container.childCount < count)
			LayoutInflater
				.from(container.context)
				.inflate(R.layout.item_subject, container)

		/* Show / Hide views */
		(0 until container.childCount).forEach {
			val child = container.getChildAt(it)

			child.isVisible = it < count
		}
	}

	private fun CardView.bindViewSubjects(quarterItem: QuarterItem) {
		val lLayoutQuarterContainer by view<LinearLayout>(R.id.lLayoutQuarterContainer)
		val container = lLayoutQuarterContainer

		quarterItem.subjectsItems.forEachIndexed { index, subjectItem ->
			with(receiver = container.getChildAt(index)) {
				setSubjectData(subjectItem)
				setEditable(quarterItem, subjectItem)
				setGradeBar(quarterItem, subjectItem)
			}
		}
	}

	private fun View.setSubjectData(subjectItem: SubjectItem) {
		val tViewSubjectName by view<MaterialTextView>(R.id.tViewSubjectName)
		val tViewSubjectCode by view<MaterialTextView>(R.id.tViewSubjectCode)
		val tViewSubjectGrade by view<MaterialTextView>(R.id.tViewSubjectGrade)
		val tViewSubjectCredits by view<MaterialTextView>(R.id.tViewSubjectCredits)
		val sBarSubjectGrade by view<SubjectGradeSeekBar>(R.id.sBarSubjectGrade)

		tViewSubjectName.text = subjectItem.nameText

		tViewSubjectCode.text = subjectItem.codeText
		tViewSubjectGrade.text = subjectItem.gradeText
		tViewSubjectCredits.text = subjectItem.creditsText

		sBarSubjectGrade.progress = subjectItem.data.grade
	}

	private fun View.setEditable(quarterItem: QuarterItem, subjectItem: SubjectItem) {
		val btnSubjectOptions by view<AppCompatImageButton>(R.id.btnSubjectOptions)

		with(btnSubjectOptions) {
			isVisible = quarterItem.isEditable

			if (subjectItem.isRetired) {
				alpha = 0.25f
				isEnabled = false
			} else {
				alpha = 1f
				isEnabled = true
			}

			if (quarterItem.isEditable)
				onClickOnce { manager.onSubjectOptionsClicked(subjectItem) }
			else
				setOnClickListener(null)
		}
	}

	private fun View.setGradeBar(quarterItem: QuarterItem, subjectItem: SubjectItem) {
		val sBarSubjectGrade by view<SubjectGradeSeekBar>(R.id.sBarSubjectGrade)

		if (quarterItem.isEditable) {
			sBarSubjectGrade.isVisible = true

			sBarSubjectGrade.onSeekBarChange {
				onProgressChanged { grade, fromUser ->
					if (fromUser)
						manager.onSubjectGradeChanged(subjectItem, grade, false)
				}

				onStopTrackingTouch { grade ->
					manager.onSubjectGradeChanged(subjectItem, grade, true)
				}
			}
		} else {
			sBarSubjectGrade.isVisible = false

			sBarSubjectGrade.setOnSeekBarChangeListener(null)
		}
	}
}