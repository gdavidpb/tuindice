package com.gdavidpb.tuindice.scenariorunner.driver

import android.content.ComponentName
import android.content.Intent
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.gdavidpb.tuindice.scenariokit.driver.AppControl
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec

/**
 * Starts the debug app through an intent whose extras carry the launch arguments.
 * It only stops and starts the app: wiping its state is the caller's job.
 */
internal class AppLauncher(private val session: DeviceSession) : AppControl {
	override fun launch(spec: LaunchSpec): Boolean {
		if (session.shell("pidof $APP_ID").isNotBlank()) session.shell("am force-stop $APP_ID")

		val intent = mainIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		spec.arguments.forEach { (key, value) -> intent.putExtra(key, value) }

		return start(intent)
	}

	override fun foreground(): Boolean = start(mainIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

	override fun terminate() {
		session.shell("am force-stop $APP_ID")
	}

	override fun isForeground(): Boolean = session.device.currentPackageName == APP_ID

	private fun start(intent: Intent): Boolean {
		val started = runCatching { session.instrumentation.context.startActivity(intent) }.isSuccess

		return started && session.device.wait(Until.hasObject(By.pkg(APP_ID).depth(0)), LAUNCH_TIMEOUT_MS) == true
	}

	private fun mainIntent(): Intent =
		Intent(Intent.ACTION_MAIN)
			.addCategory(Intent.CATEGORY_LAUNCHER)
			.setComponent(ComponentName(APP_ID, ACTIVITY))

	private companion object {
		const val APP_ID = "com.gdavidpb.tuindice.debug"
		const val ACTIVITY = "com.gdavidpb.tuindice.ui.activity.DebugMainActivity"
		const val LAUNCH_TIMEOUT_MS = 30_000L
	}
}
