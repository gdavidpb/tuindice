package com.gdavidpb.tuindice.data.source.review

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.data.model.playcore.PlayCoreSurface
import com.gdavidpb.tuindice.data.repository.playcore.PlayCoreAvailabilityDataRepository
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityDataSource
import com.gdavidpb.tuindice.data.source.playcore.reportPlayCoreFailure
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManager

class PlayReviewDataSource(
	private val reviewManager: ReviewManager,
	private val currentActivityDataSource: CurrentActivityDataSource,
	private val playCoreAvailabilityRepository: PlayCoreAvailabilityDataRepository,
	private val reportingRepository: ReportingRepository
) : ReviewRepository {
	override suspend fun launchReview() {
		if (!playCoreAvailabilityRepository.isAvailable(PlayCoreSurface.Review)) return

		val activity = currentActivityDataSource.get() ?: return

		runCatching {
			val reviewInfo = reviewManager.requestReview()

			reviewManager.launchReview(
				activity = activity,
				reviewInfo = reviewInfo
			)
		}.onFailure { throwable ->
			reportingRepository.reportPlayCoreFailure(
				message = "play_review_failed",
				throwable = throwable
			)
		}
	}
}
