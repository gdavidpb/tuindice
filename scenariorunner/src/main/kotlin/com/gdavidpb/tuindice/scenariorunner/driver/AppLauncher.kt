package com.gdavidpb.tuindice.scenariorunner.driver

import android.content.ComponentName
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.gdavidpb.tuindice.scenariokit.driver.AppControl
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Timeouts

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

	/**
	 * True only when the app's window is in front and has stayed there for [STABLE_MS]. Another app
	 * can take the front after the request is accepted (Chrome opens its first-run screen a few
	 * milliseconds after a link opens it), so a start the system accepted proves nothing: the front
	 * window is watched, and the app is brought back each time something else holds it. The wait
	 * is bounded by [Timeouts.Action]; the answer is false when the app is not in front by then.
	 */
	override fun foreground(): Boolean {
		val deadline = SystemClock.uptimeMillis() + Timeouts.Action
		var brought = 0
		var inFrontSince = NOT_IN_FRONT
		var lastOther = ""

		while (SystemClock.uptimeMillis() < deadline) {
			val front = session.device.currentPackageName.orEmpty()
			val now = SystemClock.uptimeMillis()

			if (front == APP_ID) {
				if (inFrontSince == NOT_IN_FRONT) inFrontSince = now
				if (now - inFrontSince >= STABLE_MS) {
					Log.i(TAG, "foreground: the app is in front after $brought requests; last other package '$lastOther'")
					return true
				}
			} else {
				inFrontSince = NOT_IN_FRONT
				lastOther = front
				brought++
				Log.i(TAG, "foreground: '$front' is in front, request $brought to bring the app back")
				if (start(reorderToFront(), deadline - now)) {
					session.poll(minOf(deadline - SystemClock.uptimeMillis(), Timeouts.Assert)) { isForeground() }
				}
				continue
			}
			SystemClock.sleep(Timeouts.PollInterval)
		}

		Log.i(TAG, "foreground: not in front after ${Timeouts.Action} ms and $brought requests; front '$lastOther'")
		return false
	}

	override fun terminate() {
		session.shell("am force-stop $APP_ID")
	}

	override fun isForeground(): Boolean = session.device.currentPackageName == APP_ID

	private fun start(intent: Intent, timeoutMs: Long = LAUNCH_TIMEOUT_MS): Boolean {
		val started = runCatching { session.instrumentation.context.startActivity(intent) }.isSuccess

		return started && session.device.wait(Until.hasObject(By.pkg(APP_ID).depth(0)), timeoutMs.coerceAtLeast(1)) == true
	}

	private fun reorderToFront(): Intent =
		mainIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

	private fun mainIntent(): Intent =
		Intent(Intent.ACTION_MAIN)
			.addCategory(Intent.CATEGORY_LAUNCHER)
			.setComponent(ComponentName(APP_ID, ACTIVITY))

	private companion object {
		const val APP_ID = "com.gdavidpb.tuindice.debug"
		const val ACTIVITY = "com.gdavidpb.tuindice.ui.activity.DebugMainActivity"
		const val LAUNCH_TIMEOUT_MS = 30_000L
		const val STABLE_MS = 1_000L
		const val NOT_IN_FRONT = -1L
		const val TAG = "ScenarioDriver"
	}
}
