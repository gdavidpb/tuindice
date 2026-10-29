package com.gdavidpb.tuindice.base.logging

import kotlin.concurrent.Volatile

/**
 * The debug log of every screen's state machine: what it is given, the row that takes it, the state it leaves, what
 * it rejects, the effects it sends and the jobs it runs, with their data. It writes nowhere until a debug build gives
 * it a [sink], so a release build neither formats nor writes a line: the states and events carry the student's record.
 */
object MachineLog {
	@Volatile
	var sink: ((String) -> Unit)? = null

	// The unified log of iOS cuts a line near a kilobyte, so a long state is written in numbered pieces.
	private const val PIECE_LENGTH = 800

	fun write(machine: String, message: () -> String) {
		val currentSink = sink ?: return
		val text = message()

		if (text.length <= PIECE_LENGTH) {
			currentSink("[$machine] $text")
			return
		}

		val pieces = text.chunked(PIECE_LENGTH)
		pieces.forEachIndexed { index, piece ->
			currentSink("[$machine] (${index + 1}/${pieces.size}) $piece")
		}
	}
}
