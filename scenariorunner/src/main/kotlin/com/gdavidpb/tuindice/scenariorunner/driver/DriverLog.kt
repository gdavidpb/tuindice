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

	/**
	 * The one funnel of every refusal: writes `[refusal] <primitive> <reason>` (the harness counts them by the first
	 * word, so it is always the primitive that was refused: `tap`, `tapAt`, `doubleTap`, `swipe`, `typeKeys`,
	 * `clearText`, `submitTextEntry`, `pressBack`, `foreground`, `launch`, `terminate`, `captureFailure`, `alert` or
	 * `guard` for the keyboard guard) and keeps the whole line as the reason the gesture or text entry in progress was
	 * refused. A static test reads the sources and fails if a call does not name one of those primitives as a literal,
	 * and that this list and the one of the iOS driver are the list of the test.
	 */
	fun refuse(primitive: String, reason: String) {
		refusal = "$primitive $reason"
		write("[refusal] $primitive $reason")
	}

	/**
	 * The one funnel of every tolerance (YB-9): something the driver put up with and went on. Writes
	 * `[tolerance] <key> <detail>`, which the harness counts by the key wherever it stands in the line. The key is a
	 * literal that a static test finds in its list of declared keys, so rewriting the text of a message cannot make a
	 * tolerance go uncounted.
	 */
	fun tolerate(key: String, detail: String) {
		write("[tolerance] $key $detail")
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
