package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.presentation.model.AboutItem
import com.gdavidpb.tuindice.presentation.model.AboutItemBase
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.extensions.onClickOnce
import com.google.android.material.button.MaterialButton

class AboutViewHolder(
        itemView: View
) : BaseViewHolder<AboutItemBase>(itemView = itemView) {

    init {
        with(itemView) {
            onClickOnce {
                getItem().onClick()
            }
        }
    }

    override fun bindView(item: AboutItemBase) {
        super.bindView(item)

        item as AboutItem

        with(itemView as MaterialButton) {
            text = item.content
            icon = item.drawable
            iconTint = item.drawableTint
        }
    }
}