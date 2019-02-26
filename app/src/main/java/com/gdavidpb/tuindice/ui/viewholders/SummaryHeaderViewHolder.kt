package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.model.app.CircleTransform
import com.gdavidpb.tuindice.data.model.database.SummaryBase
import com.gdavidpb.tuindice.data.model.database.SummaryHeader
import com.gdavidpb.tuindice.ui.adapters.SummaryAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.animateGrade
import kotlinx.android.synthetic.main.item_summary_header.view.*

open class SummaryHeaderViewHolder(
        itemView: View,
        private val callback: SummaryAdapter.AdapterCallback
) : BaseViewHolder<SummaryBase>(itemView) {
    override fun bindView(item: SummaryBase) {
        item as SummaryHeader

        with(itemView) {
            tViewName.text = item.name
            tViewCareer.text = item.careerName

            if (item.grade > 0.0) {
                lLayoutGrade.visibility = View.VISIBLE
                tViewGrade.animateGrade(item.grade)
            } else
                lLayoutGrade.visibility = View.GONE

            if (item.photoUrl.isNotEmpty()) {
                callback.provideImageLoader { picasso ->
                    picasso.cancelTag(item.uid)

                    picasso.load(item.photoUrl)
                            .tag(item.uid)
                            .transform(CircleTransform())
                            .into(iViewProfile)
                }
            } else {
                iViewProfile.setImageResource(R.mipmap.ic_launcher_round)
            }
        }
    }
}