package com.gdavidpb.tuindice.chatcontract.attachment

/**
 * The file name in [AttachmentHeaders.NAME]: UTF-8, percent-encoded as RFC 3986 asks (every byte outside the unreserved
 * characters, so a space is `%20` and a `+` is `%2B`), and cut to [AttachmentLimits.MAX_NAME_LENGTH] code points.
 */
public object AttachmentName {
	private const val HEX = "0123456789ABCDEF"
	private const val UNRESERVED = "-._~"
	private const val ESCAPE = '%'
	private const val ESCAPE_LENGTH = 3
	private const val BYTE_MASK = 0xFF
	private const val NIBBLE_MASK = 0x0F
	private const val NIBBLE_BITS = 4
	private const val ASCII_END = 0x80

	/** The name as the header carries it, cut first. */
	public fun encode(name: String): String = buildString {
		truncate(name).encodeToByteArray().forEach { byte ->
			val code = byte.toInt() and BYTE_MASK
			val char = code.toChar()
			if (code < ASCII_END && (char.isLetterOrDigit() || char in UNRESERVED)) {
				append(char)
			} else {
				append(ESCAPE).append(HEX[code shr NIBBLE_BITS]).append(HEX[code and NIBBLE_MASK])
			}
		}
	}

	/** The name the header carries, or null when it is not well percent-encoded UTF-8 or is blank. */
	public fun decode(header: String): String? =
		header.percentDecodedBytes()
			?.let { bytes -> runCatching { bytes.decodeToString(throwOnInvalidSequence = true) }.getOrNull() }
			?.takeIf { it.isNotBlank() }
			?.let(::truncate)

	/** The name cut to [AttachmentLimits.MAX_NAME_LENGTH] code points, never inside a surrogate pair. */
	public fun truncate(name: String): String {
		var index = 0
		var codePoints = 0

		while (index < name.length && codePoints < AttachmentLimits.MAX_NAME_LENGTH) {
			index += if (name[index].isHighSurrogate() && index + 1 < name.length) 2 else 1
			codePoints++
		}
		return name.substring(0, index)
	}

	// The bytes the header spells, or null when an escape is cut or not hex, or a character is not ASCII.
	private fun String.percentDecodedBytes(): ByteArray? {
		val bytes = ArrayList<Byte>(length)
		var index = 0
		var valid = true

		while (valid && index < length) {
			val char = this[index]
			val byte = when {
				char == ESCAPE -> escapedByte(index)
				char.code < ASCII_END -> char.code
				else -> null
			}
			if (byte == null) {
				valid = false
			} else {
				bytes.add(byte.toByte())
				index += if (char == ESCAPE) ESCAPE_LENGTH else 1
			}
		}
		return if (valid) bytes.toByteArray() else null
	}

	private fun String.escapedByte(index: Int): Int? {
		if (index + ESCAPE_LENGTH > length) return null
		val high = HEX.indexOf(this[index + 1].uppercaseChar())
		val low = HEX.indexOf(this[index + 2].uppercaseChar())
		return if (high < 0 || low < 0) null else (high shl NIBBLE_BITS) or low
	}
}
