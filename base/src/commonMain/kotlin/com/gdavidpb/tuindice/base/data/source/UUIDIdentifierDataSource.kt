package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import kotlin.random.Random

class UUIDIdentifierDataSource : IdentifierRepository {
	override fun generateRandomIdentifier(): String {
		val bytes = Random.nextBytes(16)

		return buildString(bytes.size * 2) {
			bytes.forEach { byte ->
				append("%02x".format(byte))
			}
		}
	}
}
