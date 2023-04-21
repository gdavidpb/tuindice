package com.gdavidpb.tuindice.base.utils

import android.content.Context
import androidx.annotation.StringRes

class ResourceResolver(
	private val context: Context
) {
	fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
		return context.getString(resId, formatArgs)
	}
}