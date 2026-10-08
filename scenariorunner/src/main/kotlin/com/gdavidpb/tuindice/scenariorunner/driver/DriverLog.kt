package com.gdavidpb.tuindice.scenariorunner.driver

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The driver's own record of a scenario: one line per step and per tolerance (a gesture refused, the app
 * brought back to the front, a wait that could not read the screen). Each line is appended to `driver.log`
 * in the scenario's output directory the moment it is written, so the file survives a hang or a killed
 * process; every line also goes to logcat, which the failure capture reads.
 */
internal class DriverLog {
	private val stamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

	@Volatile
	private var file: File? = null

	@Volatile
	private var refusal: String? = null

	/** Starts appending to `driver.log` inside [directory]. */
	fun open(directory: File) {
		file = File(directory, FILE_NAME)
	}

	@Synchronized
	fun write(line: String) {
		Log.i(TAG, line)
		val target = file ?: return

		runCatching {
			FileOutputStream(target, true).use { it.write("${stamp.format(Date())} $line\n".toByteArray()) }
		}.onFailure { Log.w(TAG, "driver.log could not be written: $it") }
	}

	/** Writes [reason] as a line marked `[refusal]` (the harness counts them) and keeps it as the reason the gesture or text entry in progress was refused. */
	fun refuse(reason: String) {
		refusal = reason
		write("[refusal] $reason")
	}

	/** Forgets the last refusal: every gesture and text entry starts without one. */
	fun clearRefusal() {
		refusal = null
	}

	/** The reason the last gesture or text entry was refused, or null when it was not. */
	fun lastRefusal(): String? = refusal

	companion object {
		const val FILE_NAME = "driver.log"
		private const val TAG = "ScenarioDriver"
	}
}
