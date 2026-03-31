package com.gdavidpb.tuindice.about.data.source

import android.content.Context
import android.os.Build
import com.gdavidpb.tuindice.about.data.contract.AppInfoDataSource

class AndroidAppInfoDataSource(
	private val context: Context
) : AppInfoDataSource {
	override fun appVersionName(): String {
		val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

		return packageInfo.versionName.toString()
	}

	override fun appVersionCode(): Long {
		val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
			packageInfo.longVersionCode
		else
			@Suppress("DEPRECATION")
			packageInfo.versionCode.toLong()
	}
}
