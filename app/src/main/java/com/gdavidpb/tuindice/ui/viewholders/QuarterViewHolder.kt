package com.gdavidpb.tuindice.ui.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.ui.adapters.QuarterAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.*
import kotlinx.android.synthetic.main.item_quarter.view.*
import kotlinx.android.synthetic.main.item_subject.view.*
import org.jetbrains.anko.backgroundColor

open class QuarterViewHolder(
        itemView: View,
        private val manager: QuarterAdapter.AdapterManager
) : BaseViewHolder<Quarter>(itemView) {
    override fun bindView(item: Quarter) {
        with(itemView) {
            /* Quarter has not grade sum */
            if (item.gradeSum == 0.0) {
                /* Compute quarter grade sum */
                computeGradeSum(quarter = item)

                return
            }

            val color = manager.resolveColor(item)
            val font = manager.resolveFont("Code.ttf")

            viewQuarterColor.backgroundColor = color

            tViewQuarterTitle.text = item.toQuarterTitle()
            tViewQuarterGradeDiff.text = item.toQuarterGradeDiff(color, context)
            tViewQuarterGradeSum.text = item.toQuarterGradeSum(color, context)
            tViewQuarterCredits.text = item.toQuarterCredits(color, font, context)

            /* Adjust subjects views */
            adjustViews(item, lLayoutQuarterContainer)

            /* Bind subjects view */
            bindViewSubjects(item, lLayoutQuarterContainer)
        }
    }

    private fun bindViewSubjects(quarter: Quarter, container: ViewGroup) {
        val hasGradeBar = quarter.status == STATUS_QUARTER_CURRENT || quarter.status == STATUS_QUARTER_GUESS

        quarter.subjects.forEachIndexed { index, subject ->
            with(receiver = container.getChildAt(index)) {
                setSubjectData(subject)
                setEditable(subject, hasGradeBar)
                setGradeBar(subject, quarter, hasGradeBar, index)
            }
        }
    }

    private fun View.setSubjectData(subject: Subject) {
        tViewSubjectName.text = subject.name

        tViewSubjectCode.text = subject.toSubjectCode(context)
        tViewSubjectGrade.text = subject.toSubjectGrade(context)
        tViewSubjectCredits.text = subject.toSubjectCredits(context)

        sBarGrade.progress = subject.grade
    }

    private fun View.setEditable(subject: Subject, hasGradeBar: Boolean) {
        val isEditable = hasGradeBar && subject.status != STATUS_SUBJECT_GAVE_UP

        if (isEditable) {
            tViewSubjectCode.drawables(left = R.drawable.ic_open_in)

            onClickOnce { manager.onSubjectClicked(subject) }
        } else {
            tViewSubjectCode.drawables(left = 0)

            setOnClickListener(null)
        }
    }

    private fun View.setGradeBar(subject: Subject, quarter: Quarter, hasGradeBar: Boolean, index: Int) {
        if (!hasGradeBar) {
            sBarGrade.gone()

            sBarGrade.setOnSeekBarChangeListener(null)
        } else {
            sBarGrade.visible()

            sBarGrade.onSeekBarChange { progress, fromUser ->
                if (fromUser) {
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
                    quarter.subjects[index] = updatedSubject

                    /* Compute quarter grade sum */
                    computeGradeSum(quarter = quarter)

                    /* Notify subject changed */
                    manager.onSubjectChanged(updatedSubject)
                }
            }
        }
    }

    private fun adjustViews(quarter: Quarter, container: ViewGroup) {
        val size = quarter.subjects.size

        /* Add views */
        while (container.childCount < size)
            LayoutInflater.from(container.context).inflate(R.layout.item_subject, container)

        /* Show / Hide views */
        (0 until container.childCount).forEach {
            val child = container.getChildAt(it)

            child.visibleIf(it < size)
        }
    }

    private fun computeGradeSum(quarter: Quarter) {
        val gradeSum = manager.computeGradeSum(quarter = quarter)

        /* Create updated quarter */
        val updatedQuarter = quarter.copy(
                gradeSum = gradeSum,
                grade = quarter.computeGrade(),
                credits = quarter.computeCredits()
        )

        /* Rebind user interface */
        bindView(item = updatedQuarter)

        /* Notify quarter changed */
        manager.onQuarterChanged(item = updatedQuarter, position = adapterPosition)
    }
}