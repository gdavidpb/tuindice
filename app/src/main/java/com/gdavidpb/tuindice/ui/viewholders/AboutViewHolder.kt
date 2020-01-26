package com.gdavidpb.tuindice.ui.viewholders

import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.AboutItem
import com.gdavidpb.tuindice.presentation.model.AboutItemBase
import com.gdavidpb.tuindice.ui.adapters.AboutAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.extensions.drawables
import com.gdavidpb.tuindice.utils.extensions.getCompatDrawable
import com.gdavidpb.tuindice.utils.extensions.onClickOnce
import org.jetbrains.anko.append
import org.jetbrains.anko.buildSpanned

open class AboutViewHolder(
        itemView: View,
        private val manager: AboutAdapter.AdapterManager
) : BaseViewHolder<AboutItemBase>(itemView = itemView) {
    override fun bindView(item: AboutItemBase) {
        item as AboutItem

        with(itemView as AppCompatButton) {
            drawables(left = context.getCompatDrawable(item.drawable, R.color.color_secondary_text))

            val (titleColor, subtitleColor) = manager.resolveColors()
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