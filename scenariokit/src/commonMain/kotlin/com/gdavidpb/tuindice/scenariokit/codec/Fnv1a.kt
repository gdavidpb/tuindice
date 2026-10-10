package com.gdavidpb.tuindice.scenariokit.codec

/** FNV-1a, 64 bits, as 16 lowercase hex digits; stable across platforms and releases. */
internal object Fnv1a {
	private const val OFFSET_BASIS = -3750763034362895579L // 0xcbf29ce484222325
	private const val PRIME = 1099511628211L // 0x100000001b3
	private const val HEX_DIGITS = 16

	fun hex(text: String): String {
		var hash = OFFSET_BASIS
		for (byte in text.encodeToByteArray()) {
			hash = (hash xor (byte.toLong() and BYTE_MASK)) * PRIME
		}
		return hash.toULong().toString(HEX_RADIX).padStart(HEX_DIGITS, '0')
	}

	private const val BYTE_MASK = 0xFFL
	private const val HEX_RADIX = 16
}
