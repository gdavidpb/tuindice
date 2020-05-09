package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.gdavidpb.tuindice.R

open class LoadingAdapter(
        private val items: List<String>
) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = (convertView
                ?: LayoutInflater.from(parent.context).inflate(R.layout.view_text_view_loading, parent, false)) as TextView

        itemView.text = items[position]

        return itemView
    }

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = items.size
}