package com.gdavidpb.tuindice.data

import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import java.util.UUID

class AttestationMockDataRepository : AttestationRepository {
	override suspend fun getToken(operation: String, payload: String): String {
		return UUID.randomUUID().toString()
	}
}