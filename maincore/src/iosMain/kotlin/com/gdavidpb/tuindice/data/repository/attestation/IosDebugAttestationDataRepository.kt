package com.gdavidpb.tuindice.data.repository.attestation

import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.logging.appLogger

class IosDebugAttestationDataRepository : AttestationRepository {
	private val logger = appLogger(tag = "Attestation")

	override suspend fun attest(request: AttestationRequest): Attestation {
		logger.i { "[ios-debug] attest(): returning mocked attestation token for ${request.operationCode.value}." }
		return Attestation(token = "attestation.mock.proof.ios")
	}
}
