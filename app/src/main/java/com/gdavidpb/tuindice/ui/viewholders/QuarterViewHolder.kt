package com.gdavidpb.tuindice.ui.viewholders

import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.QuarterItem
import com.gdavidpb.tuindice.presentation.model.SubjectItem
import com.gdavidpb.tuindice.ui.adapters.QuarterAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.extensions.backgroundColor
import com.gdavidpb.tuindice.utils.extensions.onClickOnce
import com.gdavidpb.tuindice.utils.extensions.onSeekBarChange
import kotlinx.android.synthetic.main.item_quarter.view.*
import kotlinx.android.synthetic.main.item_subject.view.*

class QuarterViewHolder(
        itemView: View,
        private val manager: QuarterAdapter.AdapterManager
) : BaseViewHolder<QuarterItem>(itemView) {
    override fun bindView(item: QuarterItem) {
        super.bindView(item)

        itemView as CardView

        with(itemView) {
            setData(title = item.TitleText, grade = item.gradeDiffText, gradeSum = item.gradeSumText, credits = item.creditsText)
            setStates(color = item.color, isSwiping = item.isSwiping)

            adjustViews(count = item.subjectsItems.size)

            bindViewSubjects(quarterItem = item)
        }
    }

    private fun CardView.setStates(@ColorInt color: Int, isSwiping: Boolean) {
        isEnabled = !isSwiping
        viewQuarterColor.backgroundColor = color
    }

    private fun CardView.setData(title: CharSequence, grade: CharSequence, gradeSum: CharSequence, credits: CharSequence) {
        tViewQuarterTitle.text = title
        tViewQuarterGradeDiff.text = grade
        tViewQuarterGradeSum.text = gradeSum
        tViewQuarterCredits.text = credits
    }

    private fun CardView.adjustViews(count: Int) {
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
        tViewSubjectName.text = subjectItem.nameText

        tViewSubjectCode.text = subjectItem.codeText
        tViewSubjectGrade.text = subjectItem.gradeText
        tViewSubjectCredits.text = subjectItem.creditsText

        sBarSubjectGrade.progress = subjectItem.data.grade
    }

    private fun View.setEditable(quarterItem: QuarterItem, subjectItem: SubjectItem) {
        val areOptionsAvailable = quarterItem.isEditable

        btnSubjectOptions.isVisible = areOptionsAvailable

        if (areOptionsAvailable)
            btnSubjectOptions.onClickOnce { manager.onSubjectOptionsClicked(quarterItem, subjectItem) }
        else
            btnSubjectOptions.setOnClickListener(null)
    }

    private fun View.setGradeBar(quarterItem: QuarterItem, subjectItem: SubjectItem) {
        if (quarterItem.isEditable) {
            sBarSubjectGrade.isVisible = true

            sBarSubjectGrade.onSeekBarChange {
                onProgressChanged { grade, fromUser ->
                    if (fromUser)
                        manager.onSubjectGradeChanged(quarterItem, subjectItem, grade, false)
                }

                onStopTrackingTouch { grade ->
                    manager.onSubjectGradeChanged(quarterItem, subjectItem, grade, true)
                }
            }
        } else {
            sBarSubjectGrade.isVisible = false

            sBarSubjectGrade.setOnSeekBarChangeListener(null)
        }
    }
}