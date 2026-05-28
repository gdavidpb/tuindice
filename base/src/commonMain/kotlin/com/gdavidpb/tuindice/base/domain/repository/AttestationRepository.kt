package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest

interface AttestationRepository {
    suspend fun attest(request: AttestationRequest): Attestation
}
