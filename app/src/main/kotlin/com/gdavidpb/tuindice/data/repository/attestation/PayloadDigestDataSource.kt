package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.domain.model.AttestationPayload

interface PayloadDigestDataSource {
	suspend fun digest(challenge: String, payload: AttestationPayload): String
}