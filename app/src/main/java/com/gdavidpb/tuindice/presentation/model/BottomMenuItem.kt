package com.gdavidpb.tuindice.presentation.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class BottomMenuItem(
        @DrawableRes
        val iconResource: Int,
        @StringRes
        val textResource: Int,
)