package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.domain.model.attestation.AttestationPayload

interface LocalDataSource {
	suspend fun getNonce(identifier: String, payload: AttestationPayload): String
}