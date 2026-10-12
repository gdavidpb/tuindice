package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

/**
 * The server ending the session behind the user's back, told as it happens. The latest
 * invalidation is kept until someone observes, so one that arrives while nobody is listening is
 * still delivered, once.
 *
 * The notice the invalidation persisted is the one source of truth for "the user has not been told":
 * a live signal is delivered only while that notice is still pending, so a start-up that already
 * showed it (it read it before the signal arrived) is not followed by a second one. It is not taken
 * here: [AcknowledgeSessionInvalidationUseCase] takes it once the interface showed it, so a process
 * that dies between the signal and the screen (a background kill) still explains the sign-in screen
 * at the next start.
 */
class ObserveSessionInvalidationUseCase(
	private val sessionInvalidationRepository: SessionInvalidationRepository,
	private val settingsRepository: SettingsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		return sessionInvalidationRepository.observeSessionInvalidation().filter {
			settingsRepository.hasSessionResetNoticePending()
		}
	}
}
