package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import kotlin.random.Random

class UUIDIdentifierDataSource : IdentifierRepository {
	override fun generateRandomIdentifier(): String {
		val bytes = Random.nextBytes(16)

		return buildString(bytes.size * 2) {
			bytes.forEach { byte ->
				val value = byte.toInt() and 0xFF
				append(HEX_DIGITS[value ushr 4])
				append(HEX_DIGITS[value and 0x0F])
			}
		}
	}

	private companion object {
		const val HEX_DIGITS = "0123456789abcdef"
	}
}
