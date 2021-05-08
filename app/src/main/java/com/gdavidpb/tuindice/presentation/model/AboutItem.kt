package com.gdavidpb.tuindice.presentation.model

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable

data class AboutItem(
        val content: CharSequence,
        val drawable: Drawable,
        val drawableTint: ColorStateList?,
        val onClick: () -> Unit = { }
) : AboutItemBase {
    override fun onClick() = onClick.invoke()
}