package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerConsentRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerModeRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantStandingRepository
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.turn.AssistantStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Brings the device in line with the server when the sheet opens or refreshes: it reads the status the server gives the
 * student, applies its mode and its consent, and sends the outcomes of the intents the server has not taken yet.
 */
class SyncAssistantUseCase(
	private val standingRepository: AssistantStandingRepository,
	private val serverModeRepository: AssistantServerModeRepository,
	private val serverConsentRepository: AssistantServerConsentRepository,
	private val confirmer: AssistantIntentConfirmer,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, AssistantStatus, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<AssistantStatus> = flow {
		val status = standingRepository.status()

		serverModeRepository.reportServerMode(AssistantMode.fromRemoteValue(status.mode))
		serverConsentRepository.reconcileConsent(status.consent)
		confirmer.flushPendingOutcomes()

		emit(status)
	}
}
