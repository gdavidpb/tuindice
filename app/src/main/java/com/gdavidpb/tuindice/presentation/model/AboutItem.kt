package com.gdavidpb.tuindice.presentation.model

import androidx.annotation.DrawableRes

data class AboutItem(
        val content: String,
        @DrawableRes val drawable: Int,
        val onClick: () -> Unit = { }
) : AboutItemBase {
    override fun onClick() = onClick.invoke()
}