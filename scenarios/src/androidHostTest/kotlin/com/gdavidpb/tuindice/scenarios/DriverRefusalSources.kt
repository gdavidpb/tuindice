package com.gdavidpb.tuindice.scenarios

/**
 * Reads the sources of the two drivers and finds the calls of their refusal funnels (`refuse(` in both, and the
 * XCTest wrapper `guarded(` of iOS, which refuses through it) whose first argument is not a primitive. The harness
 * counts every `[refusal] <reason>` line by the first word of the reason, so that word must be the primitive refused.
 *
 * The text is read whole, with the comments blanked out, so a call whose argument is on the next line (the shape the
 * line length pushes to) is seen as the call it is (YB-8). A call passes when its first argument is a string literal
 * that is exactly a primitive of [PRIMITIVES], or when it is the identifier `primitive` inside a function that
 * declares a parameter of that name (the helpers that forward the primitive their caller names). The declarations of
 * the funnels are not calls.
 *
 * The helpers that forward a primitive are held to the same rule from the other side: a literal passed as
 * `primitive: "..."` (Swift) or as the third argument of `click(` and `tapper(` (the primitive of the gesture they
 * refuse for) belongs to [PRIMITIVES] too.
 */
internal object DriverRefusalSources {
	/** The primitives a refusal may name; `guard` is the keyboard guard, the rest are the primitives of the driver. */
	val PRIMITIVES = setOf(
		"tap", "tapAt", "doubleTap", "swipe", "typeKeys", "clearText", "submitTextEntry", "pressBack",
		"foreground", "launch", "terminate", "guard", "captureFailure", "alert"
	)

	private val call = Regex("""\b(?:refuse|guarded)\(\s*(?:"([^"]*)"|([\w_]+))""")
	private val declarationBefore = Regex("""\b(?:fun|func)\s+$""")
	private val anyFunction = Regex("""\b(?:fun|func)\s""")
	private val namedPrimitive = Regex("""\bprimitive:\s*"([^"]*)"""")
	private val thirdArgument = Regex(
		"""\b(?:click|tapper)\((?:[^,()]|\([^()]*\))+,(?:[^,()]|\([^()]*\))+,\s*"([^"]*)""""
	)

	/** The calls in [text] that name no primitive, as `file:line: the call`. */
	fun violations(file: String, text: String): List<String> {
		val code = withoutComments(text)
		val refusals = call.findAll(code)
			.filter { !declarationBefore.containsMatchIn(code.substring(0, it.range.first)) }
			.filter { !acceptable(it, code) }
		val named = namedPrimitive.findAll(code).filter { it.groupValues[1] !in PRIMITIVES }
		val forwarded = thirdArgument.findAll(code).filter { it.groupValues[1] !in PRIMITIVES }

		return (refusals + named + forwarded).map { describe(file, code, it) }.toList()
	}

	private fun describe(file: String, code: String, match: MatchResult): String {
		val line = code.substring(0, match.range.first).count { it == '\n' } + 1

		return "$file:$line: ${match.value.replace(Regex("""\s+"""), " ")}"
	}

	private fun acceptable(match: MatchResult, code: String): Boolean {
		val literal = match.groups[1]?.value
		val identifier = match.groups[2]?.value

		return when {
			literal != null -> literal in PRIMITIVES
			identifier == "primitive" -> enclosingSignature(code, match.range.first).contains("primitive:")
			else -> false
		}
	}

	/** The signature of the function that holds [at]: from the nearest `fun`/`func` above to the brace of its body. */
	private fun enclosingSignature(code: String, at: Int): String {
		val start = anyFunction.findAll(code.substring(0, at)).lastOrNull()?.range?.first ?: return ""
		val brace = code.indexOf('{', start)

		return code.substring(start, if (brace < 0) code.length else brace)
	}

	/**
	 * [text] with every comment (`//` to the end of the line, `/* ... */` and so KDoc) replaced by spaces, new lines
	 * kept so the offsets and the line numbers stay. A string is skipped whole so a `//` inside it is not a comment; a
	 * string never continues on the next line, so a quote that this simple scan pairs wrongly (a Swift interpolation
	 * with strings inside) can mislead it only until the end of its own line.
	 */
	fun withoutComments(text: String): String = CommentScanner(text).blanked()

	private class CommentScanner(private val text: String) {
		private enum class Mode { CODE, STRING, LINE, BLOCK }

		private val out = StringBuilder(text.length)
		private var mode = Mode.CODE
		private var index = 0

		fun blanked(): String {
			while (index < text.length) {
				val c = text[index]
				val next = text.getOrNull(index + 1)

				when {
					c == '\n' -> newLine()
					mode == Mode.CODE -> code(c, next)
					mode == Mode.STRING -> string(c, next)
					mode == Mode.LINE -> blank(1)
					else -> block(c, next)
				}
			}

			return out.toString()
		}

		private fun newLine() {
			if (mode != Mode.BLOCK) mode = Mode.CODE
			out.append('\n')
			index++
		}

		private fun code(c: Char, next: Char?) {
			when {
				c == '"' -> {
					mode = Mode.STRING
					keep(1)
				}
				c == '/' && next == '/' -> {
					mode = Mode.LINE
					blank(2)
				}
				c == '/' && next == '*' -> {
					mode = Mode.BLOCK
					blank(2)
				}
				else -> keep(1)
			}
		}

		private fun string(c: Char, next: Char?) {
			when {
				c == '\\' && next != null && next != '\n' -> keep(2)
				c == '"' -> {
					mode = Mode.CODE
					keep(1)
				}
				else -> keep(1)
			}
		}

		private fun block(c: Char, next: Char?) {
			if (c == '*' && next == '/') {
				mode = Mode.CODE
				blank(2)
			} else {
				blank(1)
			}
		}

		private fun keep(count: Int) {
			out.append(text, index, index + count)
			index += count
		}

		private fun blank(count: Int) {
			repeat(count) { out.append(' ') }
			index += count
		}
	}
}
