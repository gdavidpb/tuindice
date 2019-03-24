package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import android.widget.TextView
import com.gdavidpb.tuindice.presentation.model.AboutBase
import com.gdavidpb.tuindice.presentation.model.AboutHeader
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.onClickOnce

open class AboutHeaderViewHolder(itemView: View) : BaseViewHolder<AboutBase>(itemView = itemView) {
    override fun bindView(item: AboutBase) {
        item as AboutHeader

        with(itemView as TextView) {
            text = item.title

            onClickOnce(item::onClick)
        }
    }
}