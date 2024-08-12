package com.gdavidpb.tuindice.utils

import android.content.Context
import android.os.Build
import com.gdavidpb.tuindice.base.utils.extension.versionCode
import com.gdavidpb.tuindice.base.utils.extension.versionName

class UserAgent(
	private val userAgent: String
) {
	private val values = userAgent.split(";")

	val appName = values[0]
	val versionName = values[1]
	val versionCode = values[2]
	val osName = values[3]
	val osVersion = values[4]
	val osCode = values[5]
	val osId = values[6]
	val manufacturer = values[7]
	val model = values[8]

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