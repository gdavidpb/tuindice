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
        private val callback: QuarterAdapter.AdapterCallback
) : BaseViewHolder<Quarter>(itemView) {
    override fun bindView(item: Quarter) {
        with(itemView) {

            val color = callback.resolveColor(item)

            viewQuarterColor.backgroundColor = color

            tViewQuarterTitle.text = item.toQuarterTitle()
            tViewQuarterGradeDiff.text = item.toQuarterGradeDiff(color, context)
            tViewQuarterGradeSum.text = item.toQuarterGradeSum(color, context)
            tViewQuarterCredits.text = item.toQuarterCredits(color, context)

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
}