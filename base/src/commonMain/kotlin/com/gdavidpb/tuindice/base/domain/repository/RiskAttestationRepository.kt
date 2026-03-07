package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.RiskAttestation
import com.gdavidpb.tuindice.base.domain.model.RiskAttestationRequest

interface RiskAttestationRepository {
    suspend fun issueProof(request: RiskAttestationRequest): RiskAttestation
}
