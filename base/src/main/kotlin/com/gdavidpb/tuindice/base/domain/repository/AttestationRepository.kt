package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload

interface AttestationRepository {
	suspend fun getAttestation(payload: AttestationPayload): Attestation
}