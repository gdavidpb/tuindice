package com.gdavidpb.tuindice.scenariorunner.driver

import android.os.SystemClock
import android.view.KeyEvent

/** Injects key events into the focused window. */
internal class KeyInjector(private val session: DeviceSession) {
	/** How many events the system took, and how long the call that it refused (or that threw) took. */
	class Injection(val entered: Int, val refusedAfterMs: Long?)

	/**
	 * Injects [events] in order and answers how many the system accepted, stopping at the first refusal or failure.
	 * Each event is re-stamped just before it goes in: `getEvents` gives them all the time they were built,
	 * and `Instrumentation.sendStringSync` re-stamps for the same reason (a slow emulator can leave the
	 * later ones old by the time they are injected). The time of the call that refused is kept: a refusal that came
	 * after seconds is a busy main thread, not a rejected key.
	 */
	fun inject(events: Array<KeyEvent>): Injection {
		val automation = session.instrumentation.uiAutomation
		var entered = 0

		for (event in events) {
			val fresh = KeyEvent.changeTimeRepeat(event, SystemClock.uptimeMillis(), 0)
			val began = SystemClock.uptimeMillis()
			val taken = runCatching { automation.injectInputEvent(fresh, true) }.getOrDefault(false)

			if (!taken) return Injection(entered, SystemClock.uptimeMillis() - began)

			entered++
		}

		return Injection(entered, null)
	}

	/** Presses and releases [keyCode], and answers whether the system took both events. */
	fun press(keyCode: Int): Boolean {
		val automation = session.instrumentation.uiAutomation
		val now = SystemClock.uptimeMillis()
		val down = KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0)
		val up = KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0)

		return runCatching {
			automation.injectInputEvent(down, true) && automation.injectInputEvent(up, true)
		}.getOrDefault(false)
	}
}
