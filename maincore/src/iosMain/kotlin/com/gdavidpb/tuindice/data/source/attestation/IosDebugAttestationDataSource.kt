package com.gdavidpb.tuindice.data.source.attestation


import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.logging.appLogger

class IosDebugAttestationDataSource : AttestationRepository {
	private val logger = appLogger(tag = "Attestation")

	override suspend fun attest(request: AttestationRequest): Attestation {
		logger.i { "[ios-debug] attest(): returning mocked attestation token for ${request.operationCode.value}." }
		return Attestation(token = "attestation.mock.proof.ios")
	}
}
