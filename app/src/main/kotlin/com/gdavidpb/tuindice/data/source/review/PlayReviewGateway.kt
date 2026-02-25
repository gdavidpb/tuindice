package com.gdavidpb.tuindice.data.source.review

import com.gdavidpb.tuindice.data.source.activity.CurrentActivityProvider
import com.gdavidpb.tuindice.base.domain.repository.ReviewGateway
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManager

class PlayReviewGateway(
	private val reviewManager: ReviewManager,
	private val currentActivityProvider: CurrentActivityProvider
) : ReviewGateway {
	override suspend fun launchReview() {
		val activity = currentActivityProvider.get() ?: return
		val reviewInfo = reviewManager.requestReview()

		reviewManager.launchReview(
			activity = activity,
			reviewInfo = reviewInfo
		)
	}
}
