package com.gdavidpb.tuindice.ui.viewholders

import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.About
import com.gdavidpb.tuindice.presentation.model.AboutBase
import com.gdavidpb.tuindice.ui.adapters.AboutAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.drawables
import com.gdavidpb.tuindice.utils.getCompatDrawable
import com.gdavidpb.tuindice.utils.onClickOnce
import org.jetbrains.anko.append
import org.jetbrains.anko.buildSpanned

open class AboutViewHolder(
        itemView: View,
        private val callback: AboutAdapter.AdapterCallback
) : BaseViewHolder<AboutBase>(itemView = itemView) {
    override fun bindView(item: AboutBase) {
        item as About

        with(itemView as AppCompatButton) {
            drawables(left = context.getCompatDrawable(item.drawable, R.color.colorSecondaryText))

            val (titleColor, subtitleColor) = callback.resolveColors()
            val (title, subtitle) = item.content.split('\n')

            text = buildSpanned {
                append(title, ForegroundColorSpan(titleColor))
                append('\n')
                append(subtitle, ForegroundColorSpan(subtitleColor), TypefaceSpan("sans-serif-light"))
            }

            onClickOnce(item::onClick)
        }
    }
}