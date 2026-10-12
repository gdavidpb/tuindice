package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

/**
 * The server ending the session behind the user's back, told as it happens. The latest
 * invalidation is kept until someone observes, so one that arrives while nobody is listening is
 * still delivered, once; and if the process dies first, the notice is still kept for the next start.
 */
class ObserveSessionInvalidationUseCase(
	private val sessionInvalidationRepository: SessionInvalidationRepository,
	private val settingsRepository: SettingsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		// The notice reaching the interface is the notice shown: the copy kept for the next start
		// (written by the invalidation, in case nobody was here to show it) is consumed so it is
		// not shown a second time.
		return sessionInvalidationRepository.observeSessionInvalidation().onEach {
			settingsRepository.consumeSessionResetNoticePending()
		}
	}
}
