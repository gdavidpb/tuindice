package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.model.app.CircleTransform
import com.gdavidpb.tuindice.presentation.model.SummaryBase
import com.gdavidpb.tuindice.presentation.model.SummaryHeader
import com.gdavidpb.tuindice.ui.adapters.SummaryAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.animateGrade
import com.gdavidpb.tuindice.utils.drawables
import com.gdavidpb.tuindice.utils.getCompatDrawable
import com.gdavidpb.tuindice.utils.formatLastUpdate
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

            tViewLastUpdate.text = context.getString(R.string.text_last_update, item.lastUpdate.formatLastUpdate())
            tViewLastUpdate.drawables(left = context.getCompatDrawable(R.drawable.ic_sync, R.color.colorSecondaryText))

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