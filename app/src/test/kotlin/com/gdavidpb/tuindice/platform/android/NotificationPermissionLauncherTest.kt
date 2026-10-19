package com.gdavidpb.tuindice.platform.android

import android.Manifest
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationPermissionLauncherTest {
	@Test
	fun launch_beforeAnyAttach_asksForNothing() {
		NotificationPermissionLauncher().launch()
	}

	@Test
	fun launch_afterAttach_asksForThePostNotificationsPermission() {
		val launcher = RecordingLauncher()

		NotificationPermissionLauncher().apply { attach(launcher) }.launch()

		assertEquals(listOf(Manifest.permission.POST_NOTIFICATIONS), launcher.launched)
	}

	@Test
	fun detach_unregistersTheLauncher_andLaunchesNoMore() {
		val launcher = RecordingLauncher()
		val holder = NotificationPermissionLauncher().apply { attach(launcher) }

		holder.detach()
		holder.launch()

		assertEquals(1, launcher.unregistered)
		assertEquals(emptyList<String>(), launcher.launched)
	}

	private class RecordingLauncher : ActivityResultLauncher<String>() {
		val launched = mutableListOf<String>()
		var unregistered = 0

		override val contract: ActivityResultContract<String, *> = ActivityResultContracts.RequestPermission()

		override fun launch(input: String, options: ActivityOptionsCompat?) {
			launched += input
		}

		override fun unregister() {
			unregistered++
		}
	}
}
