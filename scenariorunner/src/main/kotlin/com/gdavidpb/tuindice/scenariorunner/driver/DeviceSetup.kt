package com.gdavidpb.tuindice.scenariorunner.driver

/** One-time device settings that keep text input and timing deterministic. */
internal object DeviceSetup {
	private val settings = listOf(
		"settings put global hide_error_dialogs 1",
		"settings put secure stylus_handwriting_enabled 0",
		"settings put global window_animation_scale 0",
		"settings put global transition_animation_scale 0",
		"settings put global animator_duration_scale 0",
		"settings put secure spell_checker_enabled 0",
		"settings put secure autofill_service null",
		"settings put secure show_ime_with_hard_keyboard 0",
		"cmd autofill disable"
	)

	@Volatile
	private var applied = false

	@Synchronized
	fun apply() {
		if (applied) return

		val session = DeviceSession()
		settings.forEach { session.shell(it) }
		applied = true
	}
}
