package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Attestation

interface AttestationRepository {
	suspend fun getAttestation(payload: String): Attestation
}