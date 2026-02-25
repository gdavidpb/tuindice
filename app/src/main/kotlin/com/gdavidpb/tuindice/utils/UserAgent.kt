package com.gdavidpb.tuindice.utils

import android.content.Context
import android.os.Build
import com.gdavidpb.tuindice.di.buildStructuredUserAgent

class UserAgent(
	private val userAgent: String
) {
	constructor(context: Context) : this(
		buildStructuredUserAgent(
			appVersionName = appVersionName(context),
			appVersionCode = appVersionCode(context),
			osName = "Android",
			osVersion = Build.VERSION.RELEASE.orEmpty(),
			osCode = Build.VERSION.SDK_INT,
			osId = Build.ID.orEmpty(),
			manufacturer = Build.MANUFACTURER.orEmpty(),
			model = Build.MODEL.orEmpty()
		)
	)

	override fun toString(): String {
		return userAgent
	}

	companion object {
		private fun appVersionName(context: Context): String {
			val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
			return packageInfo.versionName.toString()
		}

		private fun appVersionCode(context: Context): Long {
			val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

			return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				packageInfo.longVersionCode
			} else {
				@Suppress("DEPRECATION")
				packageInfo.versionCode.toLong()
			}
		}
	}
}
