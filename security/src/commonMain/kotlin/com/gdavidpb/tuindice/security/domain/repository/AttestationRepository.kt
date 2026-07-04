package com.gdavidpb.tuindice.security.domain.repository

import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest

interface AttestationRepository {
    suspend fun attest(request: AttestationRequest): Attestation
}
