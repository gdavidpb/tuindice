package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.BottomMenuItem
import com.gdavidpb.tuindice.ui.viewholders.BottomMenuViewHolder

class BottomMenuAdapter(
        private val items: List<BottomMenuItem>,
        private val manager: AdapterManager
) : RecyclerView.Adapter<BottomMenuViewHolder>() {

    interface AdapterManager {
        fun onMenuItemClicked(position: Int)
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