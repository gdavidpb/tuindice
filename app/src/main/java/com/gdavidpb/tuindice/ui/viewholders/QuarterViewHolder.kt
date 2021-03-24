package com.gdavidpb.tuindice.ui.viewholders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.QuarterItem
import com.gdavidpb.tuindice.presentation.model.SubjectItem
import com.gdavidpb.tuindice.ui.adapters.QuarterAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.utils.STATUS_QUARTER_MOCK
import com.gdavidpb.tuindice.utils.STATUS_SUBJECT_RETIRED
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toQuarterItem
import com.gdavidpb.tuindice.utils.mappers.toSubjectItem
import kotlinx.android.synthetic.main.item_quarter.view.*
import kotlinx.android.synthetic.main.item_subject.view.*

open class QuarterViewHolder(
        itemView: View,
        private val manager: QuarterAdapter.AdapterManager
) : BaseViewHolder<QuarterItem>(itemView) {
    override fun bindView(item: QuarterItem) {
        super.bindView(item)

        with(itemView) {
            /* Quarter has not grade sum */
            if (item.data.gradeSum == 0.0 && item.subjectsItems.isNotEmpty()) {
                /* Compute quarter grade sum */
                computeGradeSum(quarterItem = item, context = context)

                return
            }

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
        val hasGradeBar = quarterItem.data.status == STATUS_QUARTER_CURRENT || quarterItem.data.status == STATUS_QUARTER_MOCK

        quarterItem.subjectsItems.forEachIndexed { index, subjectItem ->
            with(receiver = container.getChildAt(index)) {
                setSubjectData(subjectItem)
                setEditable(quarterItem, subjectItem, hasGradeBar)
                setGradeBar(subjectItem, quarterItem, hasGradeBar, index)
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

    private fun View.setEditable(quarterItem: QuarterItem, subjectItem: SubjectItem, hasGradeBar: Boolean) {
        val isEditable = hasGradeBar && subjectItem.data.status != STATUS_SUBJECT_RETIRED

        if (isEditable) {
            tViewSubjectCode.drawables(start = R.drawable.ic_open_in)

            onClickOnce { manager.onSubjectClicked(quarterItem, subjectItem) }
        } else {
            tViewSubjectCode.drawables(start = 0)

            setOnClickListener(null)
        }
    }

    private fun View.setGradeBar(subjectItem: SubjectItem, item: QuarterItem, hasGradeBar: Boolean, index: Int) {
        if (!hasGradeBar) {
            sBarGrade.gone()

            sBarGrade.setOnSeekBarChangeListener(null)
        } else {
            sBarGrade.visible()

            sBarGrade.onSeekBarChange {
                onProgressChanged { updatedGrade, fromUser ->
                    if (fromUser) {
                        val subject = subjectItem.data

                        val updatedStatus = updatedGrade.toSubjectStatus()

                        /* Create a updated subject */
                        val updatedSubject = subject.copy(
                                grade = updatedGrade,
                                status = updatedStatus
                        )

                        /* Set updated subject to list */
                        item.data.subjects[index] = updatedSubject

                        /* Compute quarter grade sum */
                        computeGradeSum(item, context)

                        val updatedItem = updatedSubject.toSubjectItem(context)

                        /* Notify subject changed */
                        manager.onSubjectChanged(updatedItem, false)
                    }
                }

                onStopTrackingTouch {
                    val updatedItem = manager.getItem(adapterPosition)
                    val updatedSubjectItem = updatedItem.subjectsItems.first { subject ->
                        subject.id == subjectItem.id
                    }

                    manager.onSubjectChanged(updatedSubjectItem, true)
                }
            }
        }
    }

    private fun adjustViews(quarterItem: QuarterItem, container: ViewGroup) {
        val size = quarterItem.data.subjects.size

        /* Add views */
        while (container.childCount < size)
            LayoutInflater.from(container.context).inflate(R.layout.item_subject, container)

        /* Show / Hide views */
        (0 until container.childCount).forEach {
            val child = container.getChildAt(it)

            child.visibleIf(it < size)
        }
    }

    private fun computeGradeSum(quarterItem: QuarterItem, context: Context) {
        val gradeSum = manager.computeGradeSum(quarter = quarterItem)

        /* Create updated quarter */
        val updatedQuarter = quarterItem.data.copy(
                gradeSum = gradeSum,
                grade = quarterItem.computeGrade(),
                credits = quarterItem.computeCredits()
        )

        val updatedItem = updatedQuarter.toQuarterItem(context)

        /* Rebind user interface */
        bindView(item = updatedItem)

        /* Notify quarter changed */
        manager.onQuarterChanged(item = updatedItem, position = adapterPosition)
    }
}