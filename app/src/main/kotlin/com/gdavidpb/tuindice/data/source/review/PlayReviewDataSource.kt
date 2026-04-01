package com.gdavidpb.tuindice.data.source.review

import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityDataSource
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManager

class PlayReviewDataSource(
	private val reviewManager: ReviewManager,
	private val currentActivityDataSource: CurrentActivityDataSource
) : ReviewRepository {
	override suspend fun launchReview() {
		val activity = currentActivityDataSource.get() ?: return
		val reviewInfo = reviewManager.requestReview()

		reviewManager.launchReview(
			activity = activity,
			reviewInfo = reviewInfo
		)
	}
}
