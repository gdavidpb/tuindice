package com.gdavidpb.tuindice.data.source.review

import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.platform.IosReviewCapability

class IosReviewDataSource(
	private val reviewCapability: IosReviewCapability
) : ReviewRepository {
	override suspend fun launchReview() {
		reviewCapability.launchReview()
	}
}
