package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.domain.model.RiskAttestation
import com.gdavidpb.tuindice.base.domain.model.RiskAttestationRequest
import com.gdavidpb.tuindice.base.domain.repository.RiskAttestationRepository
import com.gdavidpb.tuindice.base.logging.appLogger

class IosDebugRiskAttestationDataRepository(
) : RiskAttestationRepository {
	private val logger = appLogger(tag = "Attestation")

	override suspend fun issueProof(request: RiskAttestationRequest): RiskAttestation {
		logger.i { "[ios-debug] issueProof(): returning mocked proof token for ${request.operation.code}." }
		return RiskAttestation(token = "risk.mock.proof.ios")
	}
}
