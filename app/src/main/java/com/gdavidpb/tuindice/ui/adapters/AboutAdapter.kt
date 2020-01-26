package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.AboutItem
import com.gdavidpb.tuindice.presentation.model.AboutItemBase
import com.gdavidpb.tuindice.presentation.model.AboutHeaderItem
import com.gdavidpb.tuindice.presentation.model.AboutLibItem
import com.gdavidpb.tuindice.ui.adapters.base.BaseAdapter
import com.gdavidpb.tuindice.ui.viewholders.AboutHeaderViewHolder
import com.gdavidpb.tuindice.ui.viewholders.AboutLibViewHolder
import com.gdavidpb.tuindice.ui.viewholders.AboutViewHolder
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.VIEW_TYPE_ABOUT
import com.gdavidpb.tuindice.utils.VIEW_TYPE_ABOUT_HEADER
import com.gdavidpb.tuindice.utils.VIEW_TYPE_ABOUT_LIB

open class AboutAdapter(
        private val manager: AdapterManager
) : BaseAdapter<AboutItemBase>() {

    interface AdapterManager {
        fun resolveColors(): List<Int>
    }

    override fun provideComparator() = compareBy(AboutItemBase::hashCode)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<AboutItemBase> {
        val itemView = when (viewType) {
            VIEW_TYPE_ABOUT -> LayoutInflater.from(parent.context).inflate(R.layout.item_about, parent, false)
            VIEW_TYPE_ABOUT_LIB -> LayoutInflater.from(parent.context).inflate(R.layout.item_about, parent, false)
            VIEW_TYPE_ABOUT_HEADER -> LayoutInflater.from(parent.context).inflate(R.layout.item_about_header, parent, false)
            else -> throw IllegalArgumentException("viewType: '$viewType'")
        }

        return when (viewType) {
            VIEW_TYPE_ABOUT -> AboutViewHolder(itemView, manager)
            VIEW_TYPE_ABOUT_LIB -> AboutLibViewHolder(itemView, manager)
            VIEW_TYPE_ABOUT_HEADER -> AboutHeaderViewHolder(itemView)
            else -> throw IllegalArgumentException("viewType: '$viewType'")
        }
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is AboutItem -> VIEW_TYPE_ABOUT
        is AboutLibItem -> VIEW_TYPE_ABOUT_LIB
        is AboutHeaderItem -> VIEW_TYPE_ABOUT_HEADER
        else -> throw IllegalArgumentException("viewType: '${items[position]}'")
    }
}