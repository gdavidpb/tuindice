package com.gdavidpb.tuindice.ui.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.QuarterItem
import com.gdavidpb.tuindice.presentation.model.SubjectItem
import com.gdavidpb.tuindice.ui.adapters.QuarterAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.item_quarter.view.*
import kotlinx.android.synthetic.main.item_subject.view.*

open class QuarterViewHolder(
        itemView: View,
        private val manager: QuarterAdapter.AdapterManager
) : BaseViewHolder<QuarterItem>(itemView) {
    override fun bindView(item: QuarterItem) {
        super.bindView(item)

        with(itemView) {
            viewQuarterColor.backgroundColor = item.color

            tViewQuarterTitle.text = item.startEndDateText
            tViewQuarterGradeDiff.text = item.gradeDiffText
            tViewQuarterGradeSum.text = item.gradeSumText
            tViewQuarterCredits.text = item.creditsText

            /* Adjust subjects views */
            adjustViews(item, lLayoutQuarterContainer)

            /* Bind subjects view */
            bindViewSubjects(item, lLayoutQuarterContainer)
        }
    }

    private fun bindViewSubjects(quarterItem: QuarterItem, container: ViewGroup) {
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

        sBarGrade.progress = subjectItem.data.grade
    }

    private fun View.setEditable(quarterItem: QuarterItem, subjectItem: SubjectItem) {
        val isClickable = quarterItem.isEditable && !subjectItem.isRetired

        if (isClickable) {
            tViewSubjectCode.drawables(start = R.drawable.ic_open_in)

            onClickOnce { manager.onSubjectClicked(quarterItem, subjectItem) }
        } else {
            tViewSubjectCode.drawables(start = 0)

            setOnClickListener(null)
        }
    }

    private fun View.setGradeBar(quarterItem: QuarterItem, subjectItem: SubjectItem) {
        if (!quarterItem.isEditable) {
            sBarGrade.gone()

            sBarGrade.setOnSeekBarChangeListener(null)
        } else {
            sBarGrade.visible()

            sBarGrade.onSeekBarChange {
                onProgressChanged { grade, fromUser ->
                    if (fromUser)
                        manager.onSubjectGradeChanged(quarterItem, subjectItem, grade, false)
                }

                onStopTrackingTouch { grade ->
                    manager.onSubjectGradeChanged(quarterItem, subjectItem, grade, true)
                }
            }
        }
    }

    private fun adjustViews(quarterItem: QuarterItem, container: ViewGroup) {
        val size = quarterItem.data.subjects.size

        /* Add views */
        while (container.childCount < size)
            LayoutInflater
                    .from(container.context)
                    .inflate(R.layout.item_subject, container)

        /* Show / Hide views */
        (0 until container.childCount).forEach {
            val child = container.getChildAt(it)

            child.isVisible = it < size
        }
    }
}