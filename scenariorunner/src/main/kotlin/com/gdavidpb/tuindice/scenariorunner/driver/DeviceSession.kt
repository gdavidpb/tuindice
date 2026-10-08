package com.gdavidpb.tuindice.scenariorunner.driver

import android.app.Instrumentation
import android.graphics.Rect
import android.os.SystemClock
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
	val log = DriverLog()
	val keyboard = KeyboardGuard(this)

	init {
		Configurator.getInstance().apply {
			waitForIdleTimeout = 0
			waitForSelectorTimeout = 0
			actionAcknowledgmentTimeout = 0
		}
	}

	fun shell(command: String): String = shellOrNull(command).orEmpty()

	/** The output of [command], or null when the shell call itself failed: an empty answer then proves nothing. */
	fun shellOrNull(command: String): String? = runCatching { device.executeShellCommand(command) }.getOrNull()

	/**
	 * Whether the input method says the soft keyboard is shown ([InputMethodDump]); null when the dump cannot be read.
	 * The dump is about a megabyte, so it is asked for only when the window list cannot settle the question.
	 */
	fun inputMethodShown(): Boolean? = shellOrNull("dumpsys input_method")?.let(InputMethodDump::shown)

	/** True/false by `pidof`; null when the shell call failed and nothing is known about the process. */
	fun appProcessRunning(): Boolean? = shellOrNull("pidof ${AppIdentity.ID}")?.isNotBlank()

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
	 * Visible bounds of [q] once they read the same [STABLE_READS] times in a row, [SETTLE_POLL_MS] apart (the rule
	 * of the iOS driver: three equal reads, at most [MAX_READS] reads or [SETTLE_TIMEOUT_MS]), so a touch lands where
	 * the element is and not where it was before the layout moved (the keyboard opening, a sheet settling).
	 * The bounds are null when [q] is not on screen or its bounds never settle, and [Settled.why] says which: the caller,
	 * which knows the primitive it is, refuses the gesture with it. Every gesture that aims at an element (tap, tapAt,
	 * doubleTap, swipe) takes its point from here. When the bounds moved while they were watched, or the gesture is
	 * refused, the reads and their times are written to the driver log, which is how the staleness of the reads is
	 * measured.
	 */
	fun settledBounds(q: Query): Settled {
		val began = SystemClock.uptimeMillis()
		val reads = mutableListOf<Pair<Long, Rect?>>()
		var last = visibleBounds(q)
		reads += 0L to last
		var equalReads = 1

		while (last != null && equalReads < STABLE_READS && withinBudget(reads.size, began)) {
			SystemClock.sleep(SETTLE_POLL_MS)
			val now = visibleBounds(q)
			reads += (SystemClock.uptimeMillis() - began) to now
			equalReads = if (now == last) equalReads + 1 else 1
			last = now
		}

		val settled = last != null && equalReads >= STABLE_READS
		val why = when {
			last == null -> "$q: not on screen when the gesture was about to be made; gesture refused"
			!settled -> "$q: bounds still moving after ${reads.size} reads (last $last); gesture refused"
			else -> ""
		}
		if (reads.map { it.second }.distinct().size > 1 || !settled) {
			log.write("settledBounds $q: ${reads.joinToString { (at, bounds) -> "+${at}ms $bounds" }}")
		}

		return Settled(last.takeIf { settled }, why)
	}

	/** The settled bounds of an element, or null with the reason the gesture that asked for them is refused. */
	class Settled(val bounds: Rect?, val why: String)

	private fun withinBudget(reads: Int, since: Long) =
		reads < MAX_READS && SystemClock.uptimeMillis() - since < SETTLE_TIMEOUT_MS

	private fun visibleBounds(q: Query): Rect? = selectors.find(q)?.let { runCatching { it.visibleBounds }.getOrNull() }

	private companion object {
		const val POLL_MS = 100L
		const val SETTLE_POLL_MS = 100L
		const val SETTLE_TIMEOUT_MS = 8_000L
		const val MAX_READS = 20
		const val STABLE_READS = 3
	}
}
