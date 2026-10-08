package com.gdavidpb.tuindice.scenarios

/**
 * Reads the sources of the two drivers and finds the calls of their refusal funnels (`refuse(` in both, and the XCTest
 * wrapper `guarded(` of iOS, which refuses through it) whose first argument is not a primitive. The harness counts
 * every `[refusal] <reason>` line by the first word of the reason, so that word must be the primitive refused.
 *
 * A call passes when its first argument is a string literal that is exactly a primitive of [PRIMITIVES], or when it is
 * the identifier `primitive` inside a function that declares a parameter of that name (the helpers that forward the
 * primitive their caller names). The declarations of the funnels and the comments are not calls.
 */
internal object DriverRefusalSources {
	/** The primitives a refusal may name; `guard` is the keyboard guard, the rest are the primitives of the driver. */
	val PRIMITIVES = setOf(
		"tap", "tapAt", "doubleTap", "swipe", "typeKeys", "clearText", "submitTextEntry", "pressBack",
		"foreground", "launch", "terminate", "scroll", "guard", "captureFailure", "alert"
	)

	private val call = Regex("""\b(?:refuse|guarded)\(\s*(?:"([^"]*)"|([\w_]+))""")
	private val declaration = Regex("""\b(?:fun|func)\s+(?:refuse|guarded)\(""")
	private val anyFunction = Regex("""\b(?:fun|func)\s""")

	/** The calls in [text] that name no primitive, as `file:line: the line`. */
	fun violations(file: String, text: String): List<String> {
		val lines = text.lines()

		return lines.indices.flatMap { index ->
			val line = lines[index]
			val code = line.trim()
			val isComment = code.startsWith("//") || code.startsWith("*") || code.startsWith("/*")

			if (isComment || declaration.containsMatchIn(line)) {
				emptyList()
			} else {
				call.findAll(line).filter { !acceptable(it, lines, index) }.map { "$file:${index + 1}: $code" }.toList()
			}
		}
	}

	private fun acceptable(match: MatchResult, lines: List<String>, index: Int): Boolean {
		val literal = match.groups[1]?.value
		val identifier = match.groups[2]?.value

		return when {
			literal != null -> literal in PRIMITIVES
			identifier == "primitive" -> enclosingFunction(lines, index).contains("primitive:")
			else -> false
		}
	}

	/** The signature line of the function that holds line [index]: the nearest line above that declares one. */
	private fun enclosingFunction(lines: List<String>, index: Int): String =
		(index downTo 0).map { lines[it] }.firstOrNull { anyFunction.containsMatchIn(it) }.orEmpty()
}
