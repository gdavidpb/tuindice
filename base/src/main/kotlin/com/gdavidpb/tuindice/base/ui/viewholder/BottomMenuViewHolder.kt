package com.gdavidpb.tuindice.base.ui.viewholder

import android.view.View
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.ui.adapter.BottomMenuAdapter
import com.gdavidpb.tuindice.base.utils.extension.drawables
import com.gdavidpb.tuindice.base.utils.extension.getCompatColor
import com.gdavidpb.tuindice.base.utils.extension.getCompatDrawable
import com.gdavidpb.tuindice.base.utils.extension.onClickOnce
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
			setTextColor(context.getCompatColor(item.textColor))

			drawables(
				start = context.getCompatDrawable(
					drawableRes = item.iconResource,
					colorRes = item.iconColor
				)
			)
		}
	}
}