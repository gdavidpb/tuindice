package com.gdavidpb.tuindice.base.data.source


import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UUIDIdentifierDataSource : IdentifierRepository {
	override fun generateRandomIdentifier(): String {
		return Uuid.random().toString()
	}
}