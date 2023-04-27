package com.gdavidpb.tuindice.base.utils

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

class ResourceResolver(
	private val context: Context
) {
	fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
		return context.getString(resId, *formatArgs)
	}

	fun getQuantityString(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any): String {
		return context.resources.getQuantityString(resId, quantity, *formatArgs)
	}

	@ColorInt
	fun getColor(@ColorRes id: Int): Int {
		return ContextCompat.getColor(context, id)
	}
}