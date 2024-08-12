package com.gdavidpb.tuindice.utils

import android.content.Context
import android.os.Build
import com.gdavidpb.tuindice.base.utils.extension.versionCode
import com.gdavidpb.tuindice.base.utils.extension.versionName

class UserAgent(
	private val userAgent: String
) {
	constructor(context: Context) : this(
		listOf(
			"TuIndice",
			context.versionName(),
			context.versionCode(),
			"Android",
			Build.VERSION.RELEASE,
			Build.VERSION.SDK_INT,
			Build.ID,
			Build.MANUFACTURER,
			Build.MODEL
		).joinToString(";")
	)

	override fun toString(): String {
		return userAgent
	}
}