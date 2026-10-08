package com.gdavidpb.tuindice.scenariorunner.driver

/**
 * What `dumpsys input_method` says about whether the soft keyboard is shown. The window list of the accessibility
 * service lags behind the window manager, but the input method service does not: its `mVisibilityStateComputer` block
 * carries `mInputShown=true` from the moment the keyboard is requested and `false` from the moment it is hidden
 * (measured on the pinned emulator, Android 37 with Gboard: a single `mInputShown=` line, `true` with the keyboard up,
 * `false` before it and after Back closed it with the field still focused).
 */
internal object InputMethodDump {
	private val SHOWN = Regex("""(?m)^\s*mInputShown=(true|false)\s*$""")

	/**
	 * True or false as the dump says; null when it does not say it exactly once (a format that changed, a truncated or
	 * empty answer), so that an unreadable dump is never taken for "hidden" or "shown".
	 */
	fun shown(dump: String): Boolean? {
		val found = SHOWN.findAll(dump).map { it.groupValues[1] }.toList()

		return if (found.size == 1) found.single().toBoolean() else null
	}
}
