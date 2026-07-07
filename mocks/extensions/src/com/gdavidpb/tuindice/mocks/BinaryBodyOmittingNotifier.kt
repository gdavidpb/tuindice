package com.gdavidpb.tuindice.mocks

import com.github.tomakehurst.wiremock.common.Notifier

// Verbose WireMock logs every request verbatim, so binary uploads (profile
// pictures) land in the log as megabytes of mojibake. Lines carrying binary
// artifacts (control characters or U+FFFD replacements, which never appear in
// the JSON traffic worth reading) are collapsed into a single omission marker;
// textual lines pass through untouched.
class BinaryBodyOmittingNotifier(
	private val delegate: Notifier
) : Notifier {
	override fun info(message: String) = delegate.info(sanitize(message))

	override fun error(message: String) = delegate.error(sanitize(message))

	override fun error(message: String, t: Throwable) = delegate.error(sanitize(message), t)

	private fun sanitize(message: String): String {
		if (message.none { character -> character.isBinaryArtifact() }) return message

		val sanitized = StringBuilder()
		var omittedCharacters = 0

		fun flushOmissionMarker() {
			if (omittedCharacters > 0) {
				sanitized.appendLine("[binary content omitted: $omittedCharacters characters]")
				omittedCharacters = 0
			}
		}

		for (line in message.lineSequence()) {
			if (line.any { character -> character.isBinaryArtifact() }) {
				omittedCharacters += line.length + 1
			} else {
				flushOmissionMarker()
				sanitized.appendLine(line)
			}
		}

		flushOmissionMarker()

		return sanitized.toString().trimEnd('\n')
	}

	// Lines come from lineSequence, so \n and \r never appear here; \t is the
	// only control character legitimate in text.
	private fun Char.isBinaryArtifact(): Boolean =
		this == REPLACEMENT_CHARACTER || (isISOControl() && this != '\t')

	private companion object {
		val REPLACEMENT_CHARACTER = 0xFFFD.toChar()
	}
}
