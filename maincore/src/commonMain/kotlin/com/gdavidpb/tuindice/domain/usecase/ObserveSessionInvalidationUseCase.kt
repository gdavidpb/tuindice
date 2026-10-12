package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.SessionResetNotice
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The server ending the session behind the user's back, told as it happens. The latest
 * invalidation is kept until someone observes, so one that arrives while nobody is listening is
 * still delivered, once.
 *
 * Every signal is delivered: taking the user to the sign-in never depends on the notice. What
 * accompanies it is the reason of the notice the invalidation persisted, read when the signal
 * arrives, or null when that notice is no longer pending (a start-up that already showed it before
 * the signal arrived): the signal then carries nothing to say, and the user is not told twice. The
 * notice is not taken here: [AcknowledgeSessionInvalidationUseCase] takes it once the interface
 * showed it, so a process that dies between the signal and the screen (a background kill) still
 * explains the sign-in screen at the next start.
 */
class ObserveSessionInvalidationUseCase(
	private val sessionInvalidationRepository: SessionInvalidationRepository,
	private val settingsRepository: SettingsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, SessionResetNotice?, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<SessionResetNotice?> {
		return sessionInvalidationRepository.observeSessionInvalidation().map {
			settingsRepository.getSessionResetNotice()
		}
	}
}
