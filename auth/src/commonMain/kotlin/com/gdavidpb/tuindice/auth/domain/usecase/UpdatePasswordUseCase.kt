package com.gdavidpb.tuindice.auth.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.RiskAttestationRequest
import com.gdavidpb.tuindice.base.domain.repository.RiskAttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.canonicalRiskPayloadJson
import com.gdavidpb.tuindice.auth.domain.model.IssueTokensFlow
import com.gdavidpb.tuindice.auth.domain.model.IssueTokensRiskPayload
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.UpdatePasswordParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdatePasswordUseCase(
	private val authRepository: AuthRepository,
	private val sessionRepository: SessionRepository,
	private val riskAttestationRepository: RiskAttestationRepository,
	override val paramsValidator: UpdatePasswordParamsValidator,
	override val exceptionHandler: UpdatePasswordExceptionHandler
) : FlowUseCase<String, Unit, SignInUseCaseError>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		val usbId = sessionRepository.getUsbId()
		val flow = IssueTokensFlow.ReissueTokens
		val riskPayload = IssueTokensRiskPayload(
			usbId = usbId,
			password = params,
			authFlow = flow.headerValue
		)

		val riskAttestation = riskAttestationRepository.issueProof(
			request = RiskAttestationRequest(
				operation = flow.operation,
				payloadJson = canonicalRiskPayloadJson(
					serializer = IssueTokensRiskPayload.serializer(),
					value = riskPayload
				)
			)
		)

		authRepository.issueTokens(
			usbId = usbId,
			password = params,
			flow = flow,
			riskAttestation = riskAttestation
		)

		return flowOf(Unit)
	}
}
