package com.gdavidpb.tuindice.scenariorunner.driver

import android.app.Instrumentation
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.UiDevice

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

	private companion object {
		const val POLL_MS = 100L
	}
}
