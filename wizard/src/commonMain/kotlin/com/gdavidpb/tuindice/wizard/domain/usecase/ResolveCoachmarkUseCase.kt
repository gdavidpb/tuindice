package com.gdavidpb.tuindice.wizard.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.wizard.domain.model.CoachmarkResolution
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ResolveCoachmarkUseCase(
	private val settingsRepository: SettingsRepository,
	private val sessionRepository: SessionRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<ResolveCoachmarkUseCase.Params, CoachmarkResolution?, Nothing>() {
	override suspend fun executeOnBackground(params: Params): Flow<CoachmarkResolution?> {
		if (!sessionRepository.hasActiveSession() || params.eligibleCoachmarkIds.isEmpty()) {
			return flowOf(null)
		}

		settingsRepository.migrateLegacyOnboardingState(
			completedCoachmarkIds = params.allCoachmarkIds
		)

		val knownCoachmarkIds = params.allCoachmarkIds
		val seenCoachmarkIds = settingsRepository.getSeenCoachmarkIds()
			.filter { coachmarkId -> coachmarkId in knownCoachmarkIds }
			.toSet()
		val pendingCoachmarkIds = params.eligibleCoachmarkIds
			.filter { coachmarkId -> coachmarkId !in seenCoachmarkIds }

		return flowOf(
			pendingCoachmarkIds.firstOrNull()?.let {
				CoachmarkResolution(
					pendingCoachmarkIds = pendingCoachmarkIds
				)
			}
		)
	}

	data class Params(
		val eligibleCoachmarkIds: List<String>,
		val allCoachmarkIds: Set<String>
	)
}
