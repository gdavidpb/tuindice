package com.gdavidpb.tuindice.presentation.model

import androidx.annotation.DrawableRes

data class AboutLib(
        val content: String,
        @DrawableRes val drawable: Int,
        val onClick: () -> Unit = { }
) : AboutBase {
    override fun onClick() = onClick.invoke()
}