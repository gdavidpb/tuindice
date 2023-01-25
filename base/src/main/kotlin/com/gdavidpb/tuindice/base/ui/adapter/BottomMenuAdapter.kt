package com.gdavidpb.tuindice.base.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.base.R
import com.gdavidpb.tuindice.base.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.base.ui.viewholder.BottomMenuViewHolder

class BottomMenuAdapter(
	private val items: List<BottomMenuItem>,
	private val manager: AdapterManager
) : RecyclerView.Adapter<BottomMenuViewHolder>() {

	interface AdapterManager {
		fun onMenuItemClicked(itemId: Int)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomMenuViewHolder {
		val itemView = LayoutInflater
			.from(parent.context)
			.inflate(R.layout.item_bottom_menu, parent, false)

		return BottomMenuViewHolder(itemView, manager)
	}

	override fun onBindViewHolder(holder: BottomMenuViewHolder, position: Int) {
		val item = items[position]

		holder.bindView(item)
	}

	override fun getItemCount(): Int {
		return items.size
	}
}