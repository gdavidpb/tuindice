package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class RequestReviewUseCase(
	private val settingsRepository: SettingsRepository,
	private val configRepository: ConfigRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		val syncsCount = configRepository.getSyncsToSuggestReview()

		return if (settingsRepository.isReviewSuggested(value = syncsCount)) {
			flowOf(Unit)
		} else {
			emptyFlow()
		}
	}
}
