package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import androidx.appcompat.widget.AppCompatButton
import com.gdavidpb.tuindice.presentation.model.AboutItem
import com.gdavidpb.tuindice.presentation.model.AboutItemBase
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.extensions.drawables
import com.gdavidpb.tuindice.utils.extensions.onClickOnce

open class AboutViewHolder(
        itemView: View
) : BaseViewHolder<AboutItemBase>(itemView = itemView) {
    override fun bindView(item: AboutItemBase) {
        item as AboutItem

        with(itemView as AppCompatButton) {
            text = item.content

            drawables(left = item.drawable)

            onClickOnce(item::onClick)
        }
    }
}