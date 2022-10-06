package com.gdavidpb.tuindice.base.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager

class RequestReviewUseCase(
    private val settingsRepository: SettingsRepository,
    private val configRepository: ConfigRepository
) : EventUseCase<ReviewManager, ReviewInfo, Nothing>() {
    override suspend fun executeOnBackground(params: ReviewManager): ReviewInfo? {
        val syncsCount = configRepository.getLong(ConfigKeys.SYNCS_TO_SUGGEST_REVIEW).toInt()

        return if (settingsRepository.isReviewSuggested(value = syncsCount))
            params.requestReview()
        else
            null
    }
}