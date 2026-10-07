package com.gdavidpb.tuindice.scenariorunner.driver

import android.app.Instrumentation
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.UiDevice
import com.gdavidpb.tuindice.scenariokit.model.Query

/**
 * What every piece of the driver shares: the instrumentation, the device and the shell.
 * Implicit UI Automator waits are off, so only an explicit wait ever blocks.
 */
internal class DeviceSession {
	val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
	val device: UiDevice = UiDevice.getInstance(instrumentation)
	val selectors = Selectors(device)

	init {
		Configurator.getInstance().apply {
			waitForIdleTimeout = 0
			waitForSelectorTimeout = 0
			actionAcknowledgmentTimeout = 0
		}
	}

	fun shell(command: String): String = runCatching { device.executeShellCommand(command) }.getOrDefault("")

	/** Polls [condition] every [POLL_MS] until it holds or [timeoutMs] passes; always tries once. */
	fun poll(timeoutMs: Long, condition: () -> Boolean): Boolean {
		val deadline = SystemClock.uptimeMillis() + timeoutMs
		while (true) {
			if (runCatching(condition).getOrDefault(false)) return true
			if (SystemClock.uptimeMillis() >= deadline) return false
			SystemClock.sleep(POLL_MS)
		}
	}

	/**
	 * Visible bounds of [q] once they read the same [STABLE_READS] times in a row, so a touch lands where the
	 * element is and not where it was before the layout moved (the keyboard opening, a sheet settling).
	 * Null when [q] is not on screen or its bounds keep changing for [SETTLE_TIMEOUT_MS]; in that case the
	 * reason is written to the driver log and the gesture is refused.
	 */
	fun settledBounds(q: Query): Rect? {
		val deadline = SystemClock.uptimeMillis() + SETTLE_TIMEOUT_MS
		var last = visibleBounds(q)
		var equalReads = 1

		while (last != null && equalReads < STABLE_READS && SystemClock.uptimeMillis() < deadline) {
			SystemClock.sleep(SETTLE_POLL_MS)
			val now = visibleBounds(q)
			equalReads = if (now == last) equalReads + 1 else 1
			last = now
		}

		if (last != null && equalReads < STABLE_READS) {
			Log.w(LOG_TAG, "$q: bounds still moving after $SETTLE_TIMEOUT_MS ms (last $last); gesture refused")
		}

		return last.takeIf { equalReads >= STABLE_READS }
	}

	private fun visibleBounds(q: Query): Rect? = selectors.find(q)?.let { runCatching { it.visibleBounds }.getOrNull() }

	private companion object {
		const val POLL_MS = 100L
		const val SETTLE_POLL_MS = 50L
		const val SETTLE_TIMEOUT_MS = 5_000L
		const val STABLE_READS = 3
		const val LOG_TAG = "ScenarioDriver"
	}
}
