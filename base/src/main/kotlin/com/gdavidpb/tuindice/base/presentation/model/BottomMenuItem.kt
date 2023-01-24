package com.gdavidpb.tuindice.base.presentation.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gdavidpb.tuindice.base.R

data class BottomMenuItem(
	val itemId: Int,
	@DrawableRes
	val iconResource: Int,
	@ColorRes
	val iconColor: Int = R.color.color_menu_icon,
	@StringRes
	val textResource: Int,
	@ColorRes
	val textColor: Int = R.color.color_primary_text,
)