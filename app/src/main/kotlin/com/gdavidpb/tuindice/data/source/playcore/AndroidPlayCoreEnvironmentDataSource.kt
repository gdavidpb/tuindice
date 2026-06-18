package com.gdavidpb.tuindice.data.source.playcore

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.gdavidpb.tuindice.data.model.playcore.PlayCoreSurface
import com.gdavidpb.tuindice.data.repository.playcore.PlayCoreEnvironmentDataRepository
import com.google.android.gms.common.GoogleApiAvailability

class AndroidPlayCoreEnvironmentDataSource(
	context: Context
) : PlayCoreEnvironmentDataRepository {
	private val appContext = context.applicationContext
	private val googleApiAvailability = GoogleApiAvailability.getInstance()
	private val playStorePackageName = PLAY_STORE_PACKAGE_NAME

	override fun getGooglePlayServicesStatus(): Int {
		return googleApiAvailability.isGooglePlayServicesAvailable(appContext)
	}

	override fun isPlayStoreAvailable(): Boolean {
		return appContext.packageManager.hasPackage(playStorePackageName)
	}

	override fun isPlayCoreServiceAvailable(surface: PlayCoreSurface): Boolean {
		return appContext.packageManager.hasService(
			Intent(surface.serviceAction).setPackage(playStorePackageName)
		)
	}

	private companion object {
		const val PLAY_STORE_PACKAGE_NAME = "com.android.vending"
	}
}

@Suppress("DEPRECATION")
private fun PackageManager.hasPackage(packageName: String): Boolean {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
	} else {
		getPackageInfo(packageName, 0)
	}

	return true
}

@Suppress("DEPRECATION")
private fun PackageManager.hasService(intent: Intent): Boolean {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		queryIntentServices(intent, PackageManager.ResolveInfoFlags.of(0)).isNotEmpty()
	} else {
		queryIntentServices(intent, 0).isNotEmpty()
	}
}
