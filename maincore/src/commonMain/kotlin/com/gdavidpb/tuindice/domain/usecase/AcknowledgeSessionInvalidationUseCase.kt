package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * The interface showed the notice of a session that ended behind the user's back: the copy kept for
 * the next start (written by the invalidation, in case nobody was here to show it) is taken, so the
 * user sees the notice once, on the first occasion the interface could show it. It is taken by its
 * stamp: a newer notice persisted in the meantime is not the one that was shown.
 */
class AcknowledgeSessionInvalidationUseCase(
	private val settingsRepository: SettingsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Long, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Long): Flow<Unit> {
		settingsRepository.consumeSessionResetNotice(stamp = params)

		return flowOf(Unit)
	}
}
