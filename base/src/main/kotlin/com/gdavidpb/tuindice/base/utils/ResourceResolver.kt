package com.gdavidpb.tuindice.base.utils

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

class ResourceResolver(
	private val context: Context
) {
	fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
		return context.getString(resId, *formatArgs)
	}

	fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String {
		return context.resources.getQuantityString(id, quantity, *formatArgs)
	}
}