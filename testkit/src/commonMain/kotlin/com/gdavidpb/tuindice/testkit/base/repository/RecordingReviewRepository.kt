package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository

class RecordingReviewRepository : ReviewRepository {
	var launchCalls = 0
		private set

	override suspend fun launchReview() {
		launchCalls++
	}
}
