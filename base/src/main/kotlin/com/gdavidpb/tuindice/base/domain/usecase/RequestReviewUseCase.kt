package com.gdavidpb.tuindice.base.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class RequestReviewUseCase(
	private val settingsRepository: SettingsRepository,
	private val configRepository: ConfigRepository
) : FlowUseCase<ReviewManager, ReviewInfo, Nothing>() {
	override suspend fun executeOnBackground(params: ReviewManager): Flow<ReviewInfo> {
		val syncsCount = configRepository.getSyncsToSuggestReview()

		return if (settingsRepository.isReviewSuggested(value = syncsCount))
			flowOf(params.requestReview())
		else
			emptyFlow()
	}
}