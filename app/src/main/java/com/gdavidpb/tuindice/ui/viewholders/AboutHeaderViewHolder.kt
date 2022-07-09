package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import android.widget.TextView
import com.gdavidpb.tuindice.presentation.model.AboutHeaderItem
import com.gdavidpb.tuindice.presentation.model.AboutItemBase
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder

class AboutHeaderViewHolder(
        itemView: View
) : BaseViewHolder<AboutItemBase>(itemView = itemView) {
    override fun bindView(item: AboutItemBase) {
        item as AboutHeaderItem

        with(itemView as TextView) {
            text = item.title
        }
    }
}