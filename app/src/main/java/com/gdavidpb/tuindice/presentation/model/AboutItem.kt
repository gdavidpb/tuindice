package com.gdavidpb.tuindice.presentation.model

import android.graphics.drawable.Drawable

data class AboutItem(
        val content: CharSequence,
        val drawable: Drawable,
        val onClick: () -> Unit = { }
) : AboutItemBase {
    override fun onClick() = onClick.invoke()
}