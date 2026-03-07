package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.RiskAttestationRequest
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.RiskAttestationRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.canonicalRiskPayloadJson
import com.gdavidpb.tuindice.login.domain.model.IssueTokensFlow
import com.gdavidpb.tuindice.login.domain.model.IssueTokensRiskPayload
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignInUseCase(
	private val loginRepository: LoginRepository,
	private val messagingRepository: MessagingRepository,
	private val riskAttestationRepository: RiskAttestationRepository,
	override val paramsValidator: SignInParamsValidator,
	override val exceptionHandler: SignInExceptionHandler
) : FlowUseCase<SignInParams, Unit, SignInUseCaseError>() {
	override suspend fun executeOnBackground(params: SignInParams): Flow<Unit> {
		val flow = IssueTokensFlow.IssueTokens
		val riskPayload = IssueTokensRiskPayload(
			usbId = params.usbId,
			password = params.password,
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

		loginRepository.issueTokens(
			usbId = params.usbId,
			password = params.password,
			flow = flow,
			riskAttestation = riskAttestation
		)
		messagingRepository.subscribe()

		return flowOf(Unit)
	}
}
