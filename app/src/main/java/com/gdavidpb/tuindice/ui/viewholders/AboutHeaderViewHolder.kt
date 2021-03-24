package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import android.widget.TextView
import com.gdavidpb.tuindice.presentation.model.AboutHeaderItem
import com.gdavidpb.tuindice.presentation.model.AboutItemBase
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder

open class AboutHeaderViewHolder(
        itemView: View
) : BaseViewHolder<AboutItemBase>(itemView = itemView) {
    override fun bindView(item: AboutItemBase) {
        item as AboutHeaderItem
        itemView as TextView

        with(itemView) {
            text = item.title
        }
    }
}