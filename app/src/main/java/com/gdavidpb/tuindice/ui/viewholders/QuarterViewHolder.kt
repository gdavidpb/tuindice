package com.gdavidpb.tuindice.ui.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.ui.adapters.QuarterAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.*
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

    private fun bindViewSubjects(item: Quarter, container: ViewGroup) {
        item.subjects.forEachIndexed { index, subject ->
            val child = container.getChildAt(index)

            with(child) {
                tViewSubjectCode.text = subject.toSubjectCode(context)
                tViewSubjectGrade.text = subject.toSubjectGrade(container.context)
                tViewSubjectName.text = subject.toSubjectName()
                tViewSubjectCredits.text = subject.toSubjectCredits(container.context)

                sBarGrade.progress = subject.grade

                if (item.status == STATUS_QUARTER_COMPLETED || item.status == STATUS_QUARTER_RETIRED) {
                    sBarGrade.setOnSeekBarChangeListener(null)

                    sBarGrade.visibility = View.GONE
                } else {
                    sBarGrade.onSeekBarChange { progress, fromUser ->
                        if (fromUser) {
                            /* Create a updated subject */
                            val updatedSubject = subject.copy(
                                    grade = progress,
                                    status = when {
                                        progress == 0 -> STATUS_SUBJECT_RETIRED
                                        subject.status == STATUS_SUBJECT_RETIRED -> STATUS_SUBJECT_OK
                                        else -> subject.status
                                    }
                            )

                            /* Set updated subject to list */
                            item.subjects[index] = updatedSubject

                            /* Compute quarter grade sum */
                            computeGradeSum(quarter = item)

                            /* Notify subject changed */
                            manager.onSubjectChanged(updatedSubject)
                        }
                    }

                    sBarGrade.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun adjustViews(item: Quarter, container: ViewGroup) {
        val size = item.subjects.size

        /* Add views */
        while (container.childCount < size)
            LayoutInflater.from(container.context).inflate(R.layout.item_subject, container)

        /* Show / Hide views */
        (0 until container.childCount).forEach {
            val child = container.getChildAt(it)

            child.visibility = if (it < size) View.VISIBLE else View.GONE
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