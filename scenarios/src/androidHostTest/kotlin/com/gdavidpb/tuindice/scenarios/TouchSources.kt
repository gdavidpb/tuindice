package com.gdavidpb.tuindice.scenarios

/**
 * Finds, in a source of the Android driver, every way to put a touch on the screen: `.click(`, `.longClick(`,
 * a `MotionEvent` and `sendPointerSync` (YB-2). The focus and text paths of the driver must not have one, because a
 * touch on a point read from the tree, however steady the reads were, can land on a key of the keyboard that is
 * opening (the "v" of ZB-3). The logcat probe sees the touches UI Automator writes a line for; this rule sees the
 * rest, and the ones whose line it does not write.
 */
internal object TouchSources {
	private val touch = Regex("""\.(?:click|longClick)\(|\bMotionEvent\b|\bsendPointerSync\b""")

	/** The touches in [text], as `file:line: the touch`. */
	fun touches(file: String, text: String): List<String> {
		val code = DriverRefusalSources.withoutComments(text)

		return touch.findAll(code).map { "$file:${lineOf(code, it)}: ${it.value}" }.toList()
	}

	private fun lineOf(code: String, match: MatchResult): Int =
		code.substring(0, match.range.first).count { it == '\n' } + 1
}
