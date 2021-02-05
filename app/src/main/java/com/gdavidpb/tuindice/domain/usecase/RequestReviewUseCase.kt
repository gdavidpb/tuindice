package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.utils.KEY_SYNCS_TO_SUGGEST_REVIEW
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager

class RequestReviewUseCase(
        private val settingsRepository: SettingsRepository,
        private val configRepository: ConfigRepository
) : ResultUseCase<ReviewManager, ReviewInfo, Nothing>() {
    override suspend fun executeOnBackground(params: ReviewManager): ReviewInfo? {
        val syncsCount = configRepository.getLong(KEY_SYNCS_TO_SUGGEST_REVIEW).toInt()

        return if (settingsRepository.isReviewSuggested(value = syncsCount))
            params.requestReview()
        else
            null
    }
}