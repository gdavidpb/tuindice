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
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.toQuarterItem
import com.gdavidpb.tuindice.utils.mappers.toSubjectItem
import kotlinx.android.synthetic.main.item_quarter.view.*
import kotlinx.android.synthetic.main.item_subject.view.*
import org.jetbrains.anko.backgroundColor

open class QuarterViewHolder(
        itemView: View,
        private val manager: QuarterAdapter.AdapterManager
) : BaseViewHolder<QuarterItem>(itemView) {
    override fun bindView(item: QuarterItem) {
        with(itemView) {
            /* Quarter has not grade sum */
            if (item.data.gradeSum == 0.0) {
                /* Compute quarter grade sum */
                computeGradeSum(item = item, context = context)

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

    private fun bindViewSubjects(item: QuarterItem, container: ViewGroup) {
        val hasGradeBar = item.data.status == STATUS_QUARTER_CURRENT || item.data.status == STATUS_QUARTER_GUESS

        item.subjectsItems.forEachIndexed { index, subjectItem ->
            with(receiver = container.getChildAt(index)) {
                setSubjectData(subjectItem)
                setEditable(subjectItem, hasGradeBar)
                setGradeBar(subjectItem, item, hasGradeBar, index)
            }
        }
    }

    private fun View.setSubjectData(item: SubjectItem) {
        tViewSubjectName.text = item.nameText

        tViewSubjectCode.text = item.codeText
        tViewSubjectGrade.text = item.gradeText
        tViewSubjectCredits.text = item.creditsText

        sBarGrade.progress = item.data.grade
    }

    private fun View.setEditable(item: SubjectItem, hasGradeBar: Boolean) {
        val isEditable = hasGradeBar && item.data.status != STATUS_SUBJECT_GAVE_UP

        if (isEditable) {
            tViewSubjectCode.drawables(left = R.drawable.ic_open_in)

            onClickOnce { manager.onSubjectClicked(item) }
        } else {
            tViewSubjectCode.drawables(left = 0)

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
                onProgressChanged { progress, fromUser ->
                    if (fromUser) {
                        val subject = subjectItem.data

                        /* Create a updated subject */
                        val updatedSubject = subject.copy(
                                grade = progress,
                                status = when {
                                    progress == 0 && subject.status != STATUS_SUBJECT_GAVE_UP -> STATUS_SUBJECT_RETIRED
                                    subject.status == STATUS_SUBJECT_RETIRED -> STATUS_SUBJECT_OK
                                    else -> subject.status
                                }
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

    private fun adjustViews(item: QuarterItem, container: ViewGroup) {
        val size = item.data.subjects.size

        /* Add views */
        while (container.childCount < size)
            LayoutInflater.from(container.context).inflate(R.layout.item_subject, container)

        /* Show / Hide views */
        (0 until container.childCount).forEach {
            val child = container.getChildAt(it)

            child.visibleIf(it < size)
        }
    }

    private fun computeGradeSum(item: QuarterItem, context: Context) {
        val gradeSum = manager.computeGradeSum(quarter = item)

        /* Create updated quarter */
        val updatedQuarter = item.data.copy(
                gradeSum = gradeSum,
                grade = item.computeGrade(),
                credits = item.computeCredits()
        )

        val updatedItem = updatedQuarter.toQuarterItem(context)

        /* Rebind user interface */
        bindView(item = updatedItem)

        /* Notify quarter changed */
        manager.onQuarterChanged(item = updatedItem, position = adapterPosition)
    }
}