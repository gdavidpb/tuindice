package com.gdavidpb.tuindice.data.source.device

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoGateway

class AndroidDeviceInfoGateway(
	private val context: Context
) : DeviceInfoGateway {
	override fun appVersionName(): String {
		val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
		return packageInfo.versionName.toString()
	}

	override fun appVersionCode(): Long {
		val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			packageInfo.longVersionCode
		} else {
			@Suppress("DEPRECATION")
			packageInfo.versionCode.toLong()
		}
	}

	override fun hasCamera(): Boolean {
		return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
	}
}
