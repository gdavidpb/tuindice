package com.gdavidpb.tuindice.base.data.repository.source.uuid

import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import java.util.UUID

class UUIDIdentifierDataSource : IdentifierRepository {
	override fun generateRandomIdentifier(): String {
		return UUID.randomUUID().toString()
	}
}