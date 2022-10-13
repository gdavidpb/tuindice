package com.gdavidpb.tuindice.base.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.ui.adapters.BottomMenuAdapter
import com.gdavidpb.tuindice.base.utils.extensions.drawables
import com.gdavidpb.tuindice.base.utils.extensions.onClickOnce
import com.google.android.material.textview.MaterialTextView

class BottomMenuViewHolder(
	itemView: View,
	private val manager: BottomMenuAdapter.AdapterManager
) : BaseViewHolder<BottomMenuItem>(itemView = itemView) {

	init {
		with(itemView) {
			onClickOnce {
				manager.onMenuItemClicked(itemId = getItem().itemId)
			}
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