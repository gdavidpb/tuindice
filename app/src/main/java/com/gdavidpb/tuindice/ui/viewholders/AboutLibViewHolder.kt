package com.gdavidpb.tuindice.ui.viewholders

import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import com.gdavidpb.tuindice.presentation.model.AboutBase
import com.gdavidpb.tuindice.presentation.model.AboutLib
import com.gdavidpb.tuindice.ui.adapters.AboutAdapter
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.drawables
import com.gdavidpb.tuindice.utils.getCompatVector
import com.gdavidpb.tuindice.utils.onClickOnce
import org.jetbrains.anko.append
import org.jetbrains.anko.buildSpanned
import org.jetbrains.anko.dip

open class AboutLibViewHolder(
        itemView: View,
        private val callback: AboutAdapter.AdapterCallback
) : BaseViewHolder<AboutBase>(itemView = itemView) {
    override fun bindView(item: AboutBase) {
        item as AboutLib

        with(itemView as AppCompatButton) {
            val size = dip(48)

            drawables(left = context.getCompatVector(item.drawable, size, size))

            val (titleColor, subtitleColor) = callback.resolveColors()
            val (title, subtitle) = listOf(
                    item.content.substringBefore('\n'),
                    item.content.substringAfter('\n')
            )

            text = buildSpanned {
                append(title, ForegroundColorSpan(titleColor))
                append('\n')
                append(subtitle, ForegroundColorSpan(subtitleColor), TypefaceSpan("sans-serif-light"))
            }

            onClickOnce(item::onClick)
        }
    }
}