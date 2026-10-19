package com.gdavidpb.tuindice.assistant.domain.mapper

import io.ktor.util.sha256
import kotlin.uuid.Uuid

// The same photo gives the same id, so a retry of it reaches the service with the id of the proposal it already has.
// The id is the first 128 bits of the SHA-256 of the encoded bytes, written as a version 8 UUID.
internal fun ByteArray.toClientRequestId(): String {
	val bytes = sha256(this).copyOf(UUID_BYTES)

	bytes[VERSION_INDEX] = ((bytes[VERSION_INDEX].toInt() and VERSION_MASK) or VERSION_8).toByte()
	bytes[VARIANT_INDEX] = ((bytes[VARIANT_INDEX].toInt() and VARIANT_MASK) or VARIANT_RFC_4122).toByte()

	return Uuid.fromByteArray(bytes).toString()
}

private const val UUID_BYTES = 16
private const val VERSION_INDEX = 6
private const val VARIANT_INDEX = 8
private const val VERSION_MASK = 0x0F
private const val VERSION_8 = 0x80
private const val VARIANT_MASK = 0x3F
private const val VARIANT_RFC_4122 = 0x80
