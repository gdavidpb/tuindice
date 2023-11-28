package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.attestation.AttestationPayload

interface AttestationRepository {
	suspend fun getToken(operation: String, payload: AttestationPayload): String
}