package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.ui.adapters.BottomMenuAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.extensions.drawables
import com.gdavidpb.tuindice.utils.extensions.onClickOnce
import com.google.android.material.textview.MaterialTextView

class BottomMenuViewHolder(
        itemView: View,
        private val manager: BottomMenuAdapter.AdapterManager
) : BaseViewHolder<BottomMenuItem>(itemView = itemView) {

    init {
        with(itemView) {
            onClickOnce { manager.onMenuItemClicked(position = absoluteAdapterPosition) }
        }
    }

    override fun bindView(item: BottomMenuItem) {
        super.bindView(item)

        with(itemView as MaterialTextView) {
            setText(item.textResource)

            drawables(start = item.iconResource)
        }
    }
}