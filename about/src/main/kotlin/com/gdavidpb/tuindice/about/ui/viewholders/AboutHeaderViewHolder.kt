package com.gdavidpb.tuindice.about.ui.viewholders

import android.view.View
import android.widget.TextView
import com.gdavidpb.tuindice.about.presentation.model.AboutHeaderItem
import com.gdavidpb.tuindice.about.presentation.model.AboutItemBase
import com.gdavidpb.tuindice.base.ui.viewholders.BaseViewHolder

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