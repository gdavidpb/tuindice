package com.gdavidpb.tuindice.about.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.about.presentation.model.AboutItem
import com.gdavidpb.tuindice.about.presentation.model.AboutItemBase
import com.gdavidpb.tuindice.base.ui.viewholder.BaseViewHolder
import com.gdavidpb.tuindice.base.utils.extension.onClickOnce
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