package com.gdavidpb.tuindice.data.source.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.gdavidpb.tuindice.base.domain.repository.NotificationOfferRepository
import com.gdavidpb.tuindice.base.domain.repository.NotificationPermissionRepository
import com.gdavidpb.tuindice.platform.android.NotificationPermissionLauncher

// Android asks for the notification permission from version 13 on. Below it the permission is granted at install.
class AndroidNotificationPermissionDataSource(
	private val context: Context,
	private val launcher: NotificationPermissionLauncher,
	private val offers: NotificationOfferRepository
) : NotificationPermissionRepository {
	override suspend fun isOfferNeeded(): Boolean {
		return notificationOfferNeeded(
			sdkInt = Build.VERSION.SDK_INT,
			isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
					PackageManager.PERMISSION_GRANTED,
			isOffered = offers.isOffered()
		)
	}

	override fun requestPermission() {
		launcher.launch()
	}
}

// The chat offers the permission only where the system asks for it, when it is not granted yet, and once per device.
internal fun notificationOfferNeeded(sdkInt: Int, isGranted: Boolean, isOffered: Boolean): Boolean {
	return sdkInt >= ANDROID_NOTIFICATION_PERMISSION_SDK && !isGranted && !isOffered
}

private const val ANDROID_NOTIFICATION_PERMISSION_SDK = 33
