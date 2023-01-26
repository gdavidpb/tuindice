package com.gdavidpb.tuindice.base.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager

class RequestReviewUseCase(
	private val settingsRepository: SettingsRepository,
	override val configRepository: ConfigRepository,
	override val reportingRepository: ReportingRepository
) : EventUseCase<ReviewManager, ReviewInfo, Nothing>() {
	override suspend fun executeOnBackground(params: ReviewManager): ReviewInfo? {
		val syncsCount = configRepository.getSyncsToSuggestReview()

		return if (settingsRepository.isReviewSuggested(value = syncsCount))
			params.requestReview()
		else
			null
	}
}