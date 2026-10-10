package com.gdavidpb.tuindice.scenarios

/**
 * Reads the sources of the two drivers and finds the calls of their tolerance funnel (`tolerate(`), whose first
 * argument must be a key that is declared (YB-9). The harness counts every `[tolerance] <key> <detail>` line by its
 * key, and a key that nobody declared is one the harness and the documentation do not know of.
 *
 * Kotlin passes the key as a string literal that belongs to [ANDROID_KEYS]; Swift passes a case of the `Tolerance`
 * enum, which must be one of the cases the enum declares ([swiftCases] reads them).
 */
internal object DriverToleranceSources {
	/** The tolerances the Android driver may put up with, each one a key of the `[tolerance]` lines it writes. */
	val ANDROID_KEYS = setOf(
		"foreground-request",
		"foreground-not-in-front",
		"keyboard-taken-as-hidden",
		"keyboard-unreadable"
	)

	private val call = Regex("""\btolerate\(\s*(?:"([^"]*)"|\.(\w+)|([\w_]+))""")
	private val declarationBefore = Regex("""\b(?:fun|func)\s+$""")
	private val enumCase = Regex("""(?m)^\s*case\s+(\w+)\s*=\s*"[^"]+"""")

	/** The cases of `enum Tolerance` in the Swift source [runConfig]. */
	fun swiftCases(runConfig: String): Set<String> {
		val code = DriverRefusalSources.withoutComments(runConfig)
		val start = code.indexOf("enum Tolerance")
		val end = if (start < 0) -1 else code.indexOf('}', start)

		return if (end < 0) emptySet() else enumCase.findAll(code.substring(start, end)).map { it.groupValues[1] }.toSet()
	}

	/**
	 * The calls in [text] whose key is not in [allowed] (Kotlin: a literal key; Swift: a case name), as
	 * `file:line: the call`.
	 */
	fun violations(file: String, text: String, allowed: Set<String>): List<String> {
		val code = DriverRefusalSources.withoutComments(text)

		return call.findAll(code)
			.filter { !declarationBefore.containsMatchIn(code.substring(0, it.range.first)) }
			.filter { match -> keyOf(match).let { key -> key == null || key !in allowed } }
			.map { "$file:${lineOf(code, it)}: ${it.value.replace(Regex("""\s+"""), " ")}" }
			.toList()
	}

	private fun keyOf(match: MatchResult): String? = match.groups[1]?.value ?: match.groups[2]?.value

	private fun lineOf(code: String, match: MatchResult): Int =
		code.substring(0, match.range.first).count { it == '\n' } + 1
}
