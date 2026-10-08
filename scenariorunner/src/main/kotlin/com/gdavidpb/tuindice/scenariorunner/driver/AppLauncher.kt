package com.gdavidpb.tuindice.scenariorunner.driver

import android.content.ComponentName
import android.content.Intent
import android.os.SystemClock
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
		// A failed `pidof` proves nothing about the process, so the app is stopped anyway: a cold start needs it.
		if (session.appProcessRunning() != false) session.shell("am force-stop ${AppIdentity.ID}")

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
	 * A dead process is never revived here: starting it again would be a cold start without the launch
	 * arguments, so the answer is false and the death is in the driver log. Every request to bring the
	 * app back is written to the driver log.
	 */
	override fun foreground(): Boolean {
		val deadline = SystemClock.uptimeMillis() + Timeouts.Action
		var alive = !diedBefore("foreground")
		var inFront = false
		var brought = 0
		var inFrontSince = NOT_IN_FRONT
		var lastOther = ""

		while (alive && !inFront && SystemClock.uptimeMillis() < deadline) {
			val front = session.device.currentPackageName.orEmpty()
			val now = SystemClock.uptimeMillis()

			if (front == AppIdentity.ID) {
				if (inFrontSince == NOT_IN_FRONT) inFrontSince = now
				inFront = now - inFrontSince >= STABLE_MS
				if (!inFront) SystemClock.sleep(Timeouts.PollInterval)
			} else {
				inFrontSince = NOT_IN_FRONT
				lastOther = front
				alive = !diedBefore("foreground request ${brought + 1}")
				if (alive) bringBack(front, ++brought, deadline)
			}
		}

		if (inFront) {
			session.log.write("foreground: in front after $brought requests; last other package '$lastOther'")
		} else if (alive) {
			session.log.write("foreground: not in front after ${Timeouts.Action} ms, $brought requests; front '$lastOther'")
		}

		return inFront
	}

	private fun bringBack(front: String, request: Int, deadline: Long) {
		session.log.write("foreground: '$front' is in front, request $request to bring the app back")

		if (start(reorderToFront(), deadline - SystemClock.uptimeMillis())) {
			session.poll(minOf(deadline - SystemClock.uptimeMillis(), Timeouts.Assert)) { isForeground() }
		}
	}

	override fun terminate() {
		session.shell("am force-stop ${AppIdentity.ID}")
	}

	override fun isForeground(): Boolean = session.device.currentPackageName == AppIdentity.ID

	/** False only when `pidof` proves the process is gone; a failed `pidof` is not a death. */
	override fun isRunning(): Boolean = session.appProcessRunning() != false

	/** True, after logging it, when the app's process is gone; an unreadable answer does not count as dead. */
	private fun diedBefore(what: String): Boolean {
		val running = session.appProcessRunning()

		if (running == null) session.log.write("$what: could not tell whether the app process is running; assuming it is")
		if (running == false) session.log.write("$what: the app process is not running; it is not started again")

		return running == false
	}

	private fun start(intent: Intent, timeoutMs: Long = LAUNCH_TIMEOUT_MS): Boolean {
		val started = runCatching { session.instrumentation.context.startActivity(intent) }.isSuccess

		val shown = Until.hasObject(By.pkg(AppIdentity.ID).depth(0))

		return started && session.device.wait(shown, timeoutMs.coerceAtLeast(1)) == true
	}

	private fun reorderToFront(): Intent =
		mainIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

	private fun mainIntent(): Intent =
		Intent(Intent.ACTION_MAIN)
			.addCategory(Intent.CATEGORY_LAUNCHER)
			.setComponent(ComponentName(AppIdentity.ID, AppIdentity.ACTIVITY))

	private companion object {
		const val LAUNCH_TIMEOUT_MS = 30_000L
		const val STABLE_MS = 1_000L
		const val NOT_IN_FRONT = -1L
	}
}
